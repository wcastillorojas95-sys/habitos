package com.lucas.habitos

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Sostiene la cuenta atras de una sesion de enfoque.
 *
 * Existe por una razon concreta: si el temporizador viviera solo en la pantalla,
 * Android podria matar la app al apagar el telefono o al cambiar de aplicacion y
 * la sesion se perderia. Un servicio en primer plano con notificacion permanente
 * es la unica forma de que el sistema respete un contador largo.
 *
 * El servicio no cuenta en memoria: cada tick recalcula contra el reloj del
 * sistema usando [Sesion.inicioMs]. Aunque el proceso muera y reviva, el tiempo
 * restante sigue siendo correcto.
 */
class ServicioEnfoque : Service() {

    private val ambito = CoroutineScope(Dispatchers.Main)
    private var tarea: Job? = null
    private lateinit var almacenEnfoque: AlmacenEnfoque

    override fun onCreate() {
        super.onCreate()
        almacenEnfoque = AlmacenEnfoque(applicationContext)
        crearCanales()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACCION_ABANDONAR) {
            terminar(Desenlace.ABANDONADA)
            return START_NOT_STICKY
        }

        val sesion = almacenEnfoque.sesionActiva()
        if (sesion == null || sesion.terminada()) {
            stopSelf()
            return START_NOT_STICKY
        }

        EstadoEnfoque.sesion = sesion
        arrancarEnPrimerPlano(sesion)
        silenciar(true)
        iniciarConteo(sesion)

        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        tarea?.cancel()
        ambito.cancel()
        super.onDestroy()
    }

    private fun iniciarConteo(sesion: Sesion) {
        tarea?.cancel()
        tarea = ambito.launch {
            while (true) {
                if (sesion.terminada()) {
                    terminar(Desenlace.COMPLETADA)
                    return@launch
                }
                EstadoEnfoque.sesion = sesion
                delay(1000)
            }
        }
    }

    private fun terminar(desenlace: Desenlace) {
        val sesion = almacenEnfoque.sesionActiva() ?: EstadoEnfoque.sesion
        tarea?.cancel()
        silenciar(false)

        if (sesion != null) {
            val minutos = if (desenlace == Desenlace.COMPLETADA) sesion.duracionMin
            else sesion.minutosCumplidos()

            almacenEnfoque.sumarMinutos(LocalDate.now(), minutos)

            if (desenlace == Desenlace.COMPLETADA) {
                registrarProgreso(sesion)
                avisarFin(sesion)
                vibrar()
            }
            EstadoEnfoque.ultimoDesenlace = sesion to desenlace
        }

        almacenEnfoque.limpiarSesion()
        EstadoEnfoque.sesion = null

        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * Anota el resultado en el habito.
     *
     * Distingue el tipo de meta, que es lo que hace que esto se sienta integrado
     * en vez de pegado con cinta:
     *
     *  - Meta de TIEMPO: suma los minutos de la sesion al progreso del dia. Una
     *    sesion de 20 min sobre una meta de 30 deja el habito en 20/30, no lo da
     *    por cumplido de golpe.
     *  - Meta de CANTIDAD o SI_NO: lo marca cumplido. Dedicarle una sesion
     *    entera a "leer" es cumplirlo.
     *
     * Releemos del almacen en vez de usar una copia en memoria porque el usuario
     * pudo tocar cosas en la app, o el widget, mientras corria la sesion.
     */
    private fun registrarProgreso(sesion: Sesion) {
        val hoy = LocalDate.now()
        val clave = hoy.toString()
        var resultado: Habito? = null

        Almacen(applicationContext).actualizarUno(sesion.habitoId) { h ->
            val registros = h.registros.toMutableMap()
            registros[clave] = if (h.meta == Meta.TIEMPO) {
                (h.progreso(hoy) + sesion.duracionMin).coerceAtMost(h.objetivoDiario())
            } else {
                h.objetivoDiario()
            }
            h.copy(registros = registros).also { resultado = it }
        }

        // La v2 ya escribe en el calendario del telefono (que Android sincroniza
        // con Google Calendar solo). Reutilizamos eso en vez de hablar con la API.
        resultado?.let { h ->
            if (h.enCalendario && h.cumplido(hoy)) {
                runCatching { Calendario.registrar(applicationContext, h, hoy) }
            }
        }

        WidgetHabitos.refrescar(applicationContext)
    }

    // -------------------------------------------------------------------------

    private fun arrancarEnPrimerPlano(sesion: Sesion) {
        val abrir = PendingIntent.getActivity(
            this,
            0,
            Intent(this, PantallaEnfoque::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE
        )

        val aviso = NotificationCompat.Builder(this, CANAL_ACTIVO)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(sesion.nombre)
            .setContentText(
                if (sesion.estricto) "Modo estricto activo" else "Sesión de enfoque en curso"
            )
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setWhen(sesion.finMs)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(abrir)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        val tipo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(this, ID_ACTIVO, aviso, tipo)
    }

    private fun avisarFin(sesion: Sesion) {
        val abrir = PendingIntent.getActivity(
            this,
            1,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE
        )
        val aviso = NotificationCompat.Builder(this, CANAL_FIN)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle("Sesión completada")
            .setContentText("${sesion.nombre} · ${sesion.duracionMin} min")
            .setAutoCancel(true)
            .setContentIntent(abrir)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        runCatching { NotificationManagerCompat.from(this).notify(ID_FIN, aviso) }
    }

    private fun crearCanales() {
        val gestor = getSystemService(NotificationManager::class.java) ?: return
        gestor.createNotificationChannel(
            NotificationChannel(
                CANAL_ACTIVO,
                "Sesión de enfoque",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Cuenta atrás mientras dura la actividad"
                setShowBadge(false)
            }
        )
        gestor.createNotificationChannel(
            NotificationChannel(
                CANAL_FIN,
                "Sesión terminada",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Aviso al completar una actividad" }
        )
    }

    /**
     * Activa "No molestar" mientras dura la sesion. Requiere que el usuario
     * haya concedido acceso a la politica de notificaciones; si no lo hizo, no
     * hacemos nada: es una mejora, no un requisito.
     */
    private fun silenciar(activar: Boolean) {
        val gestor = getSystemService(NotificationManager::class.java) ?: return
        if (!gestor.isNotificationPolicyAccessGranted) return
        runCatching {
            gestor.setInterruptionFilter(
                if (activar) NotificationManager.INTERRUPTION_FILTER_PRIORITY
                else NotificationManager.INTERRUPTION_FILTER_ALL
            )
        }
    }

    private fun vibrar() {
        val patron = longArrayOf(0, 220, 130, 220, 130, 420)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val gestor = getSystemService(VibratorManager::class.java) ?: return
            gestor.defaultVibrator.vibrate(VibrationEffect.createWaveform(patron, -1))
        } else {
            @Suppress("DEPRECATION")
            val vibrador = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
            vibrador.vibrate(VibrationEffect.createWaveform(patron, -1))
        }
    }

    companion object {
        const val CANAL_ACTIVO = "enfoque_activo"
        const val CANAL_FIN = "enfoque_fin"
        const val ID_ACTIVO = 1001
        const val ID_FIN = 1002
        const val ACCION_ABANDONAR = "com.lucas.habitos.ABANDONAR"

        /** Arranca una sesion. La guarda primero para que sobreviva a todo. */
        fun iniciar(context: Context, sesion: Sesion) {
            AlmacenEnfoque(context).guardarSesion(sesion)
            EstadoEnfoque.sesion = sesion
            val intent = Intent(context, ServicioEnfoque::class.java)
            context.startForegroundService(intent)
        }

        fun abandonar(context: Context) {
            context.startService(
                Intent(context, ServicioEnfoque::class.java).setAction(ACCION_ABANDONAR)
            )
        }
    }
}

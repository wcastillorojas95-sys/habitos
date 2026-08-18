package com.lucas.habitos

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Los avisos de cada habito.
 *
 * Hay dos clases y no se comportan igual: los previos son notificaciones
 * normales que dan un toque ("dentro de diez minutos"), y el de la hora en punto
 * es una alarma de pantalla completa, como la de un despertador. La diferencia
 * es deliberada: si todo avisara igual, o el previo molesta demasiado o el de la
 * hora se pierde entre lo demas.
 */
object Recordatorios {

    // Android congela la importancia de un canal al crearlo, asi que cambiarla
    // obliga a estrenar identificador y retirar el viejo.
    const val CANAL = "recordatorios_habitos_v2"
    const val CANAL_ALARMA = "alarma_habitos"

    const val ACCION_AVISAR = "com.lucas.habitos.AVISAR"
    const val ACCION_MARCAR = "com.lucas.habitos.MARCAR"
    const val EXTRA_ID = "habito_id"
    const val EXTRA_ANTES = "minutos_antes"

    /** Id que MainActivity lee para arrancar la sesion nada mas abrirse. */
    const val EXTRA_ENFOCAR = "habito_a_enfocar"

    /** Las antelaciones que se ofrecen. Cancelar recorre esta lista entera. */
    val OPCIONES_PREVIO = listOf(5, 10, 15, 30, 60)

    /** Lo que se pospone la alarma al pulsar "Ahora no". */
    const val MINUTOS_POSPONER = 10

    fun crearCanal(contexto: Context) {
        val gestor = contexto.getSystemService(NotificationManager::class.java) ?: return
        gestor.deleteNotificationChannel("recordatorios_habitos")

        val avisos = NotificationChannel(
            CANAL,
            "Recordatorios de hábitos",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Avisos previos y recordatorios diarios"
            enableVibration(true)
        }
        gestor.createNotificationChannel(avisos)

        val alarma = NotificationChannel(
            CANAL_ALARMA,
            "Alarma de actividad",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Suena a la hora exacta de una actividad"
            enableVibration(true)
            // Sin sonido en el canal: lo pone la pantalla de alarma, que lo repite
            // en bucle. Si sonaran los dos se pisarían.
            setSound(null, null)
        }
        gestor.createNotificationChannel(alarma)
    }

    // ------------------------------------------------------------ programar ---

    fun reprogramarTodos(contexto: Context, habitos: List<Habito>) {
        habitos.forEach { programar(contexto, it) }
    }

    fun programar(contexto: Context, habito: Habito) {
        val alarmas = contexto.getSystemService(AlarmManager::class.java) ?: return

        // Se cancela todo antes de volver a poner: si el usuario quitó un aviso
        // previo, su alarma seguiría viva y sonando cada día para siempre.
        cancelar(contexto, habito.id)
        if (!habito.recordatorio || habito.archivado) return

        programarUno(contexto, alarmas, habito, 0)
        habito.avisosPrevios.forEach { antes -> programarUno(contexto, alarmas, habito, antes) }
    }

    private fun programarUno(
        contexto: Context,
        alarmas: AlarmManager,
        habito: Habito,
        antes: Int
    ) {
        val ahora = LocalDateTime.now()
        var momento = LocalDate.now()
            .atStartOfDay()
            .plusMinutes((habito.recordatorioMinutos - antes).toLong())
        if (!momento.isAfter(ahora)) momento = momento.plusDays(1)

        val milis = momento.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        ponerAlarma(alarmas, milis, intentoAviso(contexto, habito.id, antes))
    }

    /** Vuelve a sonar dentro de un rato. Lo usa el botón "Ahora no" de la alarma. */
    fun posponer(contexto: Context, id: String) {
        val alarmas = contexto.getSystemService(AlarmManager::class.java) ?: return
        ponerAlarma(
            alarmas,
            System.currentTimeMillis() + MINUTOS_POSPONER * 60_000L,
            intentoAviso(contexto, id, 0)
        )
    }

    private fun ponerAlarma(alarmas: AlarmManager, milis: Long, intento: PendingIntent) {
        val exactas = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmas.canScheduleExactAlarms()
        } else true
        try {
            if (exactas) alarmas.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, milis, intento)
            else alarmas.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, milis, intento)
        } catch (e: SecurityException) {
            alarmas.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, milis, intento)
        }
    }

    fun cancelar(contexto: Context, id: String) {
        val alarmas = contexto.getSystemService(AlarmManager::class.java) ?: return
        (listOf(0) + OPCIONES_PREVIO).forEach { antes ->
            alarmas.cancel(intentoAviso(contexto, id, antes))
        }
    }

    private fun intentoAviso(contexto: Context, id: String, antes: Int): PendingIntent {
        val intento = Intent(contexto, ReceptorRecordatorio::class.java).apply {
            action = ACCION_AVISAR
            // El Uri hace única a cada alarma: sin él Android trataría la de las
            // 8:00 y la de "10 minutos antes" como la misma, porque los extras no
            // cuentan para comparar intents.
            data = Uri.parse("habitos://aviso/$id/$antes")
            putExtra(EXTRA_ID, id)
            putExtra(EXTRA_ANTES, antes)
        }
        return PendingIntent.getBroadcast(
            contexto,
            "$id|$antes".hashCode(),
            intento,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    // --------------------------------------------------------------- estado ---

    /** Si el sistema deja poner alarmas al minuto exacto. */
    fun alarmasExactas(contexto: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val alarmas = contexto.getSystemService(AlarmManager::class.java) ?: return false
        return alarmas.canScheduleExactAlarms()
    }

    /** Si el usuario tiene activados los avisos de la app. */
    fun avisosPermitidos(contexto: Context): Boolean =
        NotificationManagerCompat.from(contexto).areNotificationsEnabled()

    /**
     * Si la app puede tomar la pantalla entera con una alarma.
     *
     * Desde Android 14 esto se concede solo a despertadores y llamadas; el resto
     * tiene que pedirlo. Sin permiso la alarma no falla: se degrada a una
     * notificación normal, que avisa igual pero es más fácil de ignorar.
     */
    fun pantallaCompletaPermitida(contexto: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return true
        val gestor = contexto.getSystemService(NotificationManager::class.java) ?: return false
        return gestor.canUseFullScreenIntent()
    }

    // -------------------------------------------------------------- mostrar ---

    fun mostrar(contexto: Context, habito: Habito, antes: Int = 0) {
        if (antes > 0) mostrarPrevio(contexto, habito, antes) else mostrarAlarma(contexto, habito)
    }

    /** Aviso previo: informa, no interrumpe. */
    private fun mostrarPrevio(contexto: Context, habito: Habito, antes: Int) {
        val cuando = if (antes >= 60) "1 hora" else "$antes minutos"

        val aviso: Notification = NotificationCompat.Builder(contexto, CANAL)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle("Dentro de $cuando: ${habito.nombre}")
            .setContentText("A las ${horaTexto(habito.recordatorioMinutos)}. Ve terminando lo que tengas.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(abrirApp(contexto, habito))
            .addAction(R.drawable.ic_play_arrow, "Empezar ya", empezarAhora(contexto, habito))
            .build()

        lanzar(contexto, "${habito.id}|$antes".hashCode(), aviso)
    }

    /** La hora en punto: alarma de pantalla completa. */
    private fun mostrarAlarma(contexto: Context, habito: Habito) {
        val pantalla = Intent(contexto, PantallaAlarma::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ID, habito.id)
        }
        val aPantalla = PendingIntent.getActivity(
            contexto,
            habito.id.hashCode() + 3,
            pantalla,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val aviso: Notification = NotificationCompat.Builder(contexto, CANAL_ALARMA)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle("Es la hora: ${habito.nombre}")
            .setContentText(detalle(habito))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            // El "true" pide abrirse sola aunque el móvil esté en uso. Si el
            // sistema no lo permite, esto se queda como aviso normal y ya está.
            .setFullScreenIntent(aPantalla, true)
            .setContentIntent(aPantalla)
            .addAction(R.drawable.ic_play_arrow, "Empezar", empezarAhora(contexto, habito))
            .addAction(R.drawable.ic_check, "Marcar como hecho", marcarHecho(contexto, habito))
            .build()

        lanzar(contexto, habito.id.hashCode(), aviso)
    }

    private fun detalle(habito: Habito): String = if (habito.meta == Meta.SI_NO) {
        "Es momento de cumplirlo"
    } else {
        val falta = habito.objetivoDiario() - habito.progreso(LocalDate.now())
        val unidad = habito.unidad.ifBlank { if (habito.meta == Meta.TIEMPO) "min" else "veces" }
        "Te faltan $falta $unidad"
    }

    private fun abrirApp(contexto: Context, habito: Habito) = PendingIntent.getActivity(
        contexto,
        habito.id.hashCode(),
        Intent(contexto, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun empezarAhora(contexto: Context, habito: Habito) = PendingIntent.getActivity(
        contexto,
        habito.id.hashCode() + 2,
        Intent(contexto, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ENFOCAR, habito.id)
        },
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun marcarHecho(contexto: Context, habito: Habito) = PendingIntent.getBroadcast(
        contexto,
        habito.id.hashCode() + 1,
        Intent(contexto, ReceptorRecordatorio::class.java).apply {
            action = ACCION_MARCAR
            data = Uri.parse("habitos://marcar/${habito.id}")
            putExtra(EXTRA_ID, habito.id)
        },
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun lanzar(contexto: Context, id: Int, aviso: Notification) {
        try {
            NotificationManagerCompat.from(contexto).notify(id, aviso)
        } catch (e: SecurityException) {
            // Sin permiso de notificaciones no hay nada que hacer desde aquí.
        }
    }
}

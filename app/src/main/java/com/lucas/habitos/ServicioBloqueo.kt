package com.lucas.habitos

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.SystemClock
import android.provider.Settings
import android.telecom.TelecomManager
import android.view.accessibility.AccessibilityEvent

/**
 * Bloquea el resto del telefono mientras dura una sesion de enfoque.
 *
 * Android no deja que una app normal sepa que hay en pantalla, y con razon.
 * La unica via legitima es un servicio de accesibilidad: el sistema nos avisa
 * cada vez que cambia la ventana en primer plano y nosotros decidimos si
 * tapamos con [PantallaBloqueo].
 *
 * Este servicio NO lee el contenido de ninguna pantalla —canRetrieveWindowContent
 * va a false en el XML— ni tiene permiso de internet propio. Solo mira el nombre
 * del paquete que acaba de abrirse.
 *
 * Tres frenos de seguridad, porque una app que puede secuestrar el telefono tiene
 * que ser paranoica consigo misma:
 *
 *  1. Solo bloquea si hay una sesion guardada y viva. Toda sesion tiene hora de
 *     fin: aunque el proceso muera, el reloj del sistema la termina igual.
 *  2. Tope duro de cuatro horas desde el inicio. Si los datos se corrompen y
 *     apareciera una sesion eterna, el bloqueo se desactiva solo.
 *  3. Los ajustes de Android nunca se bloquean. Son la salida de emergencia:
 *     desde ahi siempre se puede apagar este servicio.
 */
class ServicioBloqueo : AccessibilityService() {

    private var almacen: AlmacenEnfoque? = null
    private var permitidos: Set<String> = emptySet()

    /** Sesion cacheada para no leer las preferencias en cada evento. */
    private var sesion: Sesion? = null
    private var leidaMs: Long = 0L
    private var ultimoBloqueoMs: Long = 0L
    private var permitidosDe: Long = -1L

    override fun onServiceConnected() {
        super.onServiceConnected()
        almacen = AlmacenEnfoque(applicationContext)
        permitidos = calcularPermitidos()
    }

    override fun onAccessibilityEvent(evento: AccessibilityEvent?) {
        if (evento?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val paquete = evento.packageName?.toString()?.takeIf { it.isNotBlank() } ?: return

        // Primero si toca bloquear —lo barato y lo que descarta el 99 % de los
        // eventos— y despues si este paquete se salva. En ese orden, porque
        // hayQueBloquear() es quien refresca la lista de permitidos.
        if (!hayQueBloquear()) return
        if (paquete in permitidos) return

        // Un cambio de ventana puede disparar varios eventos seguidos. Sin este
        // freno lanzariamos la pantalla de bloqueo cuatro veces por parpadeo.
        val ahora = SystemClock.elapsedRealtime()
        if (ahora - ultimoBloqueoMs < 500) return
        ultimoBloqueoMs = ahora

        PantallaBloqueo.mostrar(this)
    }

    override fun onInterrupt() {}

    // --------------------------------------------------------------- reglas ---

    private fun hayQueBloquear(): Boolean {
        val enfoque = almacen ?: return false
        if (!enfoque.bloquearApps) return false

        // Sin permiso de superposicion, Android no nos deja abrir una pantalla
        // desde segundo plano y el bloqueo no funcionaria. Mejor no hacer nada
        // que dejar el telefono a medio bloquear.
        if (!puedeSuperponer(this)) return false

        val actual = sesionActual(enfoque) ?: return false
        if (actual.terminada()) return false

        // El teclado o el marcador por defecto pueden haber cambiado desde que
        // arrancó el servicio. Se recalcula una vez por sesión, no por evento.
        if (permitidosDe != actual.inicioMs) {
            permitidos = calcularPermitidos()
            permitidosDe = actual.inicioMs
        }

        val corriendo = System.currentTimeMillis() - actual.inicioMs
        return corriendo in 0..TOPE_MS
    }

    private fun sesionActual(enfoque: AlmacenEnfoque): Sesion? {
        val ahora = SystemClock.elapsedRealtime()
        if (ahora - leidaMs > 1500L || sesion == null) {
            sesion = enfoque.sesionActiva()
            leidaMs = ahora
        }
        return sesion
    }

    /**
     * Lo que nunca se bloquea.
     *
     * La lista es corta a proposito: el usuario eligio bloquearlo todo. Lo que
     * queda fuera no es una concesion a la distraccion, es lo que haria
     * peligroso o irrecuperable el bloqueo.
     */
    private fun calcularPermitidos(): Set<String> {
        val lista = mutableSetOf(packageName, "com.android.systemui")

        // Ajustes de Android: la salida de emergencia.
        lista += "com.android.settings"
        resolverPaquete(Intent(Settings.ACTION_SETTINGS))?.let { lista += it }

        // El telefono. Una llamada entrante no es una distraccion que hayas
        // elegido, y podria ser urgente.
        runCatching {
            getSystemService(TelecomManager::class.java)?.defaultDialerPackage
        }.getOrNull()?.let { lista += it }

        // Y el marcador de serie aunque no sea el predeterminado, por si el
        // usuario tiene otro instalado y la llamada entra por el del sistema.
        lista += "com.android.dialer"
        lista += "com.google.android.dialer"
        lista += "com.samsung.android.dialer"
        lista += "com.android.incallui"

        // WhatsApp. Para mucha gente es el telefono, no una red social: por ahi
        // entran las llamadas y los recados de casa. Bloquearlo convierte una
        // sesion de enfoque en estar ilocalizable, que no es lo que se pedia.
        lista += APPS_DE_CONTACTO

        // El teclado, o no podrias escribir ni siquiera en nuestra app.
        runCatching {
            Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
                ?.substringBefore('/')
                ?.takeIf { it.isNotBlank() }
        }.getOrNull()?.let { lista += it }

        return lista
    }

    private fun resolverPaquete(intent: Intent): String? = runCatching {
        packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo?.packageName
    }.getOrNull()

    companion object {
        /** Cuatro horas. Ninguna sesion honesta dura tanto. */
        private const val TOPE_MS = 4L * 60L * 60L * 1000L

        /**
         * Apps de contacto que nunca se bloquean.
         *
         * WhatsApp incluye el cliente normal y el Business, porque hay quien
         * tiene solo el segundo. Si algun dia hace falta anadir Telegram o
         * Signal, es esta lista y nada mas.
         */
        private val APPS_DE_CONTACTO = setOf(
            "com.whatsapp",
            "com.whatsapp.w4b"
        )

        /** Si el usuario activo el servicio en los ajustes de accesibilidad. */
        fun activo(contexto: Context): Boolean {
            val corto = ComponentName(contexto, ServicioBloqueo::class.java).flattenToShortString()
            val largo = ComponentName(contexto, ServicioBloqueo::class.java).flattenToString()
            val activos = runCatching {
                Settings.Secure.getString(
                    contexto.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                )
            }.getOrNull().orEmpty()
            return activos.split(':').any { it.equals(corto, true) || it.equals(largo, true) }
        }

        fun puedeSuperponer(contexto: Context): Boolean =
            runCatching { Settings.canDrawOverlays(contexto) }.getOrDefault(false)

        /** Listo para bloquear: interruptor puesto y los dos permisos dados. */
        fun preparado(contexto: Context): Boolean =
            AlmacenEnfoque(contexto).bloquearApps && activo(contexto) && puedeSuperponer(contexto)

        fun abrirAccesibilidad(contexto: Context) {
            runCatching {
                contexto.startActivity(
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }

        fun abrirSuperposicion(contexto: Context) {
            runCatching {
                contexto.startActivity(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                        .setData(android.net.Uri.parse("package:${contexto.packageName}"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
    }
}

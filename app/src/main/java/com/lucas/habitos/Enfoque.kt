package com.lucas.habitos

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONObject
import java.time.LocalDate

/**
 * Una sesion de enfoque: un habito concreto durante un tiempo concreto.
 *
 * No guardamos "segundos restantes" sino el instante de inicio y la duracion.
 * Asi el reloj sigue corriendo aunque Android mate el proceso: al volver
 * recalculamos contra el reloj del sistema en vez de fiarnos de un contador en
 * memoria que se habria perdido.
 */
data class Sesion(
    val habitoId: String,
    val nombre: String,
    val icono: String,
    val colorIndice: Int,
    val duracionSeg: Int,
    val inicioMs: Long,
    val estricto: Boolean
) {

    val finMs: Long get() = inicioMs + duracionSeg * 1000L

    val duracionMin: Int get() = duracionSeg / 60

    fun restanteSeg(ahoraMs: Long = System.currentTimeMillis()): Int =
        ((finMs - ahoraMs) / 1000L).coerceAtLeast(0L).toInt()

    fun terminada(ahoraMs: Long = System.currentTimeMillis()): Boolean = ahoraMs >= finMs

    fun progreso(ahoraMs: Long = System.currentTimeMillis()): Float =
        if (duracionSeg <= 0) 1f
        else ((ahoraMs - inicioMs).toFloat() / (duracionSeg * 1000f)).coerceIn(0f, 1f)

    /** Minutos realmente transcurridos. Nunca mas que la duracion pactada. */
    fun minutosCumplidos(ahoraMs: Long = System.currentTimeMillis()): Int =
        (((ahoraMs - inicioMs) / 60_000L).coerceIn(0L, duracionMin.toLong())).toInt()

    fun aJson(): JSONObject = JSONObject().apply {
        put("habitoId", habitoId)
        put("nombre", nombre)
        put("icono", icono)
        put("colorIndice", colorIndice)
        put("duracionSeg", duracionSeg)
        put("inicioMs", inicioMs)
        put("estricto", estricto)
    }

    companion object {
        fun deJson(o: JSONObject): Sesion = Sesion(
            habitoId = o.optString("habitoId"),
            nombre = o.optString("nombre", "Actividad"),
            icono = claveDeIcono(
                if (o.has("icono")) o.optString("icono") else o.optString("emoji")
            ),
            colorIndice = o.optInt("colorIndice", 0),
            duracionSeg = o.optInt("duracionSeg", 0),
            inicioMs = o.optLong("inicioMs", 0L),
            estricto = o.optBoolean("estricto", false)
        )

        /**
         * Crea la sesion a partir del habito.
         *
         * Si el habito tiene meta de tiempo, la duracion por defecto es lo que
         * le falta hoy para cumplirla: si tu meta son 30 min y ya llevas 10, la
         * sesion propone 20. Es la respuesta que el usuario habria elegido.
         */
        fun para(habito: Habito, minutos: Int, estricto: Boolean): Sesion = Sesion(
            habitoId = habito.id,
            nombre = habito.nombre,
            icono = habito.icono,
            colorIndice = habito.color,
            duracionSeg = minutos * 60,
            inicioMs = System.currentTimeMillis(),
            estricto = estricto
        )

        fun minutosSugeridos(habito: Habito, hoy: LocalDate, porDefecto: Int): Int {
            if (habito.meta != Meta.TIEMPO) return porDefecto
            val falta = habito.objetivoDiario() - habito.progreso(hoy)
            return if (falta in 1..480) falta else porDefecto
        }
    }
}

/** Como acabo una sesion, para poder contarselo al usuario al volver. */
enum class Desenlace { COMPLETADA, ABANDONADA }

/**
 * Estado compartido entre el servicio y la interfaz.
 *
 * Servicio y actividad viven en el mismo proceso, asi que un objeto con estado
 * de Compose basta y nos ahorra montar flujos o broadcasts. Solo se escribe
 * desde el hilo principal.
 */
object EstadoEnfoque {
    var sesion by mutableStateOf<Sesion?>(null)
    var ultimoDesenlace by mutableStateOf<Pair<Sesion, Desenlace>?>(null)

    /**
     * Id del habito que hay que arrancar en cuanto la interfaz este viva.
     *
     * Lo pone MainActivity cuando la abre el boton "Empezar ahora" de una
     * notificacion. Va por aqui y no por el Intent porque la actividad ya puede
     * estar abierta y Compose necesita enterarse del cambio.
     */
    var pedidoEnfoque by mutableStateOf<String?>(null)
}

/**
 * Guarda la sesion en curso y el tiempo enfocado por dia.
 *
 * Va aparte de [Almacen] a proposito: son datos con otro ciclo de vida y no
 * queremos que un fallo aqui se lleve por delante el historial de habitos.
 */
class AlmacenEnfoque(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)

    fun sesionActiva(): Sesion? {
        val texto = prefs.getString(CLAVE_SESION, null) ?: return null
        return try {
            Sesion.deJson(JSONObject(texto))
        } catch (e: Exception) {
            limpiarSesion()
            null
        }
    }

    fun guardarSesion(sesion: Sesion) {
        prefs.edit().putString(CLAVE_SESION, sesion.aJson().toString()).apply()
    }

    fun limpiarSesion() {
        prefs.edit().remove(CLAVE_SESION).apply()
    }

    /** Suma minutos al total del dia, para las estadisticas. */
    fun sumarMinutos(dia: LocalDate, minutos: Int) {
        if (minutos <= 0) return
        val clave = CLAVE_MINUTOS + dia.toString()
        prefs.edit().putInt(clave, prefs.getInt(clave, 0) + minutos).apply()
    }

    fun minutosDe(dia: LocalDate): Int = prefs.getInt(CLAVE_MINUTOS + dia.toString(), 0)

    fun minutosUltimos(hoy: LocalDate, dias: Int): Int =
        (0 until dias).sumOf { minutosDe(hoy.minusDays(it.toLong())) }

    /** Ultima duracion elegida, para no repetir la eleccion cada vez. */
    var duracionPreferidaMin: Int
        get() = prefs.getInt(CLAVE_DURACION, 25)
        set(valor) = prefs.edit().putInt(CLAVE_DURACION, valor).apply()

    var modoEstrictoPreferido: Boolean
        get() = prefs.getBoolean(CLAVE_ESTRICTO, true)
        set(valor) = prefs.edit().putBoolean(CLAVE_ESTRICTO, valor).apply()

    /** Claro por defecto: el diseño está pensado sobre fondo durazno. */
    var temaOscuro: Boolean
        get() = prefs.getBoolean(CLAVE_TEMA, false)
        set(valor) = prefs.edit().putBoolean(CLAVE_TEMA, valor).apply()

    /**
     * PIN para abandonar una sesión estricta. Cadena vacía = sin PIN.
     *
     * No es seguridad: se guarda en claro porque no protege datos, solo añade
     * una fricción deliberada contra tu yo de dentro de diez minutos. Llamarlo
     * "contraseña" sería engañoso, y guardarlo cifrado, teatro.
     */
    var pin: String
        get() = prefs.getString(CLAVE_PIN, "") ?: ""
        set(valor) = prefs.edit().putString(CLAVE_PIN, valor.filter { it.isDigit() }.take(6)).apply()

    val tienePin: Boolean get() = pin.isNotEmpty()

    private companion object {
        const val ARCHIVO = "habitos_enfoque"
        const val CLAVE_SESION = "sesion_activa"
        const val CLAVE_MINUTOS = "min_"
        const val CLAVE_DURACION = "duracion_min"
        const val CLAVE_ESTRICTO = "modo_estricto"
        const val CLAVE_TEMA = "tema_oscuro"
        const val CLAVE_PIN = "pin_estricto"
    }
}

/** Duraciones que ofrecemos de un toque. */
val DURACIONES = listOf(5, 10, 15, 25, 45, 60)

fun formatearReloj(segundos: Int): String {
    val s = segundos.coerceAtLeast(0)
    val h = s / 3600
    val m = (s % 3600) / 60
    val seg = s % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, seg)
    else String.format("%02d:%02d", m, seg)
}

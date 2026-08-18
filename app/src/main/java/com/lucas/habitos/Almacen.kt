package com.lucas.habitos

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/**
 * Guarda los habitos en el telefono como JSON dentro de SharedPreferences.
 *
 * Entiende tambien el formato de la version 1 (una lista de fechas cumplidas)
 * y lo convierte al formato nuevo la primera vez que se abre la app.
 */
class Almacen(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)

    fun cargar(): List<Habito> {
        val texto = prefs.getString(CLAVE, null) ?: return emptyList()
        return try {
            val arreglo = JSONArray(texto)
            (0 until arreglo.length()).mapNotNull { i -> leerHabito(arreglo.getJSONObject(i)) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun guardar(lista: List<Habito>) {
        val arreglo = JSONArray()
        lista.forEach { arreglo.put(escribirHabito(it)) }
        prefs.edit().putString(CLAVE, arreglo.toString()).apply()
    }

    /** Cambia un solo habito sin tocar los demas. Lo usa el widget. */
    fun actualizarUno(id: String, cambio: (Habito) -> Habito): List<Habito> {
        val nuevos = cargar().map { if (it.id == id) cambio(it) else it }
        guardar(nuevos)
        return nuevos
    }

    var idCalendario: Long
        get() = prefs.getLong(CLAVE_CALENDARIO, -1L)
        set(valor) = prefs.edit().putLong(CLAVE_CALENDARIO, valor).apply()

    /** La cuenta del calendario elegido, solo para poder enseñarla en Ajustes. */
    var nombreCalendario: String
        get() = prefs.getString(CLAVE_CUENTA_CAL, "") ?: ""
        set(valor) = prefs.edit().putString(CLAVE_CUENTA_CAL, valor).apply()

    // ---------- copia de seguridad ----------

    /** Los hábitos tal cual se guardan, para meterlos en la copia. */
    fun comoJson(): JSONArray {
        val arreglo = JSONArray()
        cargar().forEach { arreglo.put(escribirHabito(it)) }
        return arreglo
    }

    /** Lee los hábitos de una copia. Se salta en silencio los que vengan rotos. */
    fun desdeJson(arreglo: JSONArray): List<Habito> =
        (0 until arreglo.length()).mapNotNull { i ->
            runCatching { leerHabito(arreglo.getJSONObject(i)) }.getOrNull()
        }

    // ---------- lectura ----------

    private fun leerHabito(o: JSONObject): Habito? = try {
        val registros = mutableMapOf<String, Int>()

        // Formato nuevo: objeto fecha -> cantidad
        o.optJSONObject("registros")?.let { reg ->
            val claves = reg.keys()
            while (claves.hasNext()) {
                val k = claves.next()
                registros[k] = reg.optInt(k, 0)
            }
        }

        // Formato de la version 1: lista de fechas cumplidas
        o.optJSONArray("hechos")?.let { arr ->
            for (i in 0 until arr.length()) registros[arr.getString(i)] = 1
        }

        Habito(
            id = o.optString("id", System.nanoTime().toString()),
            nombre = o.optString("nombre", "Hábito"),
            // Hasta la 2.2 aquí había un emoji. claveDeIcono lo traduce al icono
            // equivalente, así que actualizar no cambia el aspecto de nada.
            icono = claveDeIcono(
                if (o.has("icono")) o.optString("icono") else o.optString("emoji")
            ),
            color = o.optInt("color", 0),
            creado = o.optString("creado", LocalDate.now().toString()),
            categoria = o.optString("categoria", ""),
            archivado = o.optBoolean("archivado", false),
            frecuencia = leerEnum(o.optString("frecuencia"), Frecuencia.DIARIO),
            diasSemana = leerEnteros(o.optJSONArray("diasSemana"), setOf(1, 2, 3, 4, 5, 6, 7))
                .ifEmpty { setOf(1, 2, 3, 4, 5, 6, 7) },
            vecesPorSemana = o.optInt("vecesPorSemana", 3),
            cadaNDias = o.optInt("cadaNDias", 2),
            meta = leerMeta(o.optString("meta")),
            metaCantidad = o.optInt("metaCantidad", 1),
            unidad = o.optString("unidad", ""),
            recordatorio = o.optBoolean("recordatorio", false),
            recordatorioMinutos = o.optInt("recordatorioMinutos", 8 * 60),
            avisosPrevios = leerEnteros(o.optJSONArray("avisosPrevios"), emptySet()),
            enCalendario = o.optBoolean("enCalendario", false),
            eventoCalendario = o.optLong("eventoCalendario", 0L),
            registros = registros,
            descansos = leerTextos(o.optJSONArray("descansos"))
        )
    } catch (e: Exception) {
        null
    }

    private fun leerEnum(valor: String?, porDefecto: Frecuencia): Frecuencia =
        Frecuencia.entries.firstOrNull { it.name == valor } ?: porDefecto

    private fun leerMeta(valor: String?): Meta =
        Meta.entries.firstOrNull { it.name == valor } ?: Meta.SI_NO

    private fun leerEnteros(arr: JSONArray?, porDefecto: Set<Int>): Set<Int> {
        if (arr == null) return porDefecto
        val s = mutableSetOf<Int>()
        for (i in 0 until arr.length()) s.add(arr.optInt(i))
        // Una lista vacía es una respuesta válida —"ningún aviso previo"— así que
        // solo se cae al valor por defecto cuando la clave no existe.
        return s
    }

    private fun leerTextos(arr: JSONArray?): Set<String> {
        if (arr == null) return emptySet()
        val s = mutableSetOf<String>()
        for (i in 0 until arr.length()) s.add(arr.optString(i))
        return s
    }

    // ---------- escritura ----------

    private fun escribirHabito(h: Habito): JSONObject {
        val o = JSONObject()
        o.put("id", h.id)
        o.put("nombre", h.nombre)
        o.put("icono", h.icono)
        o.put("color", h.color)
        o.put("creado", h.creado)
        o.put("categoria", h.categoria)
        o.put("archivado", h.archivado)
        o.put("frecuencia", h.frecuencia.name)
        o.put("diasSemana", JSONArray(h.diasSemana.toList()))
        o.put("vecesPorSemana", h.vecesPorSemana)
        o.put("cadaNDias", h.cadaNDias)
        o.put("meta", h.meta.name)
        o.put("metaCantidad", h.metaCantidad)
        o.put("unidad", h.unidad)
        o.put("recordatorio", h.recordatorio)
        o.put("recordatorioMinutos", h.recordatorioMinutos)
        o.put("avisosPrevios", JSONArray(h.avisosPrevios.toList()))
        o.put("enCalendario", h.enCalendario)
        o.put("eventoCalendario", h.eventoCalendario)
        o.put("descansos", JSONArray(h.descansos.toList()))

        val reg = JSONObject()
        h.registros.forEach { (fecha, cantidad) -> if (cantidad > 0) reg.put(fecha, cantidad) }
        o.put("registros", reg)

        return o
    }

    private companion object {
        const val ARCHIVO = "habitos_datos"
        const val CLAVE = "lista"
        const val CLAVE_CALENDARIO = "id_calendario"
        const val CLAVE_CUENTA_CAL = "cuenta_calendario"
    }
}

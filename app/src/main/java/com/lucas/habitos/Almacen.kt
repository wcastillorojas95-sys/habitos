package com.lucas.habitos

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/**
 * Guarda y recupera los habitos del telefono.
 *
 * Usa SharedPreferences con un JSON adentro: no necesita base de datos ni
 * dependencias extra, y los datos sobreviven a cerrar y reabrir la app.
 */
class Almacen(context: Context) {

    private val prefs = context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)

    fun cargar(): List<Habito> {
        val texto = prefs.getString(CLAVE, null) ?: return emptyList()
        return try {
            val arreglo = JSONArray(texto)
            (0 until arreglo.length()).map { i ->
                val o = arreglo.getJSONObject(i)
                val fechas = o.optJSONArray("hechos") ?: JSONArray()
                Habito(
                    id = o.optString("id", System.nanoTime().toString()),
                    nombre = o.optString("nombre", "Hábito"),
                    emoji = o.optString("emoji", "✅"),
                    color = o.optInt("color", 0),
                    creado = o.optString("creado", LocalDate.now().toString()),
                    hechos = (0 until fechas.length()).map { fechas.getString(it) }.toSet()
                )
            }
        } catch (e: Exception) {
            // Si el archivo quedo corrupto preferimos empezar limpio antes que cerrar la app.
            emptyList()
        }
    }

    fun guardar(lista: List<Habito>) {
        val arreglo = JSONArray()
        lista.forEach { h ->
            val o = JSONObject()
            o.put("id", h.id)
            o.put("nombre", h.nombre)
            o.put("emoji", h.emoji)
            o.put("color", h.color)
            o.put("creado", h.creado)
            o.put("hechos", JSONArray(h.hechos.toList()))
            arreglo.put(o)
        }
        prefs.edit().putString(CLAVE, arreglo.toString()).apply()
    }

    private companion object {
        const val ARCHIVO = "habitos_datos"
        const val CLAVE = "lista"
    }
}

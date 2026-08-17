package com.lucas.habitos

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/**
 * Copia de seguridad de todo lo que la app guarda.
 *
 * Se escribe donde el usuario diga usando el selector de archivos del sistema,
 * así que no hace falta ningún permiso de almacenamiento: Android nos da el
 * archivo ya abierto.
 *
 * El formato es JSON legible a propósito. Si algún día esta app desaparece,
 * quien tenga el archivo puede abrirlo y entender qué hay dentro, que es más de
 * lo que se puede decir de la mayoría de las copias de seguridad.
 */
object Copia {

    private const val VERSION = 1

    /** Lo que se ve al terminar, para poder contárselo al usuario. */
    data class Resultado(val ok: Boolean, val mensaje: String, val habitos: List<Habito>)

    fun nombreSugerido(): String = "habitos-copia-${LocalDate.now()}.json"

    // ------------------------------------------------------------- exportar ---

    fun exportar(contexto: Context, destino: Uri): Resultado {
        return try {
            val almacen = Almacen(contexto)
            val enfoque = AlmacenEnfoque(contexto)

            val raiz = JSONObject().apply {
                put("app", "habitos")
                put("version", VERSION)
                put("exportado", LocalDate.now().toString())
                put("habitos", almacen.comoJson())
                put("enfoque", JSONObject().apply {
                    // Se construye a mano en vez de con JSONObject(Map): esa
                    // sobrecarga usa un tipo crudo de Java y desde Kotlin es
                    // fuente segura de sorpresas.
                    put("minutos", JSONObject().apply {
                        enfoque.minutosTodos().forEach { (dia, valor) -> put(dia, valor) }
                    })
                    put("duracionPreferida", enfoque.duracionPreferidaMin)
                    put("modoEstricto", enfoque.modoEstrictoPreferido)
                    put("temaOscuro", enfoque.temaOscuro)
                })
                // El PIN no se exporta a propósito: se guarda sin cifrar y esta
                // copia acaba en Drive, en el correo o en un chat. Que haya que
                // volver a ponerlo es un precio pequeño.
            }

            contexto.contentResolver.openOutputStream(destino)?.use { salida ->
                salida.write(raiz.toString(2).toByteArray())
            } ?: return Resultado(false, "No se pudo escribir en ese archivo.", emptyList())

            val cuantos = almacen.cargar().size
            Resultado(true, "Copia guardada con $cuantos ${if (cuantos == 1) "hábito" else "hábitos"}.", emptyList())
        } catch (e: Exception) {
            Resultado(false, "No se pudo guardar la copia: ${e.localizedMessage.orEmpty()}", emptyList())
        }
    }

    // ------------------------------------------------------------- importar ---

    /**
     * Lee el archivo y sustituye lo que hubiera.
     *
     * Sustituye en vez de mezclar porque mezclar obliga a decidir qué gana
     * cuando el mismo día tiene dos valores distintos, y cualquier respuesta a
     * eso sorprende a alguien. Sustituir es predecible, y por eso se avisa antes.
     */
    fun importar(contexto: Context, origen: Uri): Resultado {
        return try {
            val texto = contexto.contentResolver.openInputStream(origen)
                ?.bufferedReader()?.use { it.readText() }
                ?: return Resultado(false, "No se pudo leer el archivo.", emptyList())

            val raiz = JSONObject(texto)
            if (raiz.optString("app") != "habitos") {
                return Resultado(false, "Ese archivo no es una copia de Hábitos.", emptyList())
            }

            val almacen = Almacen(contexto)
            val leidos = almacen.desdeJson(raiz.optJSONArray("habitos") ?: JSONArray())
            if (leidos.isEmpty()) {
                return Resultado(false, "La copia no contiene ningún hábito.", emptyList())
            }

            // Los ids de evento son de otro teléfono y no valen aquí: se ponen a
            // cero para que el calendario los cree de nuevo en vez de intentar
            // actualizar eventos que no existen.
            val limpios = leidos.map { it.copy(eventoCalendario = 0L) }
            almacen.guardar(limpios)

            val enfoque = AlmacenEnfoque(contexto)
            raiz.optJSONObject("enfoque")?.let { bloque ->
                bloque.optJSONObject("minutos")?.let { minutos ->
                    val claves = minutos.keys()
                    while (claves.hasNext()) {
                        val dia = claves.next()
                        enfoque.ponerMinutos(dia, minutos.optInt(dia, 0))
                    }
                }
                enfoque.duracionPreferidaMin = bloque.optInt("duracionPreferida", 25)
                enfoque.modoEstrictoPreferido = bloque.optBoolean("modoEstricto", true)
                enfoque.temaOscuro = bloque.optBoolean("temaOscuro", false)
            }

            // Las alarmas y los eventos del calendario se rehacen: son cosas de
            // este teléfono, no viajan dentro del archivo.
            val conEventos = Calendario.sincronizarTodos(contexto, limpios)
            almacen.guardar(conEventos)
            Recordatorios.reprogramarTodos(contexto, conEventos)
            WidgetHabitos.refrescar(contexto)

            Resultado(
                true,
                "Restaurados ${conEventos.size} ${if (conEventos.size == 1) "hábito" else "hábitos"}.",
                conEventos
            )
        } catch (e: Exception) {
            Resultado(false, "El archivo está dañado o no tiene el formato esperado.", emptyList())
        }
    }
}

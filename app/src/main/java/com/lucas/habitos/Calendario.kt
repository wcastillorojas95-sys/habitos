package com.lucas.habitos

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Escribe los hábitos cumplidos en el calendario del propio teléfono.
 *
 * No usa la API de Google ni pide OAuth: como la cuenta de Google del usuario
 * ya está sincronizada en Android, lo que se escribe aquí aparece solo en
 * Google Calendar en todos sus dispositivos.
 */
object Calendario {

    fun tienePermiso(contexto: Context): Boolean =
        ContextCompat.checkSelfPermission(contexto, Manifest.permission.WRITE_CALENDAR) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(contexto, Manifest.permission.READ_CALENDAR) ==
                PackageManager.PERMISSION_GRANTED

    val permisos = arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)

    /** Busca un calendario donde se pueda escribir, prefiriendo el principal de Google. */
    fun calendarioPreferido(contexto: Context): Long? {
        if (!tienePermiso(contexto)) return null

        val guardado = Almacen(contexto).idCalendario
        if (guardado > 0) return guardado

        val proyeccion = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.ACCOUNT_TYPE,
            CalendarContract.Calendars.IS_PRIMARY,
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL
        )

        var elegido: Long? = null
        try {
            contexto.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI, proyeccion, null, null, null
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(0)
                    val tipo = cursor.getString(1) ?: ""
                    val principal = cursor.getInt(2) == 1
                    val acceso = cursor.getInt(3)
                    if (acceso < CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR) continue

                    if (elegido == null) elegido = id
                    if (tipo.contains("google", ignoreCase = true) && principal) {
                        elegido = id
                        break
                    }
                }
            }
        } catch (e: Exception) {
            return null
        }

        elegido?.let { Almacen(contexto).idCalendario = it }
        return elegido
    }

    /** Anota el hábito cumplido como evento de día completo, sin duplicar. */
    fun registrar(contexto: Context, habito: Habito, fecha: LocalDate) {
        if (!tienePermiso(contexto)) return
        val calendario = calendarioPreferido(contexto) ?: return
        val titulo = habito.nombre
        val inicio = fecha.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

        if (yaExiste(contexto, calendario, titulo, inicio)) return

        val valores = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendario)
            put(CalendarContract.Events.TITLE, titulo)
            put(CalendarContract.Events.DESCRIPTION, "Cumplido · registrado por Hábitos")
            put(CalendarContract.Events.DTSTART, inicio)
            put(CalendarContract.Events.DTEND, inicio + 86_400_000L)
            put(CalendarContract.Events.ALL_DAY, 1)
            put(CalendarContract.Events.EVENT_TIMEZONE, "UTC")
            put(CalendarContract.Events.HAS_ALARM, 0)
        }

        try {
            contexto.contentResolver.insert(CalendarContract.Events.CONTENT_URI, valores)
        } catch (e: Exception) {
            // Sin calendario disponible o permiso revocado: se ignora en silencio.
        }
    }

    /** Quita el evento si el usuario desmarca el hábito. */
    fun borrar(contexto: Context, habito: Habito, fecha: LocalDate) {
        if (!tienePermiso(contexto)) return
        val calendario = calendarioPreferido(contexto) ?: return
        val titulo = habito.nombre
        val inicio = fecha.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

        try {
            contexto.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                arrayOf(CalendarContract.Events._ID),
                "${CalendarContract.Events.CALENDAR_ID} = ? AND " +
                        "${CalendarContract.Events.TITLE} = ? AND " +
                        "${CalendarContract.Events.DTSTART} = ?",
                arrayOf(calendario.toString(), titulo, inicio.toString()),
                null
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val uri = ContentUris.withAppendedId(
                        CalendarContract.Events.CONTENT_URI, cursor.getLong(0)
                    )
                    contexto.contentResolver.delete(uri, null, null)
                }
            }
        } catch (e: Exception) {
            // sin permiso: se ignora
        }
    }

    private fun yaExiste(contexto: Context, calendario: Long, titulo: String, inicio: Long): Boolean {
        return try {
            contexto.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                arrayOf(CalendarContract.Events._ID),
                "${CalendarContract.Events.CALENDAR_ID} = ? AND " +
                        "${CalendarContract.Events.TITLE} = ? AND " +
                        "${CalendarContract.Events.DTSTART} = ?",
                arrayOf(calendario.toString(), titulo, inicio.toString()),
                null
            )?.use { it.count > 0 } ?: false
        } catch (e: Exception) {
            false
        }
    }
}

package com.lucas.habitos

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.TimeZone

/**
 * Reserva en el calendario el hueco de cada hábito.
 *
 * No usa la API de Google ni pide OAuth: escribe en el calendario del propio
 * teléfono, y como la cuenta de Google ya está sincronizada en Android, el
 * evento aparece solo en Google Calendar y en el resto de dispositivos.
 *
 * Se crea un único evento repetitivo por hábito, no uno por día. Así el
 * calendario queda limpio y cambiar la hora en la app solo tiene que tocar una
 * entrada en vez de treinta.
 */
object Calendario {

    val permisos = arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)

    fun tienePermiso(contexto: Context): Boolean =
        ContextCompat.checkSelfPermission(contexto, Manifest.permission.WRITE_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.READ_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED

    // ------------------------------------------------------ elegir calendario ---

    /**
     * Busca dónde escribir, dando prioridad a una cuenta de Google.
     *
     * El orden importa más de lo que parece: si se cuela un calendario local del
     * teléfono, todo lo que se escriba se queda ahí y no sube a Google Calendar
     * nunca, sin ningún aviso. Por eso una cuenta de Google gana siempre a
     * cualquier otra, aunque la otra aparezca antes en la lista.
     */
    fun calendarioPreferido(contexto: Context): Long? {
        if (!tienePermiso(contexto)) return null

        val almacen = Almacen(contexto)
        val guardado = almacen.idCalendario
        if (guardado > 0 && sigueExistiendo(contexto, guardado)) return guardado

        val proyeccion = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.ACCOUNT_TYPE,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.IS_PRIMARY,
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
        )

        var mejorId: Long? = null
        var mejorNombre = ""
        var mejorNota = -1

        try {
            contexto.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI, proyeccion, null, null, null
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val acceso = cursor.getInt(4)
                    if (acceso < CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR) continue

                    val tipo = cursor.getString(1).orEmpty()
                    val cuenta = cursor.getString(2).orEmpty()
                    val principal = cursor.getInt(3) == 1
                    val esGoogle = tipo.equals("com.google", ignoreCase = true)

                    val nota = when {
                        esGoogle && principal -> 3   // el calendario personal de Google
                        esGoogle -> 2                // otro calendario de Google
                        principal -> 1
                        else -> 0                    // local del teléfono: último recurso
                    }
                    if (nota > mejorNota) {
                        mejorNota = nota
                        mejorId = cursor.getLong(0)
                        mejorNombre = cuenta.ifBlank { cursor.getString(5).orEmpty() }
                    }
                }
            }
        } catch (e: Exception) {
            return null
        }

        mejorId?.let {
            almacen.idCalendario = it
            almacen.nombreCalendario = mejorNombre
        }
        return mejorId
    }

    /** El nombre de la cuenta donde se está escribiendo, para poder enseñarlo. */
    fun nombreDelCalendario(contexto: Context): String {
        calendarioPreferido(contexto)
        return Almacen(contexto).nombreCalendario
    }

    /** Olvida la elección para que se vuelva a buscar el mejor calendario. */
    fun olvidarCalendario(contexto: Context) {
        Almacen(contexto).idCalendario = -1L
        Almacen(contexto).nombreCalendario = ""
    }

    private fun sigueExistiendo(contexto: Context, id: Long): Boolean = try {
        contexto.contentResolver.query(
            ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, id),
            arrayOf(CalendarContract.Calendars._ID), null, null, null
        )?.use { it.count > 0 } ?: false
    } catch (e: Exception) {
        false
    }

    // ------------------------------------------------------------- sincronizar ---

    /**
     * Crea o actualiza el evento repetitivo del hábito.
     *
     * Devuelve el id del evento, o 0 si no se pudo. El id se guarda en el propio
     * hábito: sin él habría que reconocer el evento por el título, y renombrar el
     * hábito dejaría eventos huérfanos por el calendario.
     */
    fun sincronizar(contexto: Context, habito: Habito): Long {
        if (!habito.enCalendario || habito.archivado) {
            quitar(contexto, habito)
            return 0L
        }
        if (!tienePermiso(contexto)) return habito.eventoCalendario
        val calendario = calendarioPreferido(contexto) ?: return habito.eventoCalendario

        val arranque = primeraFecha(habito) ?: return habito.eventoCalendario
        val inicio = LocalDateTime.of(arranque, java.time.LocalTime.MIDNIGHT)
            .plusMinutes(habito.recordatorioMinutos.toLong())
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val valores = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendario)
            put(CalendarContract.Events.TITLE, habito.nombre)
            put(CalendarContract.Events.DESCRIPTION, "Hueco reservado por Hábitos")
            put(CalendarContract.Events.DTSTART, inicio)
            // En un evento repetitivo hay que dar DURATION y no DTEND: con DTEND
            // el proveedor de Android rechaza la fila sin decir por qué.
            put(CalendarContract.Events.DURATION, "PT${duracionMinutos(habito)}M")
            put(CalendarContract.Events.RRULE, repeticion(habito))
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            // El aviso lo da la propia app; poner otro aquí sería avisar dos veces.
            put(CalendarContract.Events.HAS_ALARM, 0)
        }

        return try {
            val existente = habito.eventoCalendario
            if (existente > 0 && eventoVive(contexto, existente)) {
                contexto.contentResolver.update(
                    ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, existente),
                    valores, null, null
                )
                existente
            } else {
                val uri = contexto.contentResolver
                    .insert(CalendarContract.Events.CONTENT_URI, valores)
                uri?.lastPathSegment?.toLongOrNull() ?: 0L
            }
        } catch (e: Exception) {
            0L
        }
    }

    /** Borra el evento del hábito, si lo tenía. */
    fun quitar(contexto: Context, habito: Habito) {
        val id = habito.eventoCalendario
        if (id <= 0 || !tienePermiso(contexto)) return
        try {
            contexto.contentResolver.delete(
                ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, id),
                null, null
            )
        } catch (e: Exception) {
            // Permiso revocado o evento ya borrado a mano: no hay nada que hacer.
        }
    }

    /** Rehace todos los eventos. Se usa al conceder el permiso por primera vez. */
    fun sincronizarTodos(contexto: Context, habitos: List<Habito>): List<Habito> =
        habitos.map { habito ->
            val id = sincronizar(contexto, habito)
            if (id == habito.eventoCalendario) habito else habito.copy(eventoCalendario = id)
        }

    private fun eventoVive(contexto: Context, id: Long): Boolean = try {
        contexto.contentResolver.query(
            ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, id),
            arrayOf(CalendarContract.Events._ID),
            "${CalendarContract.Events.DELETED} = 0", null, null
        )?.use { it.count > 0 } ?: false
    } catch (e: Exception) {
        false
    }

    // ------------------------------------------------------------- los detalles ---

    private fun duracionMinutos(habito: Habito): Int = when (habito.meta) {
        Meta.TIEMPO -> habito.objetivoDiario().coerceIn(5, 480)
        else -> 30
    }

    private val DIAS_RFC = listOf("MO", "TU", "WE", "TH", "FR", "SA", "SU")

    private fun repeticion(habito: Habito): String = when (habito.frecuencia) {
        Frecuencia.DIARIO -> "FREQ=DAILY"
        Frecuencia.DIAS_SEMANA ->
            "FREQ=WEEKLY;BYDAY=" + habito.diasSemana.sorted().joinToString(",") { DIAS_RFC[it - 1] }
        Frecuencia.CADA_N_DIAS -> "FREQ=DAILY;INTERVAL=${maxOf(1, habito.cadaNDias)}"
        // "N veces por semana" no fija días, así que se reserva el hueco todos los
        // días y el usuario cumple los que pueda. Es la lectura menos mala: lo
        // contrario sería inventarse unos días concretos que él no ha elegido.
        Frecuencia.VECES_SEMANA -> "FREQ=DAILY"
    }

    /** El primer día, de hoy en adelante, en que el hábito toca. */
    private fun primeraFecha(habito: Habito): LocalDate? {
        val hoy = LocalDate.now()
        return when (habito.frecuencia) {
            Frecuencia.DIARIO, Frecuencia.VECES_SEMANA -> hoy
            Frecuencia.DIAS_SEMANA -> (0L..6L)
                .map { hoy.plusDays(it) }
                .firstOrNull { habito.diasSemana.contains(it.dayOfWeek.value) }
            // Hay que respetar la cadencia que ya lleva: si toca cada 3 días desde
            // el lunes, empezar hoy porque sí desplazaría toda la serie.
            Frecuencia.CADA_N_DIAS -> {
                val n = maxOf(1, habito.cadaNDias)
                (0L until n.toLong())
                    .map { hoy.plusDays(it) }
                    .firstOrNull { habito.aplicaEn(it) } ?: hoy
            }
        }
    }
}

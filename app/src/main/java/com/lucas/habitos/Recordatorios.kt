package com.lucas.habitos

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Programa un aviso diario por habito usando AlarmManager.
 *
 * Si el sistema no concede alarmas exactas (Android 12+ las restringe) cae en
 * una alarma aproximada, que igual llega dentro de la misma franja horaria.
 */
object Recordatorios {

    const val CANAL = "recordatorios_habitos"
    const val ACCION_AVISAR = "com.lucas.habitos.AVISAR"
    const val ACCION_MARCAR = "com.lucas.habitos.MARCAR"
    const val EXTRA_ID = "habito_id"

    fun crearCanal(contexto: Context) {
        val canal = NotificationChannel(
            CANAL,
            "Recordatorios de hábitos",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        canal.description = "Avisos diarios para cumplir tus hábitos"
        val gestor = contexto.getSystemService(NotificationManager::class.java)
        gestor?.createNotificationChannel(canal)
    }

    fun reprogramarTodos(contexto: Context, habitos: List<Habito>) {
        habitos.forEach { programar(contexto, it) }
    }

    fun programar(contexto: Context, habito: Habito) {
        val alarmas = contexto.getSystemService(AlarmManager::class.java) ?: return
        val intento = intentoAviso(contexto, habito.id)

        if (!habito.recordatorio || habito.archivado) {
            alarmas.cancel(intento)
            return
        }

        val ahora = LocalDateTime.now()
        var momento = LocalDate.now()
            .atStartOfDay()
            .plusMinutes(habito.recordatorioMinutos.toLong())
        if (!momento.isAfter(ahora)) momento = momento.plusDays(1)

        val milis = momento.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val exactas = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmas.canScheduleExactAlarms()
        } else true

        try {
            if (exactas) {
                alarmas.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, milis, intento)
            } else {
                alarmas.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, milis, intento)
            }
        } catch (e: SecurityException) {
            alarmas.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, milis, intento)
        }
    }

    fun cancelar(contexto: Context, id: String) {
        val alarmas = contexto.getSystemService(AlarmManager::class.java) ?: return
        alarmas.cancel(intentoAviso(contexto, id))
    }

    fun mostrar(contexto: Context, habito: Habito) {
        val abrir = Intent(contexto, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val abrirPendiente = PendingIntent.getActivity(
            contexto,
            habito.id.hashCode(),
            abrir,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val marcar = Intent(contexto, ReceptorRecordatorio::class.java).apply {
            action = ACCION_MARCAR
            putExtra(EXTRA_ID, habito.id)
        }
        val marcarPendiente = PendingIntent.getBroadcast(
            contexto,
            habito.id.hashCode() + 1,
            marcar,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val detalle = if (habito.meta == Meta.SI_NO) {
            "Es momento de cumplirlo"
        } else {
            "Te faltan ${habito.objetivoDiario() - habito.progreso(LocalDate.now())} ${habito.unidad.ifBlank { "por hacer" }}"
        }

        val aviso: Notification = NotificationCompat.Builder(contexto, CANAL)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle("${habito.emoji}  ${habito.nombre}")
            .setContentText(detalle)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(abrirPendiente)
            .addAction(R.drawable.ic_notificacion, "Marcar como hecho", marcarPendiente)
            .build()

        try {
            NotificationManagerCompat.from(contexto).notify(habito.id.hashCode(), aviso)
        } catch (e: SecurityException) {
            // El usuario no dio permiso de notificaciones: no hay nada que hacer.
        }
    }

    private fun intentoAviso(contexto: Context, id: String): PendingIntent {
        val intento = Intent(contexto, ReceptorRecordatorio::class.java).apply {
            action = ACCION_AVISAR
            putExtra(EXTRA_ID, id)
        }
        return PendingIntent.getBroadcast(
            contexto,
            id.hashCode(),
            intento,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}

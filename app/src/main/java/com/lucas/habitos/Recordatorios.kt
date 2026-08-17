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

    // Android congela la importancia de un canal al crearlo: para que los avisos
// pasen a sonar y asomar en pantalla hay que estrenar identificador y retirar
// el viejo, o el cambio no surte efecto en quien ya tenía la app instalada.
    const val CANAL = "recordatorios_habitos_v2"
    const val ACCION_AVISAR = "com.lucas.habitos.AVISAR"
    const val ACCION_MARCAR = "com.lucas.habitos.MARCAR"
    const val EXTRA_ID = "habito_id"

    /** Id que MainActivity lee para arrancar la sesion nada mas abrirse. */
    const val EXTRA_ENFOCAR = "habito_a_enfocar"

    fun crearCanal(contexto: Context) {
        val canal = NotificationChannel(
            CANAL,
            "Recordatorios de hábitos",
            NotificationManager.IMPORTANCE_HIGH
        )
        canal.description = "Avisos diarios para cumplir tus hábitos"
        canal.enableVibration(true)
        val gestor = contexto.getSystemService(NotificationManager::class.java)
        gestor?.deleteNotificationChannel("recordatorios_habitos")
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

    /** Si el sistema deja poner alarmas al minuto exacto. */
    fun alarmasExactas(contexto: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val alarmas = contexto.getSystemService(AlarmManager::class.java) ?: return false
        return alarmas.canScheduleExactAlarms()
    }

    /** Si el usuario tiene activados los avisos de la app. */
    fun avisosPermitidos(contexto: Context): Boolean =
        NotificationManagerCompat.from(contexto).areNotificationsEnabled()

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

        // Abre la app pidiendo arrancar el cronometro de este habito. Es un
        // getActivity y no un broadcast a proposito: desde Android 10 solo se
        // permite abrir pantalla si el toque viene directo del usuario, y esto
        // lo es.
        val enfocar = Intent(contexto, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ENFOCAR, habito.id)
        }
        val enfocarPendiente = PendingIntent.getActivity(
            contexto,
            habito.id.hashCode() + 2,
            enfocar,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val detalle = if (habito.meta == Meta.SI_NO) {
            "Es momento de cumplirlo"
        } else {
            "Te faltan ${habito.objetivoDiario() - habito.progreso(LocalDate.now())} ${habito.unidad.ifBlank { "por hacer" }}"
        }

        val aviso: Notification = NotificationCompat.Builder(contexto, CANAL)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(habito.nombre)
            .setContentText(detalle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(abrirPendiente)
            .addAction(R.drawable.ic_play_arrow, "Empezar ahora", enfocarPendiente)
            .addAction(R.drawable.ic_check, "Marcar como hecho", marcarPendiente)
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

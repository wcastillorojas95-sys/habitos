package com.lucas.habitos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import java.time.LocalDate

/** Recibe la alarma del recordatorio y la acción "Marcar como hecho" de la notificación. */
class ReceptorRecordatorio : BroadcastReceiver() {

    override fun onReceive(contexto: Context, intento: Intent) {
        val id = intento.getStringExtra(Recordatorios.EXTRA_ID) ?: return
        val almacen = Almacen(contexto)
        val hoy = LocalDate.now()

        when (intento.action) {

            Recordatorios.ACCION_AVISAR -> {
                val habito = almacen.cargar().firstOrNull { it.id == id } ?: return
                val pendiente = habito.aplicaEn(hoy) &&
                        !habito.cumplido(hoy) &&
                        !habito.esDescanso(hoy) &&
                        !habito.archivado
                if (pendiente) Recordatorios.mostrar(contexto, habito)
                // Vuelve a programar el aviso de mañana.
                Recordatorios.programar(contexto, habito)
            }

            Recordatorios.ACCION_MARCAR -> {
                almacen.actualizarUno(id) { h ->
                    val registros = h.registros.toMutableMap()
                    registros[hoy.toString()] = h.objetivoDiario()
                    h.copy(registros = registros)
                }
                NotificationManagerCompat.from(contexto).cancel(id.hashCode())
                WidgetHabitos.refrescar(contexto)
            }
        }
    }
}

/** Al reiniciar el teléfono las alarmas se borran: hay que volver a ponerlas. */
class ReceptorArranque : BroadcastReceiver() {

    override fun onReceive(contexto: Context, intento: Intent) {
        if (intento.action != Intent.ACTION_BOOT_COMPLETED &&
            intento.action != "android.intent.action.QUICKBOOT_POWERON"
        ) return

        Recordatorios.crearCanal(contexto)
        Recordatorios.reprogramarTodos(contexto, Almacen(contexto).cargar())
        WidgetHabitos.refrescar(contexto)
    }
}

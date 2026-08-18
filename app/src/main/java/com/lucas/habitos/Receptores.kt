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
                val antes = intento.getIntExtra(Recordatorios.EXTRA_ANTES, 0)

                val pendiente = habito.aplicaEn(hoy) &&
                        !habito.cumplido(hoy) &&
                        !habito.esDescanso(hoy) &&
                        !habito.archivado
                if (pendiente) {
                    Recordatorios.mostrar(contexto, habito, antes)
                    // A la hora en punto la actividad arranca sola si nadie la
                    // atiende. Ver Recordatorios.SEGUNDOS_ARRANQUE.
                    if (antes == 0) Recordatorios.programarArranque(contexto, habito.id)
                }

                // Reprogramar solo desde el aviso de la hora en punto: hacerlo
                // también en cada previo recolocaría toda la serie varias veces
                // el mismo día sin necesidad.
                if (antes == 0) Recordatorios.programar(contexto, habito)
            }

            Recordatorios.ACCION_ARRANCAR -> {
                val habito = almacen.cargar().firstOrNull { it.id == id } ?: return
                val enfoque = AlmacenEnfoque(contexto)

                // Si ya la empezaste tú, o hay otra actividad en marcha, aquí no
                // hay nada que hacer: dos sesiones a la vez no existen.
                val enMarcha = enfoque.sesionActiva()
                if (enMarcha != null && !enMarcha.terminada()) return

                val procede = habito.aplicaEn(hoy) &&
                        !habito.cumplido(hoy) &&
                        !habito.esDescanso(hoy) &&
                        !habito.archivado
                if (!procede) return

                val minutos = Sesion.minutosSugeridos(habito, hoy, enfoque.duracionPreferidaMin)
                ServicioEnfoque.iniciar(
                    contexto,
                    Sesion.para(habito, minutos, enfoque.modoEstrictoPreferido)
                )

                // Y traerla al frente. Si Android no nos deja abrir una pantalla
                // desde segundo plano, la sesión corre igual y aparecerá en cuanto
                // el usuario toque el teléfono.
                runCatching {
                    contexto.startActivity(
                        Intent(contexto, PantallaEnfoque::class.java).addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        )
                    )
                }
                NotificationManagerCompat.from(contexto).cancel(id.hashCode())
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

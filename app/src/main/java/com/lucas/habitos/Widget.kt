package com.lucas.habitos

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import java.time.LocalDate

/**
 * Widget de pantalla de inicio: muestra los hábitos de hoy y permite marcarlos
 * sin abrir la app.
 *
 * Construye las filas con RemoteViews.addView en vez de una lista con adaptador:
 * son menos piezas y menos cosas que puedan fallar.
 */
class WidgetHabitos : AppWidgetProvider() {

    override fun onUpdate(
        contexto: Context,
        gestor: AppWidgetManager,
        ids: IntArray
    ) {
        ids.forEach { id -> gestor.updateAppWidget(id, construir(contexto)) }
    }

    override fun onReceive(contexto: Context, intento: Intent) {
        super.onReceive(contexto, intento)

        if (intento.action == ACCION_ALTERNAR) {
            val id = intento.getStringExtra(EXTRA_ID) ?: return
            val hoy = LocalDate.now()
            val almacen = Almacen(contexto)

            val nuevos = almacen.actualizarUno(id) { h ->
                val registros = h.registros.toMutableMap()
                if (h.cumplido(hoy)) {
                    registros.remove(hoy.toString())
                } else {
                    registros[hoy.toString()] = h.objetivoDiario()
                }
                h.copy(registros = registros)
            }

            nuevos.firstOrNull { it.id == id }?.let { h ->
                if (h.enCalendario) {
                    if (h.cumplido(hoy)) Calendario.registrar(contexto, h, hoy)
                    else Calendario.borrar(contexto, h, hoy)
                }
            }

            refrescar(contexto)
        }
    }

    companion object {

        const val ACCION_ALTERNAR = "com.lucas.habitos.WIDGET_ALTERNAR"
        const val EXTRA_ID = "habito_id"
        private const val MAX_FILAS = 5

        fun refrescar(contexto: Context) {
            val gestor = AppWidgetManager.getInstance(contexto) ?: return
            val componente = ComponentName(contexto, WidgetHabitos::class.java)
            val ids = gestor.getAppWidgetIds(componente)
            if (ids == null || ids.isEmpty()) return
            val vistas = construir(contexto)
            ids.forEach { gestor.updateAppWidget(it, vistas) }
        }

        fun construir(contexto: Context): RemoteViews {
            val vistas = RemoteViews(contexto.packageName, R.layout.widget_contenedor)
            val hoy = LocalDate.now()

            val habitos = Almacen(contexto).cargar()
                .filter { !it.archivado && it.aplicaEn(hoy) && !it.esDescanso(hoy) }

            val hechos = habitos.count { it.cumplido(hoy) }
            vistas.setTextViewText(R.id.widget_titulo, "Hoy")
            vistas.setTextViewText(
                R.id.widget_resumen,
                if (habitos.isEmpty()) "Sin hábitos para hoy" else "$hechos de ${habitos.size}"
            )

            // Abrir la app al tocar la cabecera
            val abrir = Intent(contexto, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            vistas.setOnClickPendingIntent(
                R.id.widget_cabecera,
                PendingIntent.getActivity(
                    contexto, 0, abrir,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )

            vistas.removeAllViews(R.id.widget_lista)

            habitos.take(MAX_FILAS).forEachIndexed { indice, habito ->
                val fila = RemoteViews(contexto.packageName, R.layout.widget_fila)
                val listo = habito.cumplido(hoy)

                fila.setImageViewResource(R.id.fila_icono, recursoDeIcono(habito.icono))
                fila.setTextViewText(R.id.fila_nombre, habito.nombre)
                fila.setImageViewResource(
                    R.id.fila_marca,
                    if (listo) IconoMarcado else IconoCirculo
                )

                val alternar = Intent(contexto, WidgetHabitos::class.java).apply {
                    action = ACCION_ALTERNAR
                    // El Uri hace único a cada intent: sin él Android los trata
                    // como el mismo (los extras no cuentan para la comparación).
                    data = Uri.parse("habitos://alternar/${habito.id}")
                    putExtra(EXTRA_ID, habito.id)
                }
                fila.setOnClickPendingIntent(
                    R.id.fila_raiz,
                    PendingIntent.getBroadcast(
                        contexto,
                        1000 + indice,
                        alternar,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )

                vistas.addView(R.id.widget_lista, fila)
            }

            return vistas
        }
    }
}

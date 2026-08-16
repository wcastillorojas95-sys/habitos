package com.lucas.habitos

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate

/**
 * Elegir cuanto dura la actividad y con que dureza.
 *
 * El modo estricto viene marcado por defecto: si hay que decidir activamente
 * ponerse el cinturon, casi nadie se lo pone.
 */
@Composable
fun DialogoEnfoque(
    habito: Habito,
    hoy: LocalDate,
    almacenEnfoque: AlmacenEnfoque,
    onCancelar: () -> Unit,
    onEmpezar: (Sesion) -> Unit
) {
    val sugerido = remember(habito) {
        Sesion.minutosSugeridos(habito, hoy, almacenEnfoque.duracionPreferidaMin)
    }
    var minutos by remember { mutableIntStateOf(sugerido) }
    var estricto by remember { mutableStateOf(almacenEnfoque.modoEstrictoPreferido) }
    val color = PALETA[habito.color % PALETA.size]

    // Si el habito mide tiempo, ofrecemos ademas lo que le falta para hoy.
    val opciones = remember(sugerido) {
        (DURACIONES + sugerido).distinct().sorted()
    }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("${habito.emoji}  ${habito.nombre}") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

                if (habito.meta == Meta.TIEMPO) {
                    Text(
                        text = "Hoy llevas ${habito.progreso(hoy)} de ${habito.objetivoDiario()} min.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                }

                Text("¿Cuánto tiempo?", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(10.dp))

                opciones.chunked(3).forEach { fila ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        fila.forEach { m ->
                            val elegido = m == minutos
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (elegido) color.copy(alpha = 0.18f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                    )
                                    .border(
                                        width = if (elegido) 2.dp else 0.dp,
                                        color = if (elegido) color else Color.Transparent,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { minutos = m },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$m min",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (elegido) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                        // Rellena la fila si quedó incompleta, para que no se estire.
                        repeat(3 - fila.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Modo estricto", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Fija la app en pantalla y bloquea Inicio y Recientes. " +
                                "Para salir antes hay que mantener pulsados Atrás y " +
                                "Recientes a la vez.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Switch(checked = estricto, onCheckedChange = { estricto = it })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                almacenEnfoque.duracionPreferidaMin = minutos
                almacenEnfoque.modoEstrictoPreferido = estricto
                onEmpezar(Sesion.para(habito, minutos, estricto))
            }) { Text("Empezar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}

/**
 * Encuentra la Activity detras del Context de Compose.
 *
 * Hace falta porque lanzar la pantalla de enfoque y entrar en modo Lock Task
 * exige una Activity, y `LocalContext.current` a veces entrega un ContextWrapper.
 */
fun Context.actividad(): Activity? {
    var actual: Context? = this
    while (actual is ContextWrapper) {
        if (actual is Activity) return actual
        actual = actual.baseContext
    }
    return null
}

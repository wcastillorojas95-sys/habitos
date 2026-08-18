package com.lucas.habitos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.app.TimePickerDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.util.UUID

/**
 * Crear varios hábitos de una vez.
 *
 * Nace de una fricción concreta: montar una rutina de cuatro cosas obligaba a
 * pasar cuatro veces por el editor entero. Aquí se marcan de una lista y se
 * crean todos juntos, ya configurados con su icono, su frecuencia y su meta.
 *
 * Lo que sale de aquí son hábitos normales y corrientes: no quedan atados a
 * ninguna lista ni se comportan distinto. La lista es solo el atajo de entrada.
 */
@Composable
fun PantallaLista(
    yaCreados: List<Habito>,
    onCancelar: () -> Unit,
    onCrear: (List<Habito>) -> Unit
) {
    // Se compara por nombre en minúsculas porque es lo que el usuario reconoce:
    // si ya tiene "Leer", ofrecerle crear otro "Leer" solo genera duplicados.
    val existentes = remember(yaCreados) {
        yaCreados.filter { !it.archivado }.map { it.nombre.trim().lowercase() }.toSet()
    }

    // Se guarda el hábito ya montado, no solo la marca: así el popup puede
    // ajustarle la hora o la meta antes de que exista de verdad.
    var elegidas by remember { mutableStateOf(mapOf<String, Habito>()) }
    var ajustando by remember { mutableStateOf<Pair<String, Habito>?>(null) }

    fun plantillaComoHabito(cat: Categoria, p: Plantilla) = Habito(
        id = UUID.randomUUID().toString(),
        nombre = p.nombre,
        icono = p.icono,
        color = cat.color,
        creado = LocalDate.now().toString(),
        categoria = cat.nombre,
        meta = p.meta,
        metaCantidad = p.cantidad,
        unidad = p.unidad
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 18.dp, top = 12.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .clickable { onCancelar() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(IconoAtras),
                    contentDescription = "Volver",
                    modifier = Modifier.size(21.dp)
                )
            }
            Spacer(Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Nueva lista",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Toca una para ajustarla y añadirla. Se crean todas de una vez.",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CATEGORIAS.forEach { categoria ->
                item(key = "cab-${categoria.nombre}") {
                    Text(
                        text = categoria.nombre.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 14.dp, bottom = 2.dp)
                    )
                }
                items(
                    items = categoria.actividades,
                    key = { "${categoria.nombre}|${it.nombre}" }
                ) { plantilla ->
                    val marca = "${categoria.nombre}|${plantilla.nombre}"
                    FilaElegible(
                        plantilla = plantilla,
                        habito = elegidas[marca],
                        color = PALETA[categoria.color % PALETA.size],
                        yaLoTiene = plantilla.nombre.trim().lowercase() in existentes,
                        // Toda la fila hace lo mismo, casilla incluida: abrir las
                        // opciones. Que marcar y configurar fueran dos gestos
                        // distintos según dónde tocabas era justo lo confuso.
                        onClick = {
                            ajustando = marca to (elegidas[marca]
                                ?: plantillaComoHabito(categoria, plantilla))
                        }
                    )
                }
            }
        }

        // La barra de abajo dice cuántas van: sin ese número hay que contar las
        // marcas hacia atrás para saber qué se va a crear.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Button(
                onClick = { if (elegidas.isNotEmpty()) onCrear(elegidas.values.toList()) },
                enabled = elegidas.isNotEmpty(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = when (elegidas.size) {
                        0 -> "Elige al menos una"
                        1 -> "Crear 1 hábito"
                        else -> "Crear ${elegidas.size} hábitos"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    ajustando?.let { (marca, borrador) ->
        DialogoAjustes(
            habito = borrador,
            yaEstaba = marca in elegidas,
            onCerrar = { ajustando = null },
            onQuitar = { elegidas = elegidas - marca; ajustando = null },
            onGuardar = { ajustado -> elegidas = elegidas + (marca to ajustado); ajustando = null }
        )
    }
}

/**
 * Ajustes rápidos de una actividad antes de crearla.
 *
 * Solo lo que se decide de verdad al montar una rutina: cada cuánto, cuánto, y
 * a qué hora. El resto —icono, color, unidad— ya viene de la plantilla y se
 * puede cambiar luego en el editor completo, que para eso está.
 */
@Composable
private fun DialogoAjustes(
    habito: Habito,
    yaEstaba: Boolean,
    onCerrar: () -> Unit,
    onQuitar: () -> Unit,
    onGuardar: (Habito) -> Unit
) {
    val contexto = LocalContext.current
    val acento = PALETA[habito.color % PALETA.size]

    var frecuencia by remember { mutableStateOf(habito.frecuencia) }
    var dias by remember { mutableStateOf(habito.diasSemana) }
    var cantidad by remember { mutableStateOf(habito.metaCantidad) }
    var recordatorio by remember { mutableStateOf(habito.recordatorio) }
    var minutos by remember { mutableStateOf(habito.recordatorioMinutos) }
    var enCalendario by remember { mutableStateOf(habito.enCalendario) }

    AlertDialog(
        onDismissRequest = onCerrar,
        icon = {
            Icon(
                painter = painterResource(recursoDeIcono(habito.icono)),
                contentDescription = null,
                tint = acento,
                modifier = Modifier.size(26.dp)
            )
        },
        title = { Text(habito.nombre, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

                Rotulo("¿Cada cuánto?")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChipMini("Todos los días", frecuencia == Frecuencia.DIARIO, acento) {
                        frecuencia = Frecuencia.DIARIO
                    }
                    ChipMini("Días fijos", frecuencia == Frecuencia.DIAS_SEMANA, acento) {
                        frecuencia = Frecuencia.DIAS_SEMANA
                    }
                }

                if (frecuencia == Frecuencia.DIAS_SEMANA) {
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        (1..7).forEach { d ->
                            val activo = dias.contains(d)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (activo) acento.copy(alpha = 0.20f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    // Dejar la semana entera vacía crearía un hábito
                                    // que no toca ningún día: se impide quitar el último.
                                    .clickable { dias = if (activo && dias.size > 1) dias - d else dias + d },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = LETRAS_DIA[d - 1],
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (activo) FontWeight.Bold else FontWeight.Medium,
                                    color = if (activo) acento
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (habito.meta != Meta.SI_NO) {
                    Rotulo(
                        when {
                            habito.meta == Meta.TIEMPO -> "Minutos al día"
                            habito.unidad.isBlank() -> "Veces al día"
                            else -> "${habito.unidad} al día"
                        }
                    )
                    ContadorMini(
                        valor = cantidad,
                        sufijo = if (habito.meta == Meta.TIEMPO) "min"
                        else habito.unidad.ifBlank { "veces" },
                        acento = acento,
                        paso = if (habito.meta == Meta.TIEMPO) 5 else 1,
                        maximo = if (habito.meta == Meta.TIEMPO) 480 else 1000
                    ) { cantidad = it }
                }

                Rotulo("Aviso y calendario")
                InterruptorMini(
                    texto = if (recordatorio) "Avisarme a las ${horaTexto(minutos)}" else "Sin aviso",
                    activo = recordatorio
                ) { recordatorio = it }

                if (recordatorio || enCalendario) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Cambiar hora · ${horaTexto(minutos)}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = acento,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                TimePickerDialog(
                                    contexto,
                                    { _, h, m -> minutos = h * 60 + m },
                                    minutos / 60, minutos % 60, true
                                ).show()
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))
                InterruptorMini(
                    texto = "Reservar el hueco en mi calendario",
                    activo = enCalendario
                ) { enCalendario = it }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onGuardar(
                    habito.copy(
                        frecuencia = frecuencia,
                        diasSemana = dias,
                        metaCantidad = if (habito.meta == Meta.SI_NO) 1 else cantidad,
                        recordatorio = recordatorio,
                        recordatorioMinutos = minutos,
                        enCalendario = enCalendario
                    )
                )
            }) { Text(if (yaEstaba) "Guardar" else "Añadir", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            if (yaEstaba) {
                TextButton(onClick = onQuitar) {
                    Text("Quitar", color = MaterialTheme.colorScheme.error)
                }
            } else {
                TextButton(onClick = onCerrar) { Text("Cancelar") }
            }
        }
    )
}

@Composable
private fun Rotulo(texto: String) {
    Spacer(Modifier.height(16.dp))
    Text(
        text = texto.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun ChipMini(texto: String, activo: Boolean, acento: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (activo) acento.copy(alpha = 0.18f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (activo) FontWeight.Bold else FontWeight.Medium,
            color = if (activo) acento else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ContadorMini(
    valor: Int,
    sufijo: String,
    acento: Color,
    paso: Int,
    maximo: Int,
    onCambiar: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BotonMini(IconoMenos, acento) { onCambiar((valor - paso).coerceAtLeast(1)) }
        Text(
            text = "$valor $sufijo".trim(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        BotonMini(IconoMas, acento) { onCambiar((valor + paso).coerceAtMost(maximo)) }
    }
}

@Composable
private fun BotonMini(icono: Int, acento: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(acento.copy(alpha = 0.16f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icono),
            contentDescription = null,
            tint = acento,
            modifier = Modifier.size(15.dp)
        )
    }
}

@Composable
private fun InterruptorMini(texto: String, activo: Boolean, onCambiar: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onCambiar(!activo) }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = activo, onCheckedChange = onCambiar)
    }
}

@Composable
private fun FilaElegible(
    plantilla: Plantilla,
    habito: Habito?,
    color: Color,
    yaLoTiene: Boolean,
    onClick: () -> Unit
) {
    val apagado = yaLoTiene
    val marcada = habito != null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (marcada) color.copy(alpha = 0.14f)
                else MaterialTheme.colorScheme.surface
            )
            .clickable(enabled = !apagado) { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = if (apagado) 0.08f else 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(recursoDeIcono(plantilla.icono)),
                contentDescription = null,
                tint = if (apagado) color.copy(alpha = 0.5f) else color,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(13.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = plantilla.nombre,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (apagado) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = when {
                    apagado -> "Ya lo tienes"
                    habito != null -> resumen(habito)
                    plantilla.meta == Meta.TIEMPO -> "${plantilla.cantidad} min al día"
                    plantilla.meta == Meta.CANTIDAD ->
                        "${plantilla.cantidad} ${plantilla.unidad} al día"
                    else -> "Todos los días"
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(if (marcada) color else Color.Transparent)
                .border(
                    width = 2.dp,
                    color = if (marcada) color
                    else MaterialTheme.colorScheme.outline.copy(alpha = if (apagado) 0.3f else 0.7f),
                    shape = RoundedCornerShape(9.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (marcada) {
                Icon(
                    painter = painterResource(IconoCheck),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/** Lo configurado, en una línea: "Cada día · 30 min · 07:30". */
private fun resumen(h: Habito): String {
    val partes = mutableListOf(h.descripcionFrecuencia())
    if (h.meta != Meta.SI_NO) {
        partes += "${h.objetivoDiario()} ${if (h.meta == Meta.TIEMPO) "min" else h.unidad}".trim()
    }
    if (h.recordatorio) partes += horaTexto(h.recordatorioMinutos)
    if (h.enCalendario) partes += "calendario"
    return partes.joinToString(" · ")
}

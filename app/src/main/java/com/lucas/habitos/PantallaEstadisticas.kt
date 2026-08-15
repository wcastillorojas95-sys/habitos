package com.lucas.habitos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

private const val SEMANAS = 16

@Composable
fun PantallaEstadisticas(habitos: List<Habito>, hoy: LocalDate) {
    val activos = habitos.filter { !it.archivado }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        item {
            Text(text = "Progreso", style = MaterialTheme.typography.headlineLarge)
        }

        if (activos.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 70.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "📊", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Aún no hay nada que medir",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Crea un hábito y en unos días verás aquí tus patrones.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            return@LazyColumn
        }

        item { Resumen(activos, hoy) }
        item { MapaCalor(activos, hoy) }
        item { PorDiaSemana(activos, hoy) }
        item { Spacer(Modifier.height(2.dp)) }
        item {
            Text(
                text = "HÁBITO POR HÁBITO",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        activos.forEach { habito ->
            item(key = habito.id) { FilaHabito(habito, hoy) }
        }
    }
}

@Composable
private fun Resumen(habitos: List<Habito>, hoy: LocalDate) {
    val constancia = habitos.map { it.constancia(hoy, 30) }.average().toInt()
    val mejor = habitos.maxOfOrNull { it.mejorRacha(hoy) } ?: 0
    val total = habitos.sumOf { h -> h.registros.count { it.value >= h.objetivoDiario() } }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Dato("$constancia%", "constancia\n30 días")
            Dato("$mejor", "mejor\nracha")
            Dato("$total", "días\ncumplidos")
        }
    }
}

@Composable
private fun Dato(valor: String, etiqueta: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = valor,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

/** Cuadrícula estilo GitHub: una columna por semana, una fila por día. */
@Composable
private fun MapaCalor(habitos: List<Habito>, hoy: LocalDate) {
    val primario = MaterialTheme.colorScheme.primary
    val vacio = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
    val finSemana = Habito.inicioSemana(hoy).plusDays(6)

    Tarjeta("Últimas $SEMANAS semanas") {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LETRAS_DIA.forEach { letra ->
                    Box(
                        modifier = Modifier.size(width = 12.dp, height = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letra,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (diaSemana in 0..6) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (semana in (SEMANAS - 1) downTo 0) {
                            val fecha = finSemana
                                .minusWeeks(semana.toLong())
                                .minusDays((6 - diaSemana).toLong())

                            val ratio = razonDelDia(habitos, fecha)
                            val futuro = fecha.isAfter(hoy)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        when {
                                            futuro -> Color.Transparent
                                            ratio == null -> vacio.copy(alpha = 0.03f)
                                            ratio <= 0f -> vacio
                                            else -> primario.copy(alpha = 0.25f + 0.75f * ratio)
                                        }
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

/** null = ese día no tocaba nada; si no, fracción cumplida de 0 a 1. */
private fun razonDelDia(habitos: List<Habito>, fecha: LocalDate): Float? {
    val programados = habitos.filter { it.aplicaEn(fecha) && !it.esDescanso(fecha) && !fecha.isBefore(it.fechaCreado()) }
    if (programados.isEmpty()) return null
    val hechos = programados.count { it.cumplido(fecha) }
    return hechos.toFloat() / programados.size
}

@Composable
private fun PorDiaSemana(habitos: List<Habito>, hoy: LocalDate) {
    val primario = MaterialTheme.colorScheme.primary
    val porcentajes = (1..7).map { dow ->
        var programados = 0
        var hechos = 0
        for (i in 0 until SEMANAS * 7) {
            val f = hoy.minusDays(i.toLong())
            if (f.dayOfWeek.value != dow) continue
            habitos.forEach { h ->
                if (f.isBefore(h.fechaCreado())) return@forEach
                if (!h.aplicaEn(f) || h.esDescanso(f)) return@forEach
                programados++
                if (h.cumplido(f)) hechos++
            }
        }
        if (programados == 0) -1 else hechos * 100 / programados
    }

    val mejorDia = porcentajes.withIndex().filter { it.value >= 0 }.maxByOrNull { it.value }
    val peorDia = porcentajes.withIndex().filter { it.value >= 0 }.minByOrNull { it.value }

    Tarjeta("Por día de la semana") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            porcentajes.forEachIndexed { indice, valor ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = if (valor < 0) "–" else "$valor",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((6 + (if (valor < 0) 0 else valor) * 0.62f).dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                if (valor < 0) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
                                else primario.copy(alpha = 0.35f + 0.65f * (valor / 100f))
                            )
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = LETRAS_DIA[indice],
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (mejorDia != null && peorDia != null && mejorDia.index != peorDia.index) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Tu mejor día es el ${NOMBRES_DIA[mejorDia.index]} (${mejorDia.value}%) " +
                        "y el que más se te escapa es el ${NOMBRES_DIA[peorDia.index]} (${peorDia.value}%).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FilaHabito(habito: Habito, hoy: LocalDate) {
    val color = PALETA[habito.color % PALETA.size]
    val constancia = habito.constancia(hoy, 30)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) { Text(text = habito.emoji, fontSize = 19.sp) }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habito.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "Racha ${habito.racha(hoy)} · mejor ${habito.mejorRacha(hoy)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "$constancia%",
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
        }
    }
}

@Composable
private fun Tarjeta(titulo: String, contenido: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = titulo.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            contenido()
        }
    }
}

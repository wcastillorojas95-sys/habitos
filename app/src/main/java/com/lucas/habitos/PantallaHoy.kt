package com.lucas.habitos

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@Composable
fun PantallaHoy(
    habitos: List<Habito>,
    onCambiar: (List<Habito>) -> Unit,
    onNuevo: () -> Unit,
    onEditar: (Habito) -> Unit
) {
    val contexto = LocalContext.current
    val haptica = LocalHapticFeedback.current
    val hoy = remember { LocalDate.now() }
    var diaSel by remember { mutableStateOf(hoy) }

    val activos = habitos.filter { !it.archivado }
    val delDia = activos.filter { it.aplicaEn(diaSel) }
    val completados = delDia.count { it.cumplido(diaSel) || it.esDescanso(diaSel) }
    val fraccion = if (delDia.isEmpty()) 0f else completados.toFloat() / delDia.size

    fun cambiarUno(objetivo: Habito, cambio: (Habito) -> Habito) {
        onCambiar(habitos.map { if (it.id == objetivo.id) cambio(it) else it })
    }

    fun sumar(habito: Habito, cuanto: Int) {
        haptica.performHapticFeedback(HapticFeedbackType.LongPress)
        val clave = diaSel.toString()
        val antes = habito.cumplido(diaSel)
        val nuevo = (habito.progreso(diaSel) + cuanto).coerceIn(0, habito.objetivoDiario())

        cambiarUno(habito) { h ->
            val registros = h.registros.toMutableMap()
            if (nuevo <= 0) registros.remove(clave) else registros[clave] = nuevo
            h.copy(registros = registros)
        }

        if (habito.enCalendario) {
            val ahora = nuevo >= habito.objetivoDiario()
            if (ahora && !antes) Calendario.registrar(contexto, habito, diaSel)
            if (!ahora && antes) Calendario.borrar(contexto, habito, diaSel)
        }
    }

    fun alternarDescanso(habito: Habito) {
        haptica.performHapticFeedback(HapticFeedbackType.LongPress)
        val clave = diaSel.toString()
        cambiarUno(habito) { h ->
            h.copy(
                descansos = if (clave in h.descansos) h.descansos - clave else h.descansos + clave
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {

            item {
                Column {
                    Text(
                        text = if (diaSel == hoy) "Hoy" else fechaLarga(diaSel).replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        text = if (diaSel == hoy) fechaLarga(diaSel) else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                TiraSemana(
                    diaSel = diaSel,
                    hoy = hoy,
                    habitos = activos,
                    onElegir = { diaSel = it }
                )
            }

            if (delDia.isNotEmpty()) {
                item { TarjetaProgreso(completados, delDia.size, fraccion) }
            }

            items(delDia, key = { it.id }) { habito ->
                TarjetaHabito(
                    habito = habito,
                    dia = diaSel,
                    hoy = hoy,
                    onSumar = { sumar(habito, it) },
                    onDescanso = { alternarDescanso(habito) },
                    onEditar = { onEditar(habito) }
                )
            }

            if (delDia.isEmpty()) {
                item { EstadoVacio(hayHabitos = activos.isNotEmpty()) }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onNuevo,
            icon = { Icon(IconoMas, contentDescription = null) },
            text = { Text("Nuevo hábito") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(18.dp)
        )
    }
}

@Composable
private fun TiraSemana(
    diaSel: LocalDate,
    hoy: LocalDate,
    habitos: List<Habito>,
    onElegir: (LocalDate) -> Unit
) {
    val lunes = Habito.inicioSemana(diaSel)
    val semana = (0L..6L).map { lunes.plusDays(it) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        semana.forEach { dia ->
            val elegido = dia == diaSel
            val futuro = dia.isAfter(hoy)
            val delDia = habitos.filter { it.aplicaEn(dia) }
            val todo = delDia.isNotEmpty() && delDia.all { it.cumplido(dia) || it.esDescanso(dia) }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (elegido) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                    .clickable(enabled = !futuro) { onElegir(dia) }
                    .padding(vertical = 9.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = LETRAS_DIA[dia.dayOfWeek.value - 1],
                    style = MaterialTheme.typography.labelSmall,
                    color = if (elegido) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (futuro) 0.4f else 1f
                    )
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = dia.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (elegido) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = if (futuro) 0.4f else 1f)
                )
                Spacer(Modifier.height(5.dp))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                todo && elegido -> MaterialTheme.colorScheme.onPrimary
                                todo -> MaterialTheme.colorScheme.primary
                                else -> Color.Transparent
                            }
                        )
                )
            }
        }
    }
}

@Composable
private fun TarjetaProgreso(hechos: Int, total: Int, fraccion: Float) {
    val animada by animateFloatAsState(
        targetValue = fraccion,
        animationSpec = tween(500),
        label = "progreso"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${(animada * 100).toInt()}%",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "$hechos de $total completados",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { animada },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.13f)
                )
            }
        }
    }
}

@Composable
private fun TarjetaHabito(
    habito: Habito,
    dia: LocalDate,
    hoy: LocalDate,
    onSumar: (Int) -> Unit,
    onDescanso: () -> Unit,
    onEditar: () -> Unit
) {
    val color = PALETA[habito.color % PALETA.size]
    val descanso = habito.esDescanso(dia)
    val listo = habito.cumplido(dia)
    val racha = habito.racha(hoy)
    val porCantidad = habito.meta != Meta.SI_NO

    val escala by animateFloatAsState(
        targetValue = if (listo) 1f else 0.97f,
        animationSpec = tween(200),
        label = "escala"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                descanso -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                listo -> color.copy(alpha = 0.12f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = if (descanso) 0.08f else 0.18f))
                        .clickable { onEditar() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = habito.emoji, fontSize = 22.sp)
                }

                Spacer(Modifier.width(13.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habito.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (descanso) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = when {
                            descanso -> "Día de descanso"
                            racha > 0 -> "🔥 $racha ${unidadRacha(habito, racha)}"
                            else -> habito.descripcionFrecuencia()
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.width(10.dp))

                if (!porCantidad) {
                    Box(
                        modifier = Modifier
                            .scale(escala)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (listo) color else Color.Transparent)
                            .border(
                                width = 2.dp,
                                color = if (listo) color else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                            .clickable(enabled = !descanso) { onSumar(if (listo) -1 else 1) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (listo) {
                            Icon(
                                imageVector = IconoCheck,
                                contentDescription = "Cumplido",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.width(4.dp))

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable { onDescanso() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IconoLuna,
                        contentDescription = "Día de descanso",
                        tint = if (descanso) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            if (porCantidad && !descanso) {
                Spacer(Modifier.height(14.dp))
                ControlCantidad(
                    habito = habito,
                    dia = dia,
                    color = color,
                    onSumar = onSumar
                )
            }

            if (!descanso) {
                Spacer(Modifier.height(12.dp))
                PuntosSemana(habito = habito, hoy = hoy, color = color)
            }
        }
    }
}

@Composable
private fun ControlCantidad(
    habito: Habito,
    dia: LocalDate,
    color: Color,
    onSumar: (Int) -> Unit
) {
    val progreso = habito.progreso(dia)
    val objetivo = habito.objetivoDiario()
    val paso = if (habito.meta == Meta.TIEMPO) 5 else 1
    val fraccion = (progreso.toFloat() / objetivo).coerceIn(0f, 1f)

    val animada by animateFloatAsState(
        targetValue = fraccion,
        animationSpec = tween(350),
        label = "cantidad"
    )

    Column {
        LinearProgressIndicator(
            progress = { animada },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )

        Spacer(Modifier.height(11.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            BotonRedondo(IconoMenos, "Restar", color) { onSumar(-paso) }
            Spacer(Modifier.width(12.dp))
            Text(
                text = habito.textoProgreso(dia),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            BotonRedondo(IconoMas, "Sumar", color) { onSumar(paso) }
        }
    }
}

@Composable
private fun BotonRedondo(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    descripcion: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.16f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icono,
            contentDescription = descripcion,
            tint = color,
            modifier = Modifier.size(15.dp)
        )
    }
}

@Composable
private fun PuntosSemana(habito: Habito, hoy: LocalDate, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        for (atras in 13 downTo 0) {
            val d = hoy.minusDays(atras.toLong())
            val tono = when {
                habito.esDescanso(d) -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)
                !habito.aplicaEn(d) -> Color.Transparent
                habito.cumplido(d) -> color
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(tono)
            )
        }
    }
}

private fun unidadRacha(habito: Habito, racha: Int): String =
    if (habito.frecuencia == Frecuencia.VECES_SEMANA) {
        if (racha == 1) "semana seguida" else "semanas seguidas"
    } else {
        if (racha == 1) "día seguido" else "días seguidos"
    }

@Composable
private fun EstadoVacio(hayHabitos: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp, start = 20.dp, end = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = if (hayHabitos) "🌤️" else "🌱", fontSize = 52.sp)
        Spacer(Modifier.height(14.dp))
        Text(
            text = if (hayHabitos) "Nada programado para este día" else "Todavía no tienes hábitos",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (hayHabitos) {
                "Tus hábitos tienen otra frecuencia. Disfruta el descanso."
            } else {
                "Empieza con uno solo. Es más fácil sostener uno que cinco."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

package com.lucas.habitos

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

private fun textoFecha(f: LocalDate): String =
    "${NOMBRES_DIA[f.dayOfWeek.value - 1]} ${f.dayOfMonth} de ${MESES[f.monthValue - 1]}"

/**
 * La pantalla principal, con el diseno naranja del mockup.
 *
 * Estructura: cabecera con avatar, tarjeta grande de reto diario, tira de la
 * semana en capsulas verticales, y la lista de habitos en tarjetas redondeadas.
 */
@Composable
fun PantallaHoy(
    habitos: List<Habito>,
    onCambiar: (List<Habito>) -> Unit,
    onNuevo: () -> Unit,
    onEditar: (Habito) -> Unit,
    onEnfocar: (Habito) -> Unit
) {
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
        val nuevo = (habito.progreso(diaSel) + cuanto).coerceIn(0, habito.objetivoDiario())
        cambiarUno(habito) { h ->
            val registros = h.registros.toMutableMap()
            if (nuevo <= 0) registros.remove(clave) else registros[clave] = nuevo
            h.copy(registros = registros)
        }
    }

    fun alternarDescanso(habito: Habito) {
        val clave = diaSel.toString()
        cambiarUno(habito) { h ->
            h.copy(
                descansos = if (clave in h.descansos) h.descansos - clave else h.descansos + clave
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item { Cabecera(diaSel = diaSel, hoy = hoy) }

            item {
                TarjetaReto(
                    completados = completados,
                    total = delDia.size,
                    fraccion = fraccion
                )
            }

            item {
                TiraSemana(
                    diaSel = diaSel,
                    hoy = hoy,
                    habitos = activos,
                    onElegir = { diaSel = it }
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (diaSel == hoy) "Hoy (${delDia.size})" else "Ese día (${delDia.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    BotonPildora(texto = "Nuevo", onClick = onNuevo)
                }
            }

            items(delDia, key = { it.id }) { habito ->
                TarjetaHabito(
                    habito = habito,
                    dia = diaSel,
                    hoy = hoy,
                    onSumar = { sumar(habito, it) },
                    onDescanso = { alternarDescanso(habito) },
                    onEditar = { onEditar(habito) },
                    onEnfocar = { onEnfocar(habito) }
                )
            }

            if (delDia.isEmpty()) {
                item { EstadoVacio(hayHabitos = activos.isNotEmpty(), onNuevo = onNuevo) }
            }
        }
    }
}

// -----------------------------------------------------------------------------

@Composable
private fun Cabecera(diaSel: LocalDate, hoy: LocalDate) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            indice = 0,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hola, Lucas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (diaSel == hoy) "Hoy, ${textoFecha(diaSel)}" else textoFecha(diaSel),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** La tarjeta grande del mockup: titulo enorme, progreso y el despertador. */
@Composable
private fun TarjetaReto(completados: Int, total: Int, fraccion: Float) {
    val animada by animateFloatAsState(
        targetValue = fraccion,
        animationSpec = tween(500),
        label = "reto"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box {
            DespertadorNaranja(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 6.dp)
                    .size(124.dp)
            )

            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Reto",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "diario",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (total == 0) "Sin hábitos para hoy"
                    else "$completados de $total completados",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PilaAvatares(cuantos = 3, tamano = 24.dp)
                    Spacer(Modifier.width(14.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${(animada * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                LinearProgressIndicator(
                    progress = { animada },
                    modifier = Modifier
                        .fillMaxWidth(0.62f)
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )
            }
        }
    }
}

/**
 * La semana en capsulas verticales.
 *
 * Los dias cumplidos muestran un circulo verde con el check arriba; el dia
 * elegido es una capsula naranja entera. Es lo que mas cambia la cara de la app
 * respecto a la fila de numeros anterior.
 */
@Composable
private fun TiraSemana(
    diaSel: LocalDate,
    hoy: LocalDate,
    habitos: List<Habito>,
    onElegir: (LocalDate) -> Unit
) {
    val lunes = Habito.inicioSemana(diaSel)
    val semana = (0L..6L).map { lunes.plusDays(it) }
    val verde = Color(0xFF37BE87)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            semana.forEach { dia ->
                Text(
                    text = LETRAS_DIA[dia.dayOfWeek.value - 1],
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (dia == diaSel) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = if (dia == diaSel) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(7.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            semana.forEach { dia ->
                val futuro = dia.isAfter(hoy)
                val delDia = habitos.filter { it.aplicaEn(dia) }
                val todoHecho = delDia.isNotEmpty() &&
                    delDia.all { it.cumplido(dia) || it.esDescanso(dia) }
                val elegido = dia == diaSel

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(66.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (elegido) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surface
                        )
                        .clickable(enabled = !futuro) { onElegir(dia) },
                    contentAlignment = Alignment.TopCenter
                ) {
                    if (elegido) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = dia.dayOfMonth.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .background(
                                        if (todoHecho) verde.copy(alpha = 0.18f)
                                        else Color.Transparent
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (todoHecho) {
                                    Icon(
                                        imageVector = IconoCheck,
                                        contentDescription = null,
                                        tint = verde,
                                        modifier = Modifier.size(15.dp)
                                    )
                                } else {
                                    Text(
                                        text = dia.dayOfMonth.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(
                                            alpha = if (futuro) 0.3f else 1f
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
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
    onEditar: () -> Unit,
    onEnfocar: () -> Unit
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
                descanso -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                listo -> color.copy(alpha = 0.12f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                            racha > 0 -> "🔥 $racha ${if (racha == 1) "día" else "días"} seguidos"
                            else -> habito.descripcionFrecuencia()
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.width(8.dp))

                if (dia == hoy && !descanso && !listo) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.16f))
                            .clickable { onEnfocar() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconoPlay,
                            contentDescription = "Enfocarse",
                            tint = color,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                }

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
                    Spacer(Modifier.width(4.dp))
                }

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .clickable { onDescanso() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IconoLuna,
                        contentDescription = "Día de descanso",
                        tint = if (descanso) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (porCantidad && !descanso) {
                Spacer(Modifier.height(14.dp))
                ControlCantidad(habito = habito, dia = dia, color = color, onSumar = onSumar)
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
    val animada by animateFloatAsState(
        targetValue = (progreso.toFloat() / objetivo).coerceIn(0f, 1f),
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
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun PuntosSemana(habito: Habito, hoy: LocalDate, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        for (atras in 6 downTo 0) {
            val d = hoy.minusDays(atras.toLong())
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(
                        if (habito.cumplido(d)) color
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
            )
        }
    }
}

/** Pildora oscura, como el "See All" del mockup. */
@Composable
private fun BotonPildora(texto: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.primary)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = IconoMas,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun EstadoVacio(hayHabitos: Boolean, onNuevo: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp, bottom = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DespertadorNaranja(modifier = Modifier.size(120.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (hayHabitos) "Nada programado para este día" else "Todavía no tienes hábitos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (hayHabitos) "Disfruta el descanso, o crea uno nuevo."
            else "Empieza con uno solo. Es más fácil sostener uno que cinco.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(18.dp))
        BotonPildora(texto = "Nuevo hábito", onClick = onNuevo)
    }
}

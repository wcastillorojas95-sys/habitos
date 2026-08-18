package com.lucas.habitos

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalDate

private fun textoFecha(f: LocalDate): String =
    "${NOMBRES_DIA[f.dayOfWeek.value - 1]} ${f.dayOfMonth} de ${MESES[f.monthValue - 1]}"

/**
 * La pantalla principal, con el diseno naranja del mockup.
 *
 * Estructura: cabecera con avatar, tarjeta grande de reto diario, tira de la
 * semana en capsulas verticales, y la lista de habitos en tarjetas redondeadas.
 */
// animateItem() es estable desde Compose 1.7, pero la anotación no molesta si
// no hace falta y evita que la compilación se caiga si alguna vez se baja de
// versión: sobrar una anotación es un aviso, faltarla es un error.
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PantallaHoy(
    nombre: String,
    oscuro: Boolean,
    onCambiarTema: () -> Unit,
    onCambiarIdioma: () -> Unit,
    habitos: List<Habito>,
    onCambiar: (List<Habito>) -> Unit,
    onNuevo: () -> Unit,
    onNuevaLista: () -> Unit,
    onEditar: (Habito) -> Unit,
    onBorrar: (Habito) -> Unit,
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

    /**
     * Marca o desmarca el dia entero.
     *
     * Ya no hay botones de sumar y restar: el check deja el habito cumplido del
     * todo y volver a tocarlo lo deja a cero. Para los habitos de tiempo, los
     * minutos parciales los pone el cronometro.
     */
    fun alternarCumplido(habito: Habito) {
        haptica.performHapticFeedback(HapticFeedbackType.LongPress)
        val clave = diaSel.toString()
        cambiarUno(habito) { h ->
            val registros = h.registros.toMutableMap()
            if (h.cumplido(diaSel)) registros.remove(clave)
            else registros[clave] = h.objetivoDiario()
            h.copy(registros = registros)
        }
    }

    /**
     * Deja el progreso en un número exacto.
     *
     * Lo usan las marcas de "veces al día": tocar la cuarta pone 4, y volver a
     * tocar la última la deshace. Es contar sin botones de sumar y restar, que
     * era lo que resultaba confuso.
     */
    fun fijarProgreso(habito: Habito, cuanto: Int) {
        haptica.performHapticFeedback(HapticFeedbackType.LongPress)
        val clave = diaSel.toString()
        cambiarUno(habito) { h ->
            val registros = h.registros.toMutableMap()
            val valor = cuanto.coerceIn(0, h.objetivoDiario())
            if (valor <= 0) registros.remove(clave) else registros[clave] = valor
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            item {
                Cabecera(
                    nombre = nombre,
                    diaSel = diaSel,
                    hoy = hoy,
                    oscuro = oscuro,
                    onCambiarTema = onCambiarTema,
                    onCambiarIdioma = onCambiarIdioma
                )
            }

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
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (diaSel == hoy) t("Hoy (${delDia.size})", "Today (${delDia.size})") else t("Ese día (${delDia.size})", "That day (${delDia.size})"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.weight(1f)
                    )
                    BotonSecundario(texto = t("Lista", "List"), onClick = onNuevaLista)
                    Spacer(Modifier.width(8.dp))
                    BotonPildora(texto = t("Nuevo", "New"), onClick = onNuevo)
                }
            }

            items(delDia, key = { it.id }) { habito ->
                TarjetaHabito(
                    // Al cambiar de día, o al crear y borrar hábitos, las tarjetas
                    // entran, salen y se recolocan solas en vez de dar un salto.
                    modifier = Modifier.animateItem(),
                    habito = habito,
                    dia = diaSel,
                    hoy = hoy,
                    onAlternar = { alternarCumplido(habito) },
                    onFijar = { fijarProgreso(habito, it) },
                    onDescanso = { alternarDescanso(habito) },
                    onEditar = { onEditar(habito) },
                    onBorrar = { onBorrar(habito) },
                    onEnfocar = { onEnfocar(habito) }
                )
            }

            if (delDia.isEmpty()) {
                item {
                    EstadoVacio(
                        hayHabitos = activos.isNotEmpty(),
                        onNuevo = onNuevo,
                        onNuevaLista = onNuevaLista
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------

@Composable
private fun Cabecera(
    nombre: String,
    diaSel: LocalDate,
    hoy: LocalDate,
    oscuro: Boolean,
    onCambiarTema: () -> Unit,
    onCambiarIdioma: () -> Unit
) {
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
                text = if (nombre.isBlank()) t("Hola", "Hello")
                else t("Hola, $nombre", "Hello, $nombre"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (diaSel == hoy) t("Hoy, ${textoFecha(diaSel)}", "Today, ${textoFecha(diaSel)}")
                else textoFecha(diaSel),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // El idioma lleva su etiqueta escrita (ES / EN) ademas del icono: un
        // globo terraqueo solo no dice a que idioma vas a cambiar.
        BotonCabecera(
            icono = IconoIdioma,
            etiqueta = Idioma.siguiente,
            descripcion = t("Cambiar idioma", "Change language"),
            onClick = onCambiarIdioma
        )
        Spacer(Modifier.width(8.dp))
        BotonCabecera(
            icono = if (oscuro) IconoSol else IconoLuna,
            descripcion = if (oscuro) t("Modo claro", "Light mode") else t("Modo oscuro", "Dark mode"),
            onClick = onCambiarTema
        )
    }
}

/** Los botones redondos de la esquina: idioma y tema. */
@Composable
private fun BotonCabecera(
    icono: Int,
    descripcion: String,
    etiqueta: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(horizontal = if (etiqueta == null) 10.dp else 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icono),
            contentDescription = descripcion,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        if (etiqueta != null) {
            Spacer(Modifier.width(5.dp))
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * La tarjeta grande del mockup: titulo enorme, progreso y el despertador.
 *
 * El despertador va en su propia columna de la fila, no superpuesto: antes se
 * dibujaba encima con un Box y acababa rozando el titulo y el "0 de 2".
 */
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 20.dp, top = 20.dp, bottom = 20.dp, end = 4.dp)
            ) {
                Text(
                    text = t("Reto", "Daily"),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = t("diario", "challenge"),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (total == 0) t("Sin hábitos para hoy", "No habits for today")
                    else t("$completados de $total completados", "$completados of $total done"),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PilaAvatares(cuantos = 3, tamano = 24.dp)
                    Spacer(Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${(animada * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))
                LinearProgressIndicator(
                    progress = { animada },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )
            }

            DespertadorNaranja(
                modifier = Modifier
                    .padding(top = 16.dp, end = 14.dp, start = 4.dp)
                    .size(108.dp)
            )
        }
    }
}

/**
 * La semana en capsulas verticales.
 *
 * Todos los dias centran su contenido en la capsula: el elegido, los cumplidos
 * y los pendientes. Antes los no elegidos colgaban del borde de arriba y los
 * numeros quedaban desalineados respecto al dia activo.
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
    val verde = VerdeCumplido

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            semana.forEach { dia ->
                Text(
                    text = LETRAS_DIA[dia.dayOfWeek.value - 1],
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (dia == diaSel) FontWeight.ExtraBold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = if (dia == diaSel) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

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

                val fondoDia by animateColorAsState(
                    targetValue = if (elegido) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface,
                    animationSpec = tween(260),
                    label = "diaSemana"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(66.dp)
                        .clip(RoundedCornerShape(50))
                        .background(fondoDia)
                        .clickable(enabled = !futuro) { onElegir(dia) },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        elegido -> Text(
                            text = dia.dayOfMonth.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimary
                        )

                        todoHecho -> Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(verde.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(IconoCheck),
                                contentDescription = t("Día cumplido", "Day done"),
                                tint = verde,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        else -> Text(
                            text = dia.dayOfMonth.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
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

@Composable
private fun TarjetaHabito(
    modifier: Modifier = Modifier,
    habito: Habito,
    dia: LocalDate,
    hoy: LocalDate,
    onAlternar: () -> Unit,
    onFijar: (Int) -> Unit,
    onDescanso: () -> Unit,
    onEditar: () -> Unit,
    onBorrar: () -> Unit,
    onEnfocar: () -> Unit
) {
    val color = PALETA[habito.color % PALETA.size]
    val descanso = habito.esDescanso(dia)
    val listo = habito.cumplido(dia)
    val racha = habito.racha(hoy)
    val porCantidad = habito.meta != Meta.SI_NO

    var menuAbierto by remember { mutableStateOf(false) }
    var confirmandoBorrado by remember { mutableStateOf(false) }

    val escala by animateFloatAsState(
        targetValue = if (listo) 1f else 0.97f,
        animationSpec = tween(200),
        label = "escala"
    )

    // El fondo cambia con transición: marcar un hábito tiñe la tarjeta poco a
    // poco en vez de saltar de un color a otro.
    val fondo by animateColorAsState(
        targetValue = when {
            descanso -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            listo -> color.copy(alpha = 0.12f)
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(320),
        label = "fondoTarjeta"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = fondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = if (descanso) 0.08f else 0.18f))
                        .clickable { onEditar() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(recursoDeIcono(habito.icono)),
                        contentDescription = null,
                        tint = if (descanso) color.copy(alpha = 0.55f) else color,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habito.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (descanso) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = when {
                            descanso -> t("Día de descanso", "Rest day")
                            racha > 0 -> "🔥 $racha ${if (racha == 1) "día" else "días"} seguidos"
                            else -> habito.descripcionFrecuencia()
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.width(6.dp))

                // Empezar: solo tiene sentido hoy y con el habito aun pendiente.
                if (dia == hoy && !descanso && !listo) {
                    BotonCircular(
                        icono = IconoPlay,
                        descripcion = t("Empezar ahora", "Start now"),
                        tinte = color,
                        fondo = color.copy(alpha = 0.16f),
                        onClick = onEnfocar
                    )
                    Spacer(Modifier.width(6.dp))
                }

                // El check ahora sale siempre, tambien en los habitos de tiempo o
                // cantidad: es la forma directa de dar el dia por cumplido.
                Box(
                    modifier = Modifier
                        .scale(escala)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (listo) color else Color.Transparent)
                        .border(
                            width = 2.dp,
                            color = if (listo) color else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                        .clickable(enabled = !descanso) { onAlternar() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(IconoCheck),
                        contentDescription = if (listo) t("Quitar el cumplido", "Mark as not done") else t("Marcar cumplido", "Mark as done"),
                        tint = if (listo) Color.White
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(Modifier.width(2.dp))

                Box {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .clickable { menuAbierto = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(IconoMenu),
                            contentDescription = t("Más opciones", "More options"),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuAbierto,
                        onDismissRequest = { menuAbierto = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar", fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(IconoLapiz),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = { menuAbierto = false; onEditar() }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (descanso) t("Quitar el descanso", "Remove rest day") else t("Día de descanso", "Rest day"),
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(IconoLuna),
                                    contentDescription = null,
                                    tint = if (descanso) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = { menuAbierto = false; onDescanso() }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = t("Eliminar", "Delete"),
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(IconoBasuraLinea),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = { menuAbierto = false; confirmandoBorrado = true }
                        )
                    }
                }
            }

            // Progreso de los habitos de cantidad o tiempo. Sin botones de sumar
            // y restar: los minutos los pone el cronometro y el dia se cierra con
            // el check.
            if (porCantidad && !descanso) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = habito.textoProgreso(dia),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                // Hasta doce veces caben como marcas tocables, que es como se
                // cuentan de verdad ocho vasos de agua. Por encima de ahí no hay
                // sitio ni tiene sentido ir de uno en uno: barra y listo.
                if (habito.meta == Meta.CANTIDAD && habito.objetivoDiario() in 2..12) {
                    MarcasVeces(habito = habito, dia = dia, color = color, onFijar = onFijar)
                } else {
                    BarraProgreso(habito = habito, dia = dia, color = color)
                }
            }

            if (!descanso) {
                Spacer(Modifier.height(14.dp))
                PuntosSemana(habito = habito, hoy = hoy, color = color)
            }
        }
    }

    if (confirmandoBorrado) {
        AlertDialog(
            onDismissRequest = { confirmandoBorrado = false },
            title = { Text("¿Eliminar \"${habito.nombre}\"?", fontWeight = FontWeight.Bold) },
            text = { Text("Se borrará también todo su historial. No se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmandoBorrado = false
                    onBorrar()
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmandoBorrado = false }) { Text("Cancelar") }
            }
        )
    }
}

/**
 * Una marca por vez. Las hechas van rellenas; tocar una las deja todas hasta
 * ahí, y tocar la última hecha la deshace.
 */
@Composable
private fun MarcasVeces(habito: Habito, dia: LocalDate, color: Color, onFijar: (Int) -> Unit) {
    val hechas = habito.progreso(dia)
    val total = habito.objetivoDiario()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        for (i in 1..total) {
            val llena = i <= hechas
            val relleno by animateColorAsState(
                targetValue = if (llena) color else color.copy(alpha = 0.12f),
                animationSpec = tween(220),
                label = "marca"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(30.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(relleno)
                    .clickable { onFijar(if (i == hechas) i - 1 else i) },
                contentAlignment = Alignment.Center
            ) {
                if (llena) {
                    Icon(
                        painter = painterResource(IconoCheck),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BarraProgreso(habito: Habito, dia: LocalDate, color: Color) {
    val objetivo = habito.objetivoDiario()
    val animada by animateFloatAsState(
        targetValue = (habito.progreso(dia).toFloat() / objetivo).coerceIn(0f, 1f),
        animationSpec = tween(350),
        label = "cantidad"
    )
    LinearProgressIndicator(
        progress = { animada },
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
        color = color,
        trackColor = color.copy(alpha = 0.15f)
    )
}

@Composable
private fun BotonCircular(
    icono: Int,
    descripcion: String,
    tinte: Color,
    fondo: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(fondo)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icono),
            contentDescription = descripcion,
            tint = tinte,
            modifier = Modifier.size(17.dp)
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
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(IconoMas),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(15.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun EstadoVacio(hayHabitos: Boolean, onNuevo: () -> Unit, onNuevaLista: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp, bottom = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DespertadorNaranja(modifier = Modifier.size(120.dp))
        Spacer(Modifier.height(26.dp))
        Text(
            text = if (hayHabitos) t("Nada programado para este día", "Nothing scheduled for this day") else t("Todavía no tienes hábitos", "You have no habits yet"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (hayHabitos) t("Disfruta el descanso, o crea uno nuevo.", "Enjoy the break, or create a new one.")
            else t("Empieza con uno solo. Es más fácil sostener uno que cinco.", "Start with just one. Keeping one going is easier than five."),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            BotonSecundario(texto = t("Elegir de una lista", "Pick from a list"), onClick = onNuevaLista)
            Spacer(Modifier.width(8.dp))
            BotonPildora(texto = t("Nuevo hábito", "New habit"), onClick = onNuevo)
        }
    }
}

/** Píldora clara, para la acción que acompaña a la principal sin competir con ella. */
@Composable
private fun BotonSecundario(texto: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                shape = RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

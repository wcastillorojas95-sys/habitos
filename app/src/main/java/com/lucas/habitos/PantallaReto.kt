package com.lucas.habitos

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private enum class Fase { ELEGIR, LECTURA, PREGUNTAS }

/**
 * El reto para abandonar una actividad antes de tiempo.
 *
 * Tres pasos: eliges categoria, lees una capsula de divulgacion y contestas tres
 * preguntas seguidas sobre detalles del texto. Fallar una manda a una capsula
 * nueva desde el principio, con su tiempo de lectura otra vez.
 *
 * No se puede volver al texto durante las preguntas. Con la vuelta atras esto
 * seria una busqueda —localizar el dato y copiarlo— en vez de una lectura, y lo
 * que queremos es que hayas atendido, no que sepas usar Ctrl+F con los ojos.
 *
 * Salir del reto y seguir con la actividad es un solo toque, siempre. La
 * friccion tiene que estar en rendirse, no en volver.
 */
@Composable
fun PantallaReto(
    color: Color,
    almacenEnfoque: AlmacenEnfoque,
    onSuperado: () -> Unit,
    onVolver: () -> Unit
) {
    val categorias = remember { Reto.categoriasAlAzar(3) }
    val vistas = remember { almacenEnfoque.capsulasVistas }

    var fase by remember { mutableStateOf(Fase.ELEGIR) }
    var categoria by remember { mutableStateOf(categorias.first()) }
    var capsula by remember { mutableStateOf<Capsula?>(null) }
    var indice by remember { mutableIntStateOf(0) }
    var restante by remember { mutableIntStateOf(0) }
    var fallada by remember { mutableStateOf(false) }
    var ronda by remember { mutableIntStateOf(0) }

    val actual = capsula

    LaunchedEffect(ronda, fase) {
        if (fase != Fase.LECTURA || actual == null) return@LaunchedEffect
        restante = Reto.segundosDeLectura(actual)
        while (restante > 0) {
            delay(1000)
            restante--
        }
    }

    fun empezarCon(cat: TemaReto, anterior: String?) {
        categoria = cat
        capsula = Reto.siguiente(cat, vistas, anterior)
        indice = 0
        ronda++
        fase = Fase.LECTURA
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 22.dp)) {

            CabeceraReto(
                color = color,
                titulo = when (fase) {
                    Fase.ELEGIR -> "Elige un tema"
                    Fase.LECTURA -> "Lee esto entero"
                    Fase.PREGUNTAS -> "Pregunta ${indice + 1} de ${Reto.PREGUNTAS_POR_CAPSULA}"
                },
                onCerrar = onVolver
            )

            if (fallada) {
                Text(
                    text = "Esa no era. Vuelta a empezar con otro texto.",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            AnimatedContent(
                targetState = fase,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "fase",
                modifier = Modifier.weight(1f)
            ) { paso ->
                when (paso) {

                    Fase.ELEGIR -> Column(
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    ) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Para dejar la actividad tienes que leer algo y acertar " +
                                "tres preguntas sobre ello. Elige de qué quieres que vaya.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(20.dp))
                        categorias.forEach { cat ->
                            TarjetaTema(
                                categoria = cat,
                                quedan = Reto.frescasEn(cat, vistas),
                                color = color,
                                onClick = { fallada = false; empezarCon(cat, null) }
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                        Spacer(Modifier.height(20.dp))
                    }

                    Fase.LECTURA -> {
                        if (actual == null) Box(Modifier.fillMaxSize())
                        else Column(
                            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = actual.gancho,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = actual.texto,
                                style = MaterialTheme.typography.bodyLarge,
                                lineHeight = 27.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = actual.fuente,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(24.dp))
                        }
                    }

                    Fase.PREGUNTAS -> {
                        if (actual == null) Box(Modifier.fillMaxSize())
                        else {
                            val pregunta = actual.preguntas[indice]
                            // La correcta es siempre la 0 en los datos; aquí se
                            // barajan las posiciones, una vez por pregunta.
                            val orden = remember(actual.id, indice, ronda) {
                                pregunta.opciones.indices.shuffled()
                            }
                            Column(
                                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = pregunta.enunciado,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(Modifier.height(18.dp))
                                orden.forEach { i ->
                                    Opcion(
                                        texto = pregunta.opciones[i],
                                        color = color,
                                        onClick = {
                                            if (i == 0) {
                                                if (indice + 1 >= actual.preguntas.size) {
                                                    onSuperado()
                                                } else {
                                                    indice++
                                                }
                                            } else {
                                                fallada = true
                                                empezarCon(categoria, actual.id)
                                            }
                                        }
                                    )
                                    Spacer(Modifier.height(10.dp))
                                }
                                Spacer(Modifier.height(20.dp))
                            }
                        }
                    }
                }
            }

            if (fase == Fase.LECTURA && actual != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (restante > 0) MaterialTheme.colorScheme.surfaceVariant else color
                        )
                        .clickable(enabled = restante <= 0) {
                            almacenEnfoque.anotarVista(actual.id)
                            fallada = false
                            fase = Fase.PREGUNTAS
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (restante > 0) "Léelo con calma · $restante s"
                        else "Ya lo he leído",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (restante > 0) MaterialTheme.colorScheme.onSurfaceVariant
                        else Color.White
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Después vienen tres preguntas sobre detalles del texto, y no " +
                        "podrás volver a leerlo.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
private fun CabeceraReto(color: Color, titulo: String, onCerrar: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "Para abandonar",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onCerrar() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(IconoCerrar),
                contentDescription = "Volver a la actividad",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun TarjetaTema(
    categoria: TemaReto,
    quedan: Int,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(categoria.icono),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = categoria.etiqueta,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (quedan > 0) "$quedan sin leer" else "ya los has leído todos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun Opcion(texto: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.45f))
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

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

/**
 * El reto para abandonar una actividad antes de tiempo.
 *
 * Dos pasos: leer una fabula corta y despues contestar una pregunta sobre lo
 * que acabas de leer. La pregunta es de detalle, no de moraleja, asi que no se
 * acierta sin haber leido.
 *
 * El boton de continuar nace bloqueado con una cuenta atras proporcional a la
 * longitud del texto. Sin eso, cualquiera daria a continuar al instante y
 * jugaria a adivinar entre cuatro opciones: un reto que se supera con suerte no
 * es un reto, es un dado.
 *
 * Salir del reto —volver a la actividad— es un solo toque. La friccion tiene
 * que estar en rendirse, no en seguir.
 */
@Composable
fun PantallaReto(
    color: Color,
    onSuperado: () -> Unit,
    onVolver: () -> Unit
) {
    var fabula by remember { mutableStateOf(Reto.siguiente(null)) }
    var leyendo by remember { mutableStateOf(true) }
    var restante by remember { mutableIntStateOf(Reto.segundosDeLectura(fabula)) }
    var fallo by remember { mutableStateOf(false) }
    var intentos by remember { mutableIntStateOf(0) }

    // Las opciones se barajan una vez por fabula. Si se barajaran en cada
    // recomposicion, bailarian bajo el dedo del usuario.
    val orden = remember(fabula.titulo, intentos) { fabula.opciones.indices.shuffled() }

    LaunchedEffect(fabula.titulo, intentos) {
        restante = Reto.segundosDeLectura(fabula)
        while (restante > 0) {
            delay(1000)
            restante--
        }
    }

    fun otraFabula() {
        fabula = Reto.siguiente(fabula.titulo)
        intentos++
        leyendo = true
        fallo = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 22.dp)) {

            // ---------------------------------------------------- cabecera ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 6.dp),
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
                        text = if (leyendo) "Lee esto entero" else "Ahora contesta",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onVolver() },
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

            if (fallo) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "No era esa. Ahí va otra fábula distinta.",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(12.dp))

            AnimatedContent(
                targetState = leyendo,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "reto",
                modifier = Modifier.weight(1f)
            ) { enLectura ->
                if (enLectura) {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        Text(
                            text = fabula.titulo,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = fabula.texto,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 27.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(24.dp))
                    }
                } else {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        Text(
                            text = fabula.pregunta,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(16.dp))
                        orden.forEach { indice ->
                            Opcion(
                                texto = fabula.opciones[indice],
                                color = color,
                                onClick = {
                                    if (indice == fabula.correcta) onSuperado() else otraFabula()
                                }
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            // ------------------------------------------------------ el pie ---
            if (leyendo) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (restante > 0) MaterialTheme.colorScheme.surfaceVariant else color)
                        .clickable(enabled = restante <= 0) { leyendo = false },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (restante > 0) "Léela con calma · $restante s"
                        else "Ya la he leído",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (restante > 0) MaterialTheme.colorScheme.onSurfaceVariant
                        else Color.White
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Después te preguntaré por un detalle del texto.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { leyendo = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Volver a leerla",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
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
            Spacer(Modifier.size(14.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

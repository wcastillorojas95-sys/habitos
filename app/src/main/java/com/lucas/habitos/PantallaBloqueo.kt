package com.lucas.habitos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Lo que ves si abres otra app durante una sesion.
 *
 * No tiene botones. Es deliberado: elegiste que no hubiera escapatoria, y un
 * boton de "salir" convertiria el bloqueo en un recordatorio con un paso de mas.
 * La pantalla se cierra sola cuando el cronometro llega a cero.
 *
 * Los ajustes de Android siguen accesibles —desde ahi puedes apagar el servicio
 * de accesibilidad— porque una app que puede secuestrar el telefono debe dejar
 * siempre una puerta abierta que no dependa de que su propio codigo funcione.
 */
class PantallaBloqueo : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // El boton atras no cierra: seria una salida de un toque.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {}
        })

        val enfoque = AlmacenEnfoque(this)
        val sesion = enfoque.sesionActiva()

        if (sesion == null || sesion.terminada()) {
            finish()
            return
        }

        setContent {
            HabitosTheme(oscuro = enfoque.temaOscuro) {
                CaraDeBloqueo(
                    sesion = sesion,
                    onTerminar = { finish() },
                    // Tocar la pantalla lleva a la actividad, que es donde vive
                    // el reto. El bloqueo no es la pared: la pared es el reto.
                    // Sin esta salida esto sería un callejón sin puerta.
                    onTocar = { abrirEnfoque(); finish() }
                )
            }
        }
    }

    /**
     * Si la sesion acabo mientras la pantalla estaba parada, no tiene sentido
     * seguir tapando nada.
     */
    override fun onResume() {
        super.onResume()
        val sesion = AlmacenEnfoque(this).sesionActiva()
        if (sesion == null || sesion.terminada()) finish()
    }

    companion object {
        fun mostrar(contexto: Context) {
            runCatching {
                contexto.startActivity(
                    Intent(contexto, PantallaBloqueo::class.java).addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_NO_ANIMATION
                    )
                )
            }
        }
    }
}

@Composable
private fun CaraDeBloqueo(sesion: Sesion, onTerminar: () -> Unit, onTocar: () -> Unit) {
    val color = PALETA[sesion.colorIndice % PALETA.size]

    var restante by remember { mutableIntStateOf(sesion.restanteSeg()) }
    var avance by remember { mutableIntStateOf((sesion.progreso() * 1000).toInt()) }

    LaunchedEffect(sesion.inicioMs) {
        while (true) {
            restante = sesion.restanteSeg()
            avance = (sesion.progreso() * 1000).toInt()
            if (restante <= 0) {
                onTerminar()
                return@LaunchedEffect
            }
            delay(500)
        }
    }

    // Un latido muy lento en el icono. Sin el, la pantalla parece congelada y
    // da la sensacion de que el telefono se ha colgado.
    val latido = rememberInfiniteTransition(label = "latido")
    val opacidad by latido.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "opacidad"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable { onTocar() }
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.16f))
                    .alpha(opacidad),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(recursoDeIcono(sesion.icono)),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(Modifier.height(26.dp))
            Text(
                text = t("Estás en", "You are on"),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = sesion.nombre,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(28.dp))
            Text(
                text = formatearReloj(restante),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )

            Spacer(Modifier.height(20.dp))
            LinearProgressIndicator(
                progress = { avance / 1000f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(Modifier.height(30.dp))
            Text(
                text = t("El resto del teléfono está bloqueado hasta que termine. Se desbloquea solo, no tienes que hacer nada.", "The rest of the phone is blocked until this ends. It unlocks itself, you do not have to do anything."),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

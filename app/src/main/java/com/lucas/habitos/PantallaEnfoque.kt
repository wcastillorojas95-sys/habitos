package com.lucas.habitos

import android.app.Activity
import android.app.KeyguardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * La pantalla que ocupa el telefono mientras dura una actividad.
 *
 * Sobre el bloqueo, sin adornos: Android no permite que una app impida
 * desbloquear el telefono. Lo maximo alcanzable sin convertir el dispositivo en
 * uno corporativo (device owner, que exige borrado de fabrica y provision por
 * ADB) es el modo Lock Task, tambien llamado "fijar pantalla": deja la app
 * clavada, inutiliza Inicio y Recientes, y obliga a mantener pulsados Atras +
 * Recientes a la vez para salir. No es una carcel, pero rompe el automatismo de
 * salirse sin pensar, que es lo que hace fallar los habitos.
 *
 * Encima de eso: la pantalla se muestra sobre el lockscreen como un despertador,
 * se mantiene encendida, y rendirse exige tres segundos de pulsacion sostenida.
 */
class PantallaEnfoque : ComponentActivity() {

    private lateinit var almacenEnfoque: AlmacenEnfoque
    private var fijada = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        almacenEnfoque = AlmacenEnfoque(applicationContext)

        mostrarSobreBloqueo()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Atras no debe sacar de la sesion mientras el modo estricto este activo.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (EstadoEnfoque.sesion?.estricto != true) salir()
            }
        })

        setContent {
            HabitosTheme {
                val sesion = EstadoEnfoque.sesion
                if (sesion == null) {
                    PantallaFin(onCerrar = { salir() })
                } else {
                    CuentaAtras(
                        sesion = sesion,
                        onRendirse = {
                            soltarFijado()
                            ServicioEnfoque.abandonar(this)
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val sesion = EstadoEnfoque.sesion ?: almacenEnfoque.sesionActiva()
        if (sesion != null && !sesion.terminada() && sesion.estricto) fijar()
    }

    /**
     * Entra en modo Lock Task. Sin device owner el sistema pide confirmacion la
     * primera vez y permite salir con el gesto Atras + Recientes; con device
     * owner el bloqueo es total. El mismo codigo cubre los dos casos.
     */
    private fun fijar() {
        if (fijada) return
        fijada = runCatching { startLockTask() }.isSuccess
    }

    private fun soltarFijado() {
        if (!fijada) return
        runCatching { stopLockTask() }
        fijada = false
    }

    private fun salir() {
        soltarFijado()
        finish()
    }

    private fun mostrarSobreBloqueo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            getSystemService(KeyguardManager::class.java)?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    // -------------------------------------------------------------------------

    @Composable
    private fun CuentaAtras(sesion: Sesion, onRendirse: () -> Unit) {
        val color = PALETA[sesion.colorIndice % PALETA.size]
        var restante by remember { mutableIntStateOf(sesion.restanteSeg()) }
        var avance by remember { mutableFloatStateOf(sesion.progreso()) }

        LaunchedEffect(sesion.inicioMs) {
            while (true) {
                val ahora = System.currentTimeMillis()
                restante = sesion.restanteSeg(ahora)
                avance = sesion.progreso(ahora)
                if (restante <= 0) break
                delay(250)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(28.dp)
            ) {
                Text(text = sesion.emoji, fontSize = 44.sp)
                Spacer(Modifier.height(10.dp))
                Text(
                    text = sesion.nombre,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(30.dp))

                Box(contentAlignment = Alignment.Center) {
                    Anillo(avance = avance, color = color)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatearReloj(restante),
                            fontSize = 50.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "de ${sesion.duracionMin} min",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(30.dp))

                Text(
                    text = if (sesion.estricto) {
                        "Modo estricto. Para salir antes de tiempo mantén pulsados " +
                            "Atrás y Recientes a la vez."
                    } else {
                        "Puedes salir cuando quieras, pero solo cuenta si terminas."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(20.dp))
                BotonRendirse(color = color, onConfirmar = onRendirse)
            }
        }
    }

    @Composable
    private fun Anillo(avance: Float, color: Color) {
        val suave by animateFloatAsState(
            targetValue = avance,
            animationSpec = tween(400),
            label = "avance"
        )
        val pista = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)

        Canvas(modifier = Modifier.size(248.dp)) {
            val grosor = 18.dp.toPx()
            val lado = size.minDimension - grosor
            val esquina = Offset((size.width - lado) / 2f, (size.height - lado) / 2f)
            drawArc(
                color = pista,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = esquina,
                size = Size(lado, lado),
                style = Stroke(width = grosor, cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * suave.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = esquina,
                size = Size(lado, lado),
                style = Stroke(width = grosor, cap = StrokeCap.Round)
            )
        }
    }

    /**
     * Rendirse cuesta tres segundos de pulsacion sostenida.
     *
     * La friccion es deliberada: un toque accidental no debe tirar la sesion, y
     * el par de segundos de duda suele bastar para no abandonar.
     */
    @Composable
    private fun BotonRendirse(color: Color, onConfirmar: () -> Unit) {
        var presionando by remember { mutableStateOf(false) }
        var carga by remember { mutableFloatStateOf(0f) }

        LaunchedEffect(presionando) {
            if (!presionando) {
                carga = 0f
                return@LaunchedEffect
            }
            val pasos = 30
            repeat(pasos) {
                delay(100)
                carga = (it + 1f) / pasos
            }
            onConfirmar()
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            presionando = true
                            tryAwaitRelease()
                            presionando = false
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(carga)
                    .height(52.dp)
                    .background(color.copy(alpha = 0.28f))
            )
            Text(
                text = if (presionando) "Sigue pulsando para rendirte…"
                else "Mantén pulsado para rendirte",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    @Composable
    private fun PantallaFin(onCerrar: () -> Unit) {
        val resultado = EstadoEnfoque.ultimoDesenlace
        val completada = resultado?.second == Desenlace.COMPLETADA

        // Soltamos el fijado en cuanto aparece esta pantalla: dejar el telefono
        // clavado despues de cumplir seria un castigo, no una ayuda.
        LaunchedEffect(Unit) { soltarFijado() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(text = if (completada) "✅" else "⏸️", fontSize = 58.sp)
                Spacer(Modifier.height(16.dp))
                Text(
                    text = if (completada) "Actividad completada" else "Sesión interrumpida",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = when {
                        completada && resultado != null ->
                            "Se anotaron ${resultado.first.duracionMin} minutos en tu hábito."
                        else -> "No pasa nada. El hábito sigue ahí mañana."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(28.dp))
                Button(onClick = onCerrar, modifier = Modifier.fillMaxWidth()) {
                    Text("Volver a mis hábitos")
                }
            }
        }
    }
}

/** Lanza la pantalla de enfoque desde cualquier actividad. */
fun Activity.abrirEnfoque() {
    startActivity(
        Intent(this, PantallaEnfoque::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    )
}

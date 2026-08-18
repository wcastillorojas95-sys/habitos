package com.lucas.habitos

import android.app.KeyguardManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.time.LocalDate

/**
 * La alarma de una actividad, a pantalla completa.
 *
 * Se comporta como el despertador del telefono: aparece sobre la pantalla de
 * bloqueo, enciende la pantalla, suena en bucle por el canal de alarma —el que
 * no se calla con el silencio— y vibra hasta que alguien decide algo.
 *
 * Se rinde sola a los dos minutos. Una alarma que suena eternamente en un bolso
 * no consigue que hagas el habito: consigue que desinstales la app.
 */
class PantallaAlarma : ComponentActivity() {

    private var reproductor: MediaPlayer? = null
    private var vibrador: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sobreLaPantallaDeBloqueo()

        val id = intent?.getStringExtra(Recordatorios.EXTRA_ID).orEmpty()
        val habito = Almacen(this).cargar().firstOrNull { it.id == id }

        if (habito == null) {
            finish()
            return
        }

        sonar()
        vibrar()

        val enfoque = AlmacenEnfoque(this)

        setContent {
            HabitosTheme(oscuro = enfoque.temaOscuro) {
                CaraDeAlarma(
                    habito = habito,
                    onEmpezar = {
                        callar()
                        Recordatorios.cancelarArranque(this, habito.id)
                        val minutos = Sesion.minutosSugeridos(
                            habito, LocalDate.now(), enfoque.duracionPreferidaMin
                        )
                        ServicioEnfoque.iniciar(
                            this,
                            Sesion.para(habito, minutos, enfoque.modoEstrictoPreferido)
                        )
                        abrirEnfoque()
                        finish()
                    },
                    onPosponer = {
                        callar()
                        Recordatorios.posponer(this, habito.id)
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        callar()
        super.onDestroy()
    }

    // ------------------------------------------------------------- el ruido ---

    private fun sonar() {
        val tono = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: return
        runCatching {
            reproductor = MediaPlayer().apply {
                setDataSource(this@PantallaAlarma, tono)
                // USAGE_ALARM es lo que hace que suene aunque el móvil esté en
                // silencio: es el canal de volumen del despertador, no el de
                // notificaciones.
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        }
    }

    private fun vibrar() {
        val patron = longArrayOf(0, 500, 700)
        runCatching {
            vibrador = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getSystemService(VibratorManager::class.java)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            // El 0 al final repite el patrón desde el principio, en bucle.
            vibrador?.vibrate(VibrationEffect.createWaveform(patron, 0))
        }
    }

    private fun callar() {
        runCatching { reproductor?.stop(); reproductor?.release() }
        reproductor = null
        runCatching { vibrador?.cancel() }
        vibrador = null
    }

    private fun sobreLaPantallaDeBloqueo() {
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
}

@Composable
private fun CaraDeAlarma(
    habito: Habito,
    onEmpezar: () -> Unit,
    onPosponer: () -> Unit
) {
    val color = PALETA[habito.color % PALETA.size]

    // Cuenta atrás para arrancar sola.
    //
    // Antes la alarma se rendía a los dos minutos y te dejaba en paz, que era
    // justo lo que fallaba: bastaba con no hacer nada. Ahora no hacer nada es
    // la respuesta que empieza la actividad. Salir sigue siendo posible, pero
    // hay que decidirlo: aplazar diez minutos.
    var restante by remember { mutableIntStateOf(Recordatorios.SEGUNDOS_ARRANQUE) }
    LaunchedEffect(Unit) {
        while (restante > 0) {
            delay(1000)
            restante--
        }
        onEmpezar()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(30.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(recursoDeIcono(habito.icono)),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(Modifier.height(26.dp))
            Text(
                text = "Es la hora",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = habito.nombre,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = horaTexto(habito.recordatorioMinutos),
                style = MaterialTheme.typography.headlineSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(44.dp))

            BotonAlarma(
                texto = "Empezar ahora",
                fondo = color,
                textoColor = Color.White,
                onClick = onEmpezar
            )
            Spacer(Modifier.height(12.dp))
            BotonAlarma(
                texto = "Ahora no · recuérdamelo en ${Recordatorios.MINUTOS_POSPONER} min",
                fondo = Color.Transparent,
                textoColor = MaterialTheme.colorScheme.onSurfaceVariant,
                borde = true,
                onClick = onPosponer
            )

            Spacer(Modifier.height(26.dp))
            Text(
                text = "Empieza sola en ${restante}s",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun BotonAlarma(
    texto: String,
    fondo: Color,
    textoColor: Color,
    borde: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(fondo)
            .then(
                if (borde) Modifier.border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(20.dp)
                ) else Modifier
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = textoColor,
            textAlign = TextAlign.Center
        )
    }
}

package com.lucas.habitos

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.time.LocalDate

class MainActivity : ComponentActivity() {

    /** Una sesión estricta no debería poder esquivarse volviendo a la lista. */
    override fun onStart() {
        super.onStart()
        val sesion = AlmacenEnfoque(this).sesionActiva()
        if (sesion != null && !sesion.terminada() && sesion.estricto) abrirEnfoque()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Recordatorios.crearCanal(this)

        val enfoque = AlmacenEnfoque(this)
        // Si la app se reabre con una sesión viva, la recuperamos del disco: el
        // objeto en memoria se pierde si el sistema mató el proceso.
        EstadoEnfoque.sesion = enfoque.sesionActiva()?.takeIf { !it.terminada() }

        setContent {
            // El tema lo manda el ajuste de la app, no el del sistema.
            var oscuro by remember { mutableStateOf(enfoque.temaOscuro) }
            HabitosTheme(oscuro = oscuro) {
                App(
                    oscuro = oscuro,
                    onCambiarTema = { enfoque.temaOscuro = it; oscuro = it }
                )
            }
        }
    }
}

private enum class Pantalla { HOY, EXPLORAR, PROGRESO, AJUSTES }

@Composable
private fun App(oscuro: Boolean, onCambiarTema: (Boolean) -> Unit) {
    val contexto = LocalContext.current
    val almacen = remember { Almacen(contexto) }
    val almacenEnfoque = remember { AlmacenEnfoque(contexto) }

    var habitos by remember { mutableStateOf(almacen.cargar()) }
    var pantalla by remember { mutableStateOf(Pantalla.HOY) }
    var editando by remember { mutableStateOf<Habito?>(null) }
    var abriendoEditor by remember { mutableStateOf(false) }
    var aEnfocar by remember { mutableStateOf<Habito?>(null) }
    var recienCreado by remember { mutableStateOf<Habito?>(null) }

    val pedirNotificaciones = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    val pedirCalendario = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    // Al volver a la app releemos: el widget, la notificación o una sesión de
    // enfoque pueden haber cambiado los datos mientras estaba en segundo plano.
    val duenoCiclo = LocalLifecycleOwner.current
    DisposableEffect(duenoCiclo) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) habitos = almacen.cargar()
        }
        duenoCiclo.lifecycle.addObserver(observador)
        onDispose { duenoCiclo.lifecycle.removeObserver(observador) }
    }

    fun aplicar(nuevos: List<Habito>) {
        habitos = nuevos
        almacen.guardar(nuevos)
        WidgetHabitos.refrescar(contexto)
    }

    fun guardarHabito(habito: Habito) {
        val existe = habitos.any { it.id == habito.id }
        aplicar(
            if (existe) habitos.map { if (it.id == habito.id) habito else it }
            else habitos + habito
        )

        Recordatorios.programar(contexto, habito)
        if (habito.recordatorio && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pedirNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (habito.enCalendario && !Calendario.tienePermiso(contexto)) {
            pedirCalendario.launch(Calendario.permisos)
        }
        // Recién creado: ofrecemos arrancar ya. Es el momento de más ganas.
        if (!existe) recienCreado = habito
    }

    if (abriendoEditor) {
        EditorHabito(
            original = editando,
            onCancelar = { abriendoEditor = false; editando = null },
            onGuardar = { h ->
                guardarHabito(h)
                abriendoEditor = false
                editando = null
            },
            onBorrar = { h ->
                Recordatorios.cancelar(contexto, h.id)
                aplicar(habitos.filterNot { it.id == h.id })
                abriendoEditor = false
                editando = null
            }
        )
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BarraFlotante(actual = pantalla, onElegir = { pantalla = it })
        }
    ) { relleno ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
        ) {
            AnimatedVisibility(pantalla == Pantalla.HOY, enter = fadeIn(), exit = fadeOut()) {
                PantallaHoy(
                    habitos = habitos,
                    onCambiar = { aplicar(it) },
                    onNuevo = { editando = null; abriendoEditor = true },
                    onEditar = { editando = it; abriendoEditor = true },
                    onEnfocar = { aEnfocar = it }
                )
            }
            AnimatedVisibility(pantalla == Pantalla.EXPLORAR, enter = fadeIn(), exit = fadeOut()) {
                PantallaExplorar(onCrear = { editando = it; abriendoEditor = true })
            }
            AnimatedVisibility(pantalla == Pantalla.PROGRESO, enter = fadeIn(), exit = fadeOut()) {
                PantallaEstadisticas(habitos = habitos, hoy = LocalDate.now())
            }
            AnimatedVisibility(pantalla == Pantalla.AJUSTES, enter = fadeIn(), exit = fadeOut()) {
                PantallaAjustes(
                    almacenEnfoque = almacenEnfoque,
                    oscuro = oscuro,
                    onCambiarTema = onCambiarTema,
                    habitos = habitos
                )
            }
        }
    }

    recienCreado?.let { nuevo ->
        AlertDialog(
            onDismissRequest = { recienCreado = null },
            title = { Text("${nuevo.emoji}  ${nuevo.nombre}") },
            text = { Text("Creado. ¿Le dedicas un rato ahora mismo? Empezar el primer día es lo que decide si el hábito cuaja.") },
            confirmButton = {
                TextButton(onClick = { recienCreado = null; aEnfocar = nuevo }) {
                    Text("Empezar ahora")
                }
            },
            dismissButton = {
                TextButton(onClick = { recienCreado = null }) { Text("Luego") }
            }
        )
    }

    aEnfocar?.let { objetivo ->
        DialogoEnfoque(
            habito = objetivo,
            hoy = LocalDate.now(),
            almacenEnfoque = almacenEnfoque,
            onCancelar = { aEnfocar = null },
            onEmpezar = { sesion ->
                aEnfocar = null
                ServicioEnfoque.iniciar(contexto, sesion)
                contexto.actividad()?.abrirEnfoque()
            }
        )
    }
}

/** La píldora oscura flotante con las cuatro páginas. */
@Composable
private fun BarraFlotante(actual: Pantalla, onElegir: (Pantalla) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF151312))
                .padding(horizontal = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PestanaPildora(IconoCasa, "Hoy", actual == Pantalla.HOY) { onElegir(Pantalla.HOY) }
            PestanaPildora(IconoLupa, "Explorar", actual == Pantalla.EXPLORAR) { onElegir(Pantalla.EXPLORAR) }
            PestanaPildora(IconoGrafico, "Progreso", actual == Pantalla.PROGRESO) { onElegir(Pantalla.PROGRESO) }
            PestanaPildora(IconoEngranaje, "Ajustes", actual == Pantalla.AJUSTES) { onElegir(Pantalla.AJUSTES) }
        }
    }
}

/** Círculo blanco, o píldora naranja con texto si está activa. */
@Composable
private fun PestanaPildora(
    icono: ImageVector,
    texto: String,
    activa: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (activa) MaterialTheme.colorScheme.primary else Color.White)
            .clickable { onClick() }
            .padding(horizontal = if (activa) 15.dp else 11.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = texto,
            tint = if (activa) Color.White else Color(0xFF151312),
            modifier = Modifier.size(19.dp)
        )
        if (activa) {
            Spacer(Modifier.width(7.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

/** Iconos dibujados a mano para no depender de la librería de iconos. */
val IconoCasa: ImageVector by lazy {
    vector {
        moveTo(12f, 3.2f)
        lineTo(3.5f, 10.2f)
        lineTo(3.5f, 20.5f)
        lineTo(9.5f, 20.5f)
        lineTo(9.5f, 14.5f)
        lineTo(14.5f, 14.5f)
        lineTo(14.5f, 20.5f)
        lineTo(20.5f, 20.5f)
        lineTo(20.5f, 10.2f)
        close()
    }
}

val IconoGrafico: ImageVector by lazy {
    vector {
        moveTo(4f, 20f); lineTo(8f, 20f); lineTo(8f, 11f); lineTo(4f, 11f); close()
        moveTo(10f, 20f); lineTo(14f, 20f); lineTo(14f, 4f); lineTo(10f, 4f); close()
        moveTo(16f, 20f); lineTo(20f, 20f); lineTo(20f, 14f); lineTo(16f, 14f); close()
    }
}

val IconoEngranaje: ImageVector by lazy {
    vector {
        moveTo(12f, 8.4f)
        curveTo(10f, 8.4f, 8.4f, 10f, 8.4f, 12f)
        curveTo(8.4f, 14f, 10f, 15.6f, 12f, 15.6f)
        curveTo(14f, 15.6f, 15.6f, 14f, 15.6f, 12f)
        curveTo(15.6f, 10f, 14f, 8.4f, 12f, 8.4f)
        close()
        moveTo(10.4f, 2f); lineTo(13.6f, 2f); lineTo(14.1f, 4.6f)
        lineTo(16.3f, 5.5f); lineTo(18.5f, 4f); lineTo(20f, 5.5f)
        lineTo(18.5f, 7.7f); lineTo(19.4f, 9.9f); lineTo(22f, 10.4f)
        lineTo(22f, 13.6f); lineTo(19.4f, 14.1f); lineTo(18.5f, 16.3f)
        lineTo(20f, 18.5f); lineTo(18.5f, 20f); lineTo(16.3f, 18.5f)
        lineTo(14.1f, 19.4f); lineTo(13.6f, 22f); lineTo(10.4f, 22f)
        lineTo(9.9f, 19.4f); lineTo(7.7f, 18.5f); lineTo(5.5f, 20f)
        lineTo(4f, 18.5f); lineTo(5.5f, 16.3f); lineTo(4.6f, 14.1f)
        lineTo(2f, 13.6f); lineTo(2f, 10.4f); lineTo(4.6f, 9.9f)
        lineTo(5.5f, 7.7f); lineTo(4f, 5.5f); lineTo(5.5f, 4f)
        lineTo(7.7f, 5.5f); lineTo(9.9f, 4.6f)
        close()
    }
}

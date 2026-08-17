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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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

    fun borrarHabito(habito: Habito) {
        Recordatorios.cancelar(contexto, habito.id)
        aplicar(habitos.filterNot { it.id == habito.id })
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
                borrarHabito(h)
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
                    onBorrar = { borrarHabito(it) },
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
            icon = {
                Icon(
                    painter = painterResource(recursoDeIcono(nuevo.icono)),
                    contentDescription = null,
                    tint = PALETA[nuevo.color % PALETA.size],
                    modifier = Modifier.size(26.dp)
                )
            },
            title = { Text(nuevo.nombre, fontWeight = FontWeight.Bold) },
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

/**
 * La píldora oscura flotante con las cuatro páginas.
 *
 * navigationBarsPadding la levanta por encima de la barra del sistema. Sin eso,
 * con enableEdgeToEdge la app dibuja hasta el borde inferior de la pantalla y la
 * píldora quedaba pisando los botones de Atrás e Inicio del teléfono.
 */
@Composable
private fun BarraFlotante(actual: Pantalla, onElegir: (Pantalla) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
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
    icono: Int,
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
            painter = painterResource(icono),
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

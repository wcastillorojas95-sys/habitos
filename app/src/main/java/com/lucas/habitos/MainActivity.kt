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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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

        // Si la app se reabre con una sesión de enfoque viva, la recuperamos del
        // disco: el objeto en memoria se pierde si el sistema mató el proceso.
        val enfoque = AlmacenEnfoque(this)
        EstadoEnfoque.sesion = enfoque.sesionActiva()?.takeIf { !it.terminada() }

        setContent {
            HabitosTheme {
                App()
            }
        }
    }
}

private enum class Pantalla { HOY, ESTADISTICAS }

@Composable
private fun App() {
    val contexto = LocalContext.current
    val almacen = remember { Almacen(contexto) }

    var habitos by remember { mutableStateOf(almacen.cargar()) }
    var pantalla by remember { mutableStateOf(Pantalla.HOY) }
    var editando by remember { mutableStateOf<Habito?>(null) }
    var abriendoEditor by remember { mutableStateOf(false) }
    var aEnfocar by remember { mutableStateOf<Habito?>(null) }

    val almacenEnfoque = remember { AlmacenEnfoque(contexto) }

    val pedirNotificaciones = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    val pedirCalendario = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    // Al volver a la app releemos: el widget o la notificación pueden haber
    // cambiado los datos mientras estaba en segundo plano.
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
        val nuevos = if (existe) habitos.map { if (it.id == habito.id) habito else it }
        else habitos + habito
        aplicar(nuevos)

        Recordatorios.programar(contexto, habito)
        if (habito.recordatorio && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pedirNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (habito.enCalendario && !Calendario.tienePermiso(contexto)) {
            pedirCalendario.launch(Calendario.permisos)
        }
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
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = pantalla == Pantalla.HOY,
                    onClick = { pantalla = Pantalla.HOY },
                    icon = { Icon(IconoCasa, contentDescription = null) },
                    label = { Text("Hoy") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                NavigationBarItem(
                    selected = pantalla == Pantalla.ESTADISTICAS,
                    onClick = { pantalla = Pantalla.ESTADISTICAS },
                    icon = { Icon(IconoGrafico, contentDescription = null) },
                    label = { Text("Progreso") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    ) { relleno ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
        ) {
            AnimatedVisibility(
                visible = pantalla == Pantalla.HOY,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                PantallaHoy(
                    habitos = habitos,
                    onCambiar = { aplicar(it) },
                    onNuevo = { editando = null; abriendoEditor = true },
                    onEditar = { editando = it; abriendoEditor = true },
                    onEnfocar = { aEnfocar = it }
                )
            }
            AnimatedVisibility(
                visible = pantalla == Pantalla.ESTADISTICAS,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                PantallaEstadisticas(habitos = habitos, hoy = LocalDate.now())
            }
        }
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

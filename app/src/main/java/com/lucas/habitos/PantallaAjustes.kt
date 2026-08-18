package com.lucas.habitos

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Build
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import java.time.LocalDate

/**
 * La pagina de configuracion.
 *
 * El PIN del modo estricto vive aqui, no dentro del cronometro: ponerlo en
 * caliente, justo cuando quieres rendirte, no serviria de nada.
 */
@Composable
fun PantallaAjustes(
    almacenEnfoque: AlmacenEnfoque,
    oscuro: Boolean,
    onCambiarTema: (Boolean) -> Unit,
    habitos: List<Habito>,
    onDatosRestaurados: (List<Habito>) -> Unit
) {
    val contexto = LocalContext.current
    val hoy = remember { LocalDate.now() }
    var cerrandoSesion by remember { mutableStateOf(false) }
    val cuenta = SesionUsuario.cuenta

    val minutos = remember(hoy) { almacenEnfoque.minutosUltimos(hoy, 7) }
    val activos = habitos.count { !it.archivado }

    // Se recalculan en cada recomposición a propósito: el usuario puede irse a los
    // ajustes del sistema, conceder el permiso y volver, y al volver debe verlo.
    val avisosOk = Recordatorios.avisosPermitidos(contexto)
    val exactasOk = Recordatorios.alarmasExactas(contexto)
    val pantallaCompletaOk = Recordatorios.pantallaCompletaPermitida(contexto)
    var bloquear by remember { mutableStateOf(almacenEnfoque.bloquearApps) }
    val accesibilidadOk = ServicioBloqueo.activo(contexto)
    val superponerOk = ServicioBloqueo.puedeSuperponer(contexto)
    var avisoCopia by remember { mutableStateOf<String?>(null) }
    var confirmandoImportar by remember { mutableStateOf(false) }

    // El selector de archivos del sistema evita pedir permisos de almacenamiento:
    // Android entrega el archivo ya abierto y solo ese.
    val guardarCopia = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { destino ->
        if (destino != null) avisoCopia = Copia.exportar(contexto, destino).mensaje
    }

    val abrirCopia = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { origen ->
        if (origen != null) {
            val resultado = Copia.importar(contexto, origen)
            avisoCopia = resultado.mensaje
            if (resultado.ok) onDatosRestaurados(resultado.habitos)
        }
    }

    val calendarioOk = Calendario.tienePermiso(contexto)
    var cuentaCalendario by remember {
        mutableStateOf(if (calendarioOk) Calendario.nombreDelCalendario(contexto) else "")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            item {
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Avatar(indice = 0, modifier = Modifier.size(52.dp).clip(CircleShape))
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = cuenta?.nombre?.ifBlank { null } ?: t("Sin cuenta", "No account"),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        cuenta?.correo?.takeIf { it.isNotBlank() }?.let { correo ->
                            Text(
                                text = correo,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "$activos hábitos · ${if (minutos >= 60) "${minutos / 60} h" else "$minutos min"} enfocado esta semana",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text(
                    text = t("Ajustes", "Settings"),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            item { Seccion(t("Cuenta", "Account")) }
            item {
                if (cuenta == null) {
                    FilaAccion(
                        titulo = t("Entrar con Google", "Sign in with Google"),
                        detalle = t("Ahora estás usando la app sin cuenta. Al entrar, la app te saluda por tu nombre. Tus hábitos no se mueven de este teléfono.", "You are using the app without an account. If you sign in, the app greets you by name. Your habits never leave this phone."),
                        onClick = {
                            almacenEnfoque.invitado = false
                            SesionUsuario.invitado = false
                            SesionUsuario.error = null
                        }
                    )
                } else {
                    FilaAccion(
                        titulo = t("Cerrar sesión", "Sign out"),
                        detalle = "Saldrás de ${cuenta.correo.ifBlank { "tu cuenta" }}. " +
                            t("Tus hábitos y su historial se quedan intactos en el teléfono.", "Your habits and their history stay untouched on the phone."),
                        onClick = { cerrandoSesion = true }
                    )
                }
            }

            item { Seccion(t("Apariencia", "Appearance")) }
            item {
                FilaInterruptor(
                    titulo = t("Modo oscuro", "Dark mode"),
                    detalle = t("Por defecto la app va en claro, que es como está pensado el diseño.", "The app runs light by default, which is how the design was made."),
                    marcado = oscuro,
                    onCambiar = onCambiarTema
                )
            }

            item { Seccion(t("Avisos", "Reminders")) }
            item {
                FilaAccion(
                    titulo = if (avisosOk) t("Avisos activados", "Reminders on") else t("Activar los avisos", "Turn reminders on"),
                    detalle = if (avisosOk) {
                        t("Los recordatorios pueden llegarte. Toca para revisarlos o silenciar alguno.", "Reminders can reach you. Tap to review them or silence one.")
                    } else {
                        t("Ahora mismo el teléfono los tiene bloqueados, así que ningún recordatorio te llegará.", "Right now the phone has them blocked, so no reminder will reach you.")
                    },
                    onClick = {
                        contexto.startActivity(
                            Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(
                                    android.provider.Settings.EXTRA_APP_PACKAGE,
                                    contexto.packageName
                                )
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                )
            }
            if (!pantallaCompletaOk) {
                item {
                    FilaAccion(
                        titulo = t("Permitir alarmas a pantalla completa", "Allow full-screen alarms"),
                        detalle = t("Sin esto, a la hora de una actividad verás una notificación normal en vez de la alarma que ocupa la pantalla y suena aunque tengas el móvil en silencio.", "Without this, at the time of an activity you get an ordinary notification instead of the alarm that takes over the screen and rings even on silent."),
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                contexto.startActivity(
                                    Intent(android.provider.Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
                                        .setData(android.net.Uri.parse("package:${contexto.packageName}"))
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        }
                    )
                }
            }
            if (!exactasOk) {
                item {
                    FilaAccion(
                        titulo = t("Permitir avisos a la hora exacta", "Allow exact-time reminders"),
                        detalle = t("Sin esto Android agrupa las alarmas para ahorrar batería y el recordatorio de las 8:00 puede llegarte a las 8:40 o no llegar.", "Without this Android batches alarms to save battery, and your 8:00 reminder may arrive at 8:40, or not at all."),
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                contexto.startActivity(
                                    Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                        .setData(android.net.Uri.parse("package:${contexto.packageName}"))
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        }
                    )
                }
            }

            item { Seccion(t("Calendario", "Calendar")) }
            item {
                if (!calendarioOk) {
                    FilaAccion(
                        titulo = t("Dar acceso al calendario", "Grant calendar access"),
                        detalle = t("Sin permiso, los hábitos que marques para el calendario no reservan ningún hueco. Toca para concederlo en los ajustes del sistema.", "Without permission, habits marked for the calendar reserve nothing. Tap to grant it in the system settings."),
                        onClick = {
                            contexto.startActivity(
                                Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    .setData(android.net.Uri.parse("package:${contexto.packageName}"))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    )
                } else {
                    FilaAccion(
                        titulo = "Escribiendo en: ${cuentaCalendario.ifBlank { "calendario del teléfono" }}",
                        detalle = if (cuentaCalendario.contains("@")) {
                            "Es una cuenta de Google, así que lo que se reserve aparece también " +
                                "en Google Calendar. Toca para volver a elegir calendario."
                        } else {
                            "Ojo: no parece una cuenta de Google, así que esto se queda solo en " +
                                "el teléfono y no sube a Google Calendar. Toca para volver a elegir."
                        },
                        onClick = {
                            Calendario.olvidarCalendario(contexto)
                            val nuevos = Calendario.sincronizarTodos(contexto, habitos)
                            Almacen(contexto).guardar(nuevos)
                            cuentaCalendario = Calendario.nombreDelCalendario(contexto)
                        }
                    )
                }
            }

            item { Seccion(t("Modo estricto", "Strict mode")) }
            item {
                FilaInformativa(
                    titulo = t("Reto para abandonar", "Challenge to quit"),
                    detalle = t("Para dejar una actividad antes de tiempo eliges un tema, lees una cápsula de divulgación y aciertas tres preguntas sobre detalles del texto. No valen las de cultura general: hay que haber leído. Si fallas una, empiezas de cero con otro texto.", "To drop an activity early you pick a subject, read a short piece and answer three questions about details in the text. General knowledge will not save you: you have to read it. Miss one and you start over with a different text."))
            }
            item {
                FilaAccion(
                    titulo = t("Permitir No molestar", "Allow Do Not Disturb"),
                    detalle = t("Para que el teléfono se silencie solo mientras dura una actividad.", "So the phone silences itself while an activity lasts."),
                    onClick = {
                        contexto.startActivity(
                            Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                )
            }

            item { Seccion(t("Bloquear otras apps", "Block other apps")) }
            item {
                FilaInterruptor(
                    titulo = t("Bloquear el teléfono durante la actividad", "Block the phone during an activity"),
                    detalle = t("Mientras corre una sesión, cualquier app que abras se tapa con una pantalla que solo enseña el tiempo que falta. No hay botón para saltársela: se desbloquea sola al terminar.", "While a session runs, any app you open is covered by a screen showing only the time left. There is no button to skip it: it unlocks itself when the time is up."),
                    marcado = bloquear,
                    onCambiar = {
                        bloquear = it
                        almacenEnfoque.bloquearApps = it
                    }
                )
            }
            if (bloquear && !accesibilidadOk) {
                item {
                    FilaAccion(
                        titulo = t("Falta un permiso: accesibilidad", "Missing permission: accessibility"),
                        detalle = t("Es la única vía que da Android para saber qué app tienes delante. Toca, busca «Hábitos» en la lista y actívalo. Solo se mira el nombre de la app abierta, nunca lo que hay escrito en ella.", "It is the only way Android offers to know which app is in front of you. Tap, find «Hábitos» in the list and turn it on. Only the name of the open app is read, never what is written in it."),
                        onClick = { ServicioBloqueo.abrirAccesibilidad(contexto) }
                    )
                }
            }
            if (bloquear && !superponerOk) {
                item {
                    FilaAccion(
                        titulo = t("Falta un permiso: mostrar sobre otras apps", "Missing permission: display over other apps"),
                        detalle = t("Sin esto Android no deja que la pantalla de bloqueo salga encima de la app que acabas de abrir.", "Without this, Android will not let the blocking screen appear on top of the app you just opened."),
                        onClick = { ServicioBloqueo.abrirSuperposicion(contexto) }
                    )
                }
            }
            if (bloquear && accesibilidadOk && superponerOk) {
                item {
                    FilaAccion(
                        titulo = t("Bloqueo listo", "Blocking ready"),
                        detalle = t("Se bloquea todo menos el teléfono, WhatsApp, el teclado y los Ajustes de Android. Los Ajustes se dejan a propósito: son la salida de emergencia si quieres apagar esto. Toca para ir allí.", "Everything is blocked except the phone, WhatsApp, the keyboard and Android Settings. Settings are left open on purpose: they are the emergency exit if you want to turn this off. Tap to go there."),
                        onClick = { ServicioBloqueo.abrirAccesibilidad(contexto) }
                    )
                }
            }

            item { Seccion(t("Copia de seguridad", "Backup")) }
            item {
                FilaAccion(
                    titulo = t("Guardar una copia", "Save a backup"),
                    detalle = t("Un archivo con todos tus hábitos, su historial y tus minutos de enfoque. Guárdalo en Drive o donde quieras. El PIN no se incluye.", "One file with all your habits, their history and your focus minutes. Keep it in Drive or wherever you like."),
                    onClick = { guardarCopia.launch(Copia.nombreSugerido()) }
                )
            }
            item {
                FilaAccion(
                    titulo = t("Restaurar una copia", "Restore a backup"),
                    detalle = t("Recupera todo desde un archivo guardado. Sustituye lo que tengas ahora, así que se te pedirá confirmación.", "Brings everything back from a saved file. It replaces what you have now, so you will be asked to confirm."),
                    onClick = { confirmandoImportar = true }
                )
            }

            item { Seccion(t("Acerca de", "About")) }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = "Hábitos 3.7",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Tipografías Bricolage Grotesque y DM Sans (SIL Open Font " +
                                "License). Ilustraciones de illlustrations.co (Vijay Verma, " +
                                "licencia MIT). Iconos de la interfaz: Material Icons de Google " +
                                "(Apache 2.0). Iconos de los hábitos: Solar, de 480 Design " +
                                "(CC BY 4.0). Avatares dibujados para esta app.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    avisoCopia?.let { mensaje ->
        AlertDialog(
            onDismissRequest = { avisoCopia = null },
            title = { Text("Copia de seguridad", fontWeight = FontWeight.Bold) },
            text = { Text(mensaje) },
            confirmButton = {
                TextButton(onClick = { avisoCopia = null }) { Text("Entendido") }
            }
        )
    }

    if (confirmandoImportar) {
        AlertDialog(
            onDismissRequest = { confirmandoImportar = false },
            title = { Text("¿Restaurar una copia?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Los hábitos que tengas ahora se sustituyen por los del archivo, con su " +
                        "historial. Esto no se puede deshacer, así que si tienes algo que no " +
                        "esté en la copia, guarda una antes."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmandoImportar = false
                    abrirCopia.launch(arrayOf("application/json", "text/plain", "*/*"))
                }) { Text("Elegir archivo") }
            },
            dismissButton = {
                TextButton(onClick = { confirmandoImportar = false }) { Text("Cancelar") }
            }
        )
    }

    if (cerrandoSesion) {
        AlertDialog(
            onDismissRequest = { cerrandoSesion = false },
            title = { Text("¿Cerrar sesión?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Volverás a la pantalla de entrada. Tus hábitos siguen guardados " +
                        "en el teléfono, no se borra nada."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    cerrandoSesion = false
                    Autenticacion.salir(contexto)
                    almacenEnfoque.invitado = false
                    SesionUsuario.invitado = false
                }) { Text("Cerrar sesión") }
            },
            dismissButton = {
                TextButton(onClick = { cerrandoSesion = false }) { Text("Cancelar") }
            }
        )
    }

}

/** Como [FilaAccion] pero sin destino: solo explica algo. */
@Composable
private fun FilaInformativa(titulo: String, detalle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Seccion(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 18.dp, bottom = 4.dp)
    )
}

@Composable
private fun FilaInterruptor(
    titulo: String,
    detalle: String,
    marcado: Boolean,
    onCambiar: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = detalle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(12.dp))
            Switch(checked = marcado, onCheckedChange = onCambiar)
        }
    }
}

@Composable
private fun FilaAccion(titulo: String, detalle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

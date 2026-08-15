package com.lucas.habitos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.util.UUID

val PALETA = listOf(
    Color(0xFF2E9E5B),
    Color(0xFF2F80ED),
    Color(0xFFF2994A),
    Color(0xFFEB5757),
    Color(0xFF9B51E0),
    Color(0xFF00B8D9)
)

val EMOJIS = listOf(
    "💧", "🏃", "📖", "🧘", "💪", "🥗",
    "😴", "🎸", "✍️", "🧹", "💊", "🌱"
)

private val LETRAS_DIA = listOf("L", "M", "X", "J", "V", "S", "D")
private val NOMBRES_DIA = listOf(
    "lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo"
)
private val MESES = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
)

private fun fechaLarga(f: LocalDate): String =
    "${NOMBRES_DIA[f.dayOfWeek.value - 1]} ${f.dayOfMonth} de ${MESES[f.monthValue - 1]}"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val almacen = Almacen(applicationContext)
        setContent {
            HabitosTheme {
                PantallaPrincipal(almacen)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(almacen: Almacen) {
    val hoy = remember { LocalDate.now() }
    var habitos by remember { mutableStateOf(almacen.cargar()) }
    var diaSel by remember { mutableStateOf(hoy) }
    var creando by remember { mutableStateOf(false) }
    var porBorrar by remember { mutableStateOf<Habito?>(null) }

    fun aplicar(nuevos: List<Habito>) {
        habitos = nuevos
        almacen.guardar(nuevos)
    }

    val inicioSemana = diaSel.minusDays((diaSel.dayOfWeek.value - 1).toLong())
    val semana = (0L..6L).map { inicioSemana.plusDays(it) }
    val completados = habitos.count { it.hechoEn(diaSel) }
    val fraccion = if (habitos.isEmpty()) 0f else completados.toFloat() / habitos.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Mis hábitos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (diaSel == hoy) "Hoy, ${fechaLarga(diaSel)}" else fechaLarga(diaSel),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { creando = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo hábito") }
            )
        }
    ) { relleno ->
        Column(
            modifier = Modifier
                .padding(relleno)
                .fillMaxSize()
        ) {

            TiraSemana(
                semana = semana,
                diaSel = diaSel,
                hoy = hoy,
                habitos = habitos,
                onElegir = { diaSel = it }
            )

            if (habitos.isNotEmpty()) {
                TarjetaProgreso(completados, habitos.size, fraccion)
            }

            if (habitos.isEmpty()) {
                Box(modifier = Modifier.weight(1f)) {
                    EstadoVacio()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, top = 4.dp, bottom = 110.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(habitos, key = { it.id }) { h ->
                        TarjetaHabito(
                            habito = h,
                            dia = diaSel,
                            hoy = hoy,
                            onAlternar = {
                                val clave = diaSel.toString()
                                aplicar(
                                    habitos.map {
                                        if (it.id == h.id) {
                                            it.copy(
                                                hechos = if (clave in it.hechos) it.hechos - clave
                                                else it.hechos + clave
                                            )
                                        } else it
                                    }
                                )
                            },
                            onBorrar = { porBorrar = h }
                        )
                    }

                    item {
                        Spacer(Modifier.height(6.dp))
                        Resumen(habitos, hoy)
                    }
                }
            }
        }
    }

    if (creando) {
        DialogoNuevoHabito(
            onCancelar = { creando = false },
            onCrear = { nombre, emoji, color ->
                aplicar(
                    habitos + Habito(
                        id = UUID.randomUUID().toString(),
                        nombre = nombre,
                        emoji = emoji,
                        color = color,
                        creado = hoy.toString(),
                        hechos = emptySet()
                    )
                )
                creando = false
            }
        )
    }

    porBorrar?.let { objetivo ->
        AlertDialog(
            onDismissRequest = { porBorrar = null },
            title = { Text("¿Eliminar \"${objetivo.nombre}\"?") },
            text = { Text("Se borrará también todo su historial. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    aplicar(habitos.filterNot { it.id == objetivo.id })
                    porBorrar = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { porBorrar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun TiraSemana(
    semana: List<LocalDate>,
    diaSel: LocalDate,
    hoy: LocalDate,
    habitos: List<Habito>,
    onElegir: (LocalDate) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        semana.forEach { dia ->
            val elegido = dia == diaSel
            val futuro = dia.isAfter(hoy)
            val todoHecho = habitos.isNotEmpty() && habitos.all { it.hechoEn(dia) }

            val colorTexto = when {
                elegido -> MaterialTheme.colorScheme.onPrimary
                futuro -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (elegido) MaterialTheme.colorScheme.primary else Color.Transparent
                    )
                    .clickable(enabled = !futuro) { onElegir(dia) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = LETRAS_DIA[dia.dayOfWeek.value - 1],
                    style = MaterialTheme.typography.labelSmall,
                    color = colorTexto
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = dia.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (dia == hoy) FontWeight.Bold else FontWeight.Normal,
                    color = if (elegido) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = if (futuro) 0.35f else 1f
                    )
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                todoHecho && elegido -> MaterialTheme.colorScheme.onPrimary
                                todoHecho -> MaterialTheme.colorScheme.primary
                                else -> Color.Transparent
                            }
                        )
                )
            }
        }
    }
}

@Composable
private fun TarjetaProgreso(completados: Int, total: Int, fraccion: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$completados de $total completados",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${(fraccion * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { fraccion },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
            )
        }
    }
}

@Composable
private fun TarjetaHabito(
    habito: Habito,
    dia: LocalDate,
    hoy: LocalDate,
    onAlternar: () -> Unit,
    onBorrar: () -> Unit
) {
    val color = PALETA[habito.color % PALETA.size]
    val marcado = habito.hechoEn(dia)
    val racha = habito.racha(hoy)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (marcado) color.copy(alpha = 0.13f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAlternar() }
                .padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = habito.emoji, fontSize = 21.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = habito.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = if (racha > 0) {
                        "🔥 $racha ${if (racha == 1) "día" else "días"} seguidos"
                    } else {
                        "Empieza tu racha"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(7.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (atras in 6 downTo 0) {
                        val d = hoy.minusDays(atras.toLong())
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (habito.hechoEn(d)) color
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                )
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (marcado) color else Color.Transparent)
                    .border(
                        width = 2.dp,
                        color = if (marcado) color else MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
                    .clickable { onAlternar() },
                contentAlignment = Alignment.Center
            ) {
                if (marcado) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Cumplido",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            IconButton(onClick = onBorrar) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar hábito",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun Resumen(habitos: List<Habito>, hoy: LocalDate) {
    val mejor = habitos.maxOfOrNull { it.mejorRacha() } ?: 0
    val ultimos7 = habitos.sumOf { it.cumplidosUltimos(hoy, 7) }
    val posibles = habitos.size * 7
    val porcentaje = if (posibles == 0) 0 else ultimos7 * 100 / posibles

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DatoResumen("$porcentaje%", "últimos 7 días")
            DatoResumen("$mejor", if (mejor == 1) "mejor racha (día)" else "mejor racha (días)")
            DatoResumen("${habitos.size}", if (habitos.size == 1) "hábito" else "hábitos")
        }
    }
}

@Composable
private fun DatoResumen(valor: String, etiqueta: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = valor,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EstadoVacio() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🌱", fontSize = 56.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Todavía no tienes hábitos",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Toca «Nuevo hábito» y empieza con uno solo. Es más fácil sostener uno que cinco.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DialogoNuevoHabito(
    onCancelar: () -> Unit,
    onCrear: (String, String, Int) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf(EMOJIS.first()) }
    var color by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Nuevo hábito") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { if (it.length <= 40) nombre = it },
                    label = { Text("¿Qué quieres hacer cada día?") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(18.dp))
                Text("Ícono", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))

                EMOJIS.chunked(6).forEach { fila ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        fila.forEach { e ->
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (e == emoji) MaterialTheme.colorScheme.primaryContainer
                                        else Color.Transparent
                                    )
                                    .clickable { emoji = e },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = e, fontSize = 20.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
                Text("Color", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PALETA.forEachIndexed { i, c ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(c)
                                .then(
                                    if (i == color) {
                                        Modifier.border(
                                            width = 3.dp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            shape = CircleShape
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { color = i }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCrear(nombre.trim(), emoji, color) },
                enabled = nombre.isNotBlank()
            ) { Text("Crear") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}

package com.lucas.habitos

import android.app.TimePickerDialog
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.util.UUID

@Composable
fun EditorHabito(
    original: Habito?,
    onCancelar: () -> Unit,
    onGuardar: (Habito) -> Unit,
    onBorrar: (Habito) -> Unit
) {
    val contexto = LocalContext.current

    var nombre by remember { mutableStateOf(original?.nombre ?: "") }
    var icono by remember { mutableStateOf(claveDeIcono(original?.icono)) }
    var color by remember { mutableStateOf(original?.color ?: 0) }
    var frecuencia by remember { mutableStateOf(original?.frecuencia ?: Frecuencia.DIARIO) }
    var dias by remember { mutableStateOf(original?.diasSemana ?: setOf(1, 2, 3, 4, 5, 6, 7)) }
    var veces by remember { mutableStateOf(original?.vecesPorSemana ?: 3) }
    var cadaN by remember { mutableStateOf(original?.cadaNDias ?: 2) }
    var meta by remember { mutableStateOf(original?.meta ?: Meta.SI_NO) }
    var cantidad by remember { mutableStateOf(original?.metaCantidad ?: 8) }
    var unidad by remember { mutableStateOf(original?.unidad ?: "") }
    var recordatorio by remember { mutableStateOf(original?.recordatorio ?: false) }
    var minutos by remember { mutableStateOf(original?.recordatorioMinutos ?: 8 * 60) }
    var calendario by remember { mutableStateOf(original?.enCalendario ?: false) }
    var confirmandoBorrado by remember { mutableStateOf(false) }

    val acento = PALETA[color % PALETA.size]

    // El editor se dibuja fuera del Scaffold, así que nadie le reserva el hueco
    // de la barra de estado ni el de la de navegación: sin esto, la flecha de
    // volver y la papelera quedan pegadas al reloj del teléfono.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {

        // Cabecera
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 18.dp, top = 12.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .clickable { onCancelar() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(IconoAtras),
                    contentDescription = "Volver",
                    modifier = Modifier.size(21.dp)
                )
            }
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (original == null) "Nuevo hábito" else "Editar hábito",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (original != null) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable { confirmandoBorrado = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(IconoBasuraLinea),
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
        ) {

            OutlinedTextField(
                value = nombre,
                onValueChange = { if (it.length <= 40) nombre = it },
                label = { Text("¿Qué quieres hacer?") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Titulo("Ícono")
            Rejilla(ICONOS_HABITO, 6) { entrada ->
                val elegido = entrada.clave == icono
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (elegido) acento.copy(alpha = 0.18f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                        .clickable { icono = entrada.clave },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(entrada.recurso),
                        contentDescription = entrada.clave,
                        tint = if (elegido) acento else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Titulo("Color")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PALETA.forEachIndexed { i, c ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(c)
                            .then(
                                if (i == color) Modifier.border(
                                    3.dp, MaterialTheme.colorScheme.onSurface, CircleShape
                                ) else Modifier
                            )
                            .clickable { color = i }
                    )
                }
            }

            Titulo("¿Cada cuánto?")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Fila {
                    Chip("Todos los días", frecuencia == Frecuencia.DIARIO, acento) {
                        frecuencia = Frecuencia.DIARIO
                    }
                    Chip("Días fijos", frecuencia == Frecuencia.DIAS_SEMANA, acento) {
                        frecuencia = Frecuencia.DIAS_SEMANA
                    }
                }
                Fila {
                    Chip("Veces por semana", frecuencia == Frecuencia.VECES_SEMANA, acento) {
                        frecuencia = Frecuencia.VECES_SEMANA
                    }
                    Chip("Cada N días", frecuencia == Frecuencia.CADA_N_DIAS, acento) {
                        frecuencia = Frecuencia.CADA_N_DIAS
                    }
                }
            }

            when (frecuencia) {
                Frecuencia.DIAS_SEMANA -> {
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (1..7).forEach { d ->
                            val activo = dias.contains(d)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (activo) acento.copy(alpha = 0.20f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .clickable {
                                        dias = if (activo && dias.size > 1) dias - d else dias + d
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = LETRAS_DIA[d - 1],
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (activo) FontWeight.Bold else FontWeight.Medium,
                                    color = if (activo) acento
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Frecuencia.VECES_SEMANA -> {
                    Spacer(Modifier.height(12.dp))
                    Contador(
                        etiqueta = "veces por semana",
                        valor = veces,
                        acento = acento,
                        minimo = 1,
                        maximo = 7
                    ) { veces = it }
                }

                Frecuencia.CADA_N_DIAS -> {
                    Spacer(Modifier.height(12.dp))
                    Contador(
                        etiqueta = "días entre cada vez",
                        valor = cadaN,
                        acento = acento,
                        minimo = 2,
                        maximo = 30
                    ) { cadaN = it }
                }

                Frecuencia.DIARIO -> {}
            }

            Titulo("¿Cómo se mide?")
            Fila {
                Chip("Hecho o no", meta == Meta.SI_NO, acento) { meta = Meta.SI_NO }
                Chip("Cantidad", meta == Meta.CANTIDAD, acento) { meta = Meta.CANTIDAD }
                Chip("Minutos", meta == Meta.TIEMPO, acento) { meta = Meta.TIEMPO }
            }

            if (meta != Meta.SI_NO) {
                Spacer(Modifier.height(12.dp))
                Contador(
                    etiqueta = if (meta == Meta.TIEMPO) "minutos al día" else "por día",
                    valor = cantidad,
                    acento = acento,
                    minimo = 1,
                    maximo = if (meta == Meta.TIEMPO) 480 else 1000,
                    paso = if (meta == Meta.TIEMPO) 5 else 1
                ) { cantidad = it }

                if (meta == Meta.CANTIDAD) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = unidad,
                        onValueChange = { if (it.length <= 12) unidad = it },
                        label = { Text("Unidad (vasos, km, páginas…)") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Titulo("Recordatorio")
            Interruptor(
                titulo = "Avisarme cada día",
                detalle = if (recordatorio) "A las ${horaTexto(minutos)}" else "Sin aviso",
                activo = recordatorio,
                acento = acento,
                onCambiar = { recordatorio = it }
            )
            // La hora sirve para las dos cosas: para el aviso y para el hueco que
            // se reserva en el calendario. Antes solo se podía tocar con el aviso
            // encendido, así que quien solo quería el calendario no podía elegirla.
            if (recordatorio || calendario) {
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        TimePickerDialog(
                            contexto,
                            { _, h, m -> minutos = h * 60 + m },
                            minutos / 60,
                            minutos % 60,
                            true
                        ).show()
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = acento.copy(alpha = 0.16f),
                        contentColor = acento
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cambiar hora · ${horaTexto(minutos)}")
                }
            }

            Titulo("Calendario")
            Interruptor(
                titulo = "Anotar en mi calendario",
                detalle = "Reserva el hueco a las ${horaTexto(minutos)} en tu calendario, " +
                    "repitiéndose según la frecuencia. Aparece también en Google Calendar.",
                activo = calendario,
                acento = acento,
                onCambiar = { calendario = it }
            )

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    val h = (original ?: Habito(
                        id = UUID.randomUUID().toString(),
                        nombre = "",
                        icono = icono,
                        color = color,
                        creado = LocalDate.now().toString()
                    )).copy(
                        nombre = nombre.trim(),
                        icono = icono,
                        color = color,
                        frecuencia = frecuencia,
                        diasSemana = dias,
                        vecesPorSemana = veces,
                        cadaNDias = cadaN,
                        meta = meta,
                        metaCantidad = if (meta == Meta.SI_NO) 1 else cantidad,
                        unidad = if (meta == Meta.TIEMPO) "min" else unidad.trim(),
                        recordatorio = recordatorio,
                        recordatorioMinutos = minutos,
                        enCalendario = calendario
                    )
                    onGuardar(h)
                },
                enabled = nombre.isNotBlank(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = acento,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = if (original == null) "Crear hábito" else "Guardar cambios",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.height(30.dp))
        }
    }

    if (confirmandoBorrado && original != null) {
        AlertDialog(
            onDismissRequest = { confirmandoBorrado = false },
            title = { Text("¿Eliminar \"${original.nombre}\"?") },
            text = { Text("Se borrará también todo su historial. No se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmandoBorrado = false
                    onBorrar(original)
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmandoBorrado = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun Titulo(texto: String) {
    Spacer(Modifier.height(28.dp))
    Text(
        text = texto.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun Fila(contenido: @Composable () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { contenido() }
}

@Composable
private fun Chip(texto: String, activo: Boolean, acento: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (activo) acento.copy(alpha = 0.18f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (activo) FontWeight.Bold else FontWeight.Medium,
            color = if (activo) acento else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Rejilla(
    elementos: List<IconoHabito>,
    porFila: Int,
    contenido: @Composable (IconoHabito) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        elementos.chunked(porFila).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                fila.forEach { contenido(it) }
            }
        }
    }
}

@Composable
private fun Contador(
    etiqueta: String,
    valor: Int,
    acento: Color,
    minimo: Int,
    maximo: Int,
    paso: Int = 1,
    onCambiar: (Int) -> Unit
) {
    // El texto va aparte del número para poder dejar el campo vacío mientras se
    // escribe sin que salte al mínimo en cuanto se borra la última cifra.
    var texto by remember { mutableStateOf(valor.toString()) }
    var editando by remember { mutableStateOf(false) }

    // Si el valor cambia desde fuera (los botones − y +), el texto lo sigue.
    LaunchedEffect(valor) { if (!editando) texto = valor.toString() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(acento.copy(alpha = 0.16f))
                .clickable { onCambiar((valor - paso).coerceAtLeast(minimo)) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(IconoMenos),
                contentDescription = "Restar",
                tint = acento,
                modifier = Modifier.size(16.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Campo escribible, no solo un número: para poner 33 minutos con los
            // botones harían falta siete toques.
            BasicTextField(
                value = texto,
                onValueChange = { entrada ->
                    val limpio = entrada.filter { it.isDigit() }.take(4)
                    texto = limpio
                    limpio.toIntOrNull()?.let { n -> if (n in minimo..maximo) onCambiar(n) }
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(acento),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .width(96.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 4.dp)
                    .onFocusChanged { estado ->
                        editando = estado.isFocused
                        if (!estado.isFocused) {
                            val n = texto.toIntOrNull()?.coerceIn(minimo, maximo) ?: valor
                            onCambiar(n)
                            texto = n.toString()
                        }
                    }
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(acento.copy(alpha = 0.16f))
                .clickable { onCambiar((valor + paso).coerceAtMost(maximo)) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(IconoMas),
                contentDescription = "Sumar",
                tint = acento,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun Interruptor(
    titulo: String,
    detalle: String,
    activo: Boolean,
    acento: Color,
    onCambiar: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onCambiar(!activo) }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(10.dp))
        Switch(checked = activo, onCheckedChange = onCambiar)
    }
}

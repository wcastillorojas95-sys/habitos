package com.lucas.habitos

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.util.UUID

/**
 * Crear varios hábitos de una vez.
 *
 * Nace de una fricción concreta: montar una rutina de cuatro cosas obligaba a
 * pasar cuatro veces por el editor entero. Aquí se marcan de una lista y se
 * crean todos juntos, ya configurados con su icono, su frecuencia y su meta.
 *
 * Lo que sale de aquí son hábitos normales y corrientes: no quedan atados a
 * ninguna lista ni se comportan distinto. La lista es solo el atajo de entrada.
 */
@Composable
fun PantallaLista(
    yaCreados: List<Habito>,
    onCancelar: () -> Unit,
    onCrear: (List<Habito>) -> Unit
) {
    // Se compara por nombre en minúsculas porque es lo que el usuario reconoce:
    // si ya tiene "Leer", ofrecerle crear otro "Leer" solo genera duplicados.
    val existentes = remember(yaCreados) {
        yaCreados.filter { !it.archivado }.map { it.nombre.trim().lowercase() }.toSet()
    }

    var elegidas by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 18.dp, top = 12.dp, bottom = 4.dp),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Nueva lista",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Marca las que quieras y se crean todas de una vez",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CATEGORIAS.forEach { categoria ->
                item(key = "cab-${categoria.nombre}") {
                    Text(
                        text = categoria.nombre.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 14.dp, bottom = 2.dp)
                    )
                }
                items(
                    items = categoria.actividades,
                    key = { "${categoria.nombre}|${it.nombre}" }
                ) { plantilla ->
                    val marca = "${categoria.nombre}|${plantilla.nombre}"
                    FilaElegible(
                        plantilla = plantilla,
                        color = PALETA[categoria.color % PALETA.size],
                        marcada = marca in elegidas,
                        yaLoTiene = plantilla.nombre.trim().lowercase() in existentes,
                        onClick = {
                            elegidas = if (marca in elegidas) elegidas - marca else elegidas + marca
                        }
                    )
                }
            }
        }

        // La barra de abajo dice cuántas van: sin ese número hay que contar las
        // marcas hacia atrás para saber qué se va a crear.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Button(
                onClick = {
                    val nuevos = CATEGORIAS.flatMap { cat ->
                        cat.actividades
                            .filter { "${cat.nombre}|${it.nombre}" in elegidas }
                            .map { plantilla ->
                                Habito(
                                    id = UUID.randomUUID().toString(),
                                    nombre = plantilla.nombre,
                                    icono = plantilla.icono,
                                    color = cat.color,
                                    creado = LocalDate.now().toString(),
                                    categoria = cat.nombre,
                                    meta = plantilla.meta,
                                    metaCantidad = plantilla.cantidad,
                                    unidad = plantilla.unidad
                                )
                            }
                    }
                    if (nuevos.isNotEmpty()) onCrear(nuevos)
                },
                enabled = elegidas.isNotEmpty(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = when (elegidas.size) {
                        0 -> "Elige al menos una"
                        1 -> "Crear 1 hábito"
                        else -> "Crear ${elegidas.size} hábitos"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FilaElegible(
    plantilla: Plantilla,
    color: Color,
    marcada: Boolean,
    yaLoTiene: Boolean,
    onClick: () -> Unit
) {
    val apagado = yaLoTiene
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (marcada) color.copy(alpha = 0.14f)
                else MaterialTheme.colorScheme.surface
            )
            .clickable(enabled = !apagado) { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = if (apagado) 0.08f else 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(recursoDeIcono(plantilla.icono)),
                contentDescription = null,
                tint = if (apagado) color.copy(alpha = 0.5f) else color,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(13.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = plantilla.nombre,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (apagado) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = when {
                    apagado -> "Ya lo tienes"
                    plantilla.meta == Meta.TIEMPO -> "${plantilla.cantidad} min al día"
                    plantilla.meta == Meta.CANTIDAD ->
                        "${plantilla.cantidad} ${plantilla.unidad} al día"
                    else -> "Todos los días"
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(if (marcada) color else Color.Transparent)
                .border(
                    width = 2.dp,
                    color = if (marcada) color
                    else MaterialTheme.colorScheme.outline.copy(alpha = if (apagado) 0.3f else 0.7f),
                    shape = RoundedCornerShape(9.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (marcada) {
                Icon(
                    painter = painterResource(IconoCheck),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

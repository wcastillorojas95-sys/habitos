package com.lucas.habitos

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.util.UUID

/**
 * Una categoria del explorador, con su ilustracion y sus actividades sugeridas.
 *
 * Las plantillas no son decoracion: al tocarlas se abre el editor con el habito
 * ya relleno. La friccion de crear un habito desde cero es justo lo que hace que
 * la gente no cree ninguno.
 */
private data class Categoria(
    val nombre: String,
    val ilustracion: Int,
    val color: Int,
    val actividades: List<Plantilla>
)

private data class Plantilla(
    val nombre: String,
    val emoji: String,
    val meta: Meta = Meta.SI_NO,
    val cantidad: Int = 1,
    val unidad: String = ""
)

private val CATEGORIAS = listOf(
    Categoria("Esencial", R.drawable.ilu_essential, 0, listOf(
        Plantilla("Escribir el diario", "✍️"),
        Plantilla("Planificar el día", "🗒️"),
        Plantilla("Leer", "📖", Meta.TIEMPO, 20, "min")
    )),
    Categoria("Salud", R.drawable.ilu_health, 1, listOf(
        Plantilla("Beber agua", "💧", Meta.CANTIDAD, 8, "vasos"),
        Plantilla("Comer fruta", "🍎", Meta.CANTIDAD, 2, "piezas"),
        Plantilla("Dormir 8 horas", "😴")
    )),
    Categoria("Ejercicio", R.drawable.ilu_exercise, 2, listOf(
        Plantilla("Entrenar", "💪", Meta.TIEMPO, 30, "min"),
        Plantilla("Caminar", "🚶", Meta.CANTIDAD, 8000, "pasos"),
        Plantilla("Estirar", "🧘", Meta.TIEMPO, 10, "min")
    )),
    Categoria("Relaciones", R.drawable.ilu_relationship, 3, listOf(
        Plantilla("Llamar a la familia", "📞"),
        Plantilla("Quedar con alguien", "☕"),
        Plantilla("Escribir a un amigo", "💬")
    )),
    Categoria("Casa", R.drawable.ilu_home, 4, listOf(
        Plantilla("Ordenar 10 minutos", "🧹", Meta.TIEMPO, 10, "min"),
        Plantilla("Cocinar en casa", "🍳"),
        Plantilla("Hacer la cama", "🛏️")
    )),
    Categoria("Trabajo", R.drawable.ilu_work, 5, listOf(
        Plantilla("Trabajo profundo", "🎯", Meta.TIEMPO, 60, "min"),
        Plantilla("Vaciar la bandeja", "📥"),
        Plantilla("Estudiar", "📚", Meta.TIEMPO, 45, "min")
    )),
    Categoria("Mente", R.drawable.ilu_mindfulness, 2, listOf(
        Plantilla("Meditar", "🧘", Meta.TIEMPO, 10, "min"),
        Plantilla("Sin móvil una hora", "📵"),
        Plantilla("Anotar 3 cosas buenas", "🌟")
    )),
    Categoria("Dinero", R.drawable.ilu_finance, 1, listOf(
        Plantilla("Anotar gastos", "🧾"),
        Plantilla("Día sin gastar", "🚫"),
        Plantilla("Revisar el presupuesto", "📊")
    ))
)

/**
 * La pagina del buscador: arriba la busqueda, luego las categorias, y debajo
 * las actividades de la categoria elegida.
 */
@Composable
fun PantallaExplorar(onCrear: (Habito) -> Unit) {
    var busqueda by remember { mutableStateOf("") }
    var elegida by remember { mutableStateOf(0) }

    val buscando = busqueda.isNotBlank()
    val resultados = remember(busqueda, elegida) {
        if (buscando) {
            CATEGORIAS.flatMap { c -> c.actividades.map { c to it } }
                .filter { (_, p) -> p.nombre.contains(busqueda.trim(), ignoreCase = true) }
        } else {
            CATEGORIAS[elegida].actividades.map { CATEGORIAS[elegida] to it }
        }
    }

    fun crear(cat: Categoria, p: Plantilla) {
        onCrear(
            Habito(
                id = UUID.randomUUID().toString(),
                nombre = p.nombre,
                emoji = p.emoji,
                color = cat.color,
                creado = LocalDate.now().toString(),
                categoria = cat.nombre,
                meta = p.meta,
                metaCantidad = p.cantidad,
                unidad = p.unidad
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Buscador(valor = busqueda, onCambiar = { busqueda = it }) }

            item {
                Text(
                    text = "Explorar",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (!buscando) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                        CATEGORIAS.chunked(2).forEachIndexed { fila, par ->
                            Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                                par.forEachIndexed { col, cat ->
                                    val indice = fila * 2 + col
                                    TarjetaCategoria(
                                        categoria = cat,
                                        activa = indice == elegida,
                                        modifier = Modifier.weight(1f),
                                        onClick = { elegida = indice }
                                    )
                                }
                                if (par.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = if (buscando) "${resultados.size} resultados"
                    else "Actividades de ${CATEGORIAS[elegida].nombre}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            items(resultados) { (cat, p) ->
                FilaActividad(categoria = cat, plantilla = p, onCrear = { crear(cat, p) })
            }

            if (resultados.isEmpty()) {
                item {
                    Text(
                        text = "Nada con ese nombre. Puedes crear el hábito a mano desde la pestaña Hoy.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun Buscador(valor: String, onCambiar: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = IconoLupa,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(17.dp)
        )
        Spacer(Modifier.width(10.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (valor.isEmpty()) {
                Text(
                    text = "Buscar actividad…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            BasicTextField(
                value = valor,
                onValueChange = onCambiar,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TarjetaCategoria(
    categoria: Categoria,
    activa: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (activa) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (activa) Color.White.copy(alpha = 0.9f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(categoria.ilustracion),
                    contentDescription = categoria.nombre,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.height(9.dp))
            Text(
                text = categoria.nombre,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (activa) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FilaActividad(categoria: Categoria, plantilla: Plantilla, onCrear: () -> Unit) {
    val color = PALETA[categoria.color % PALETA.size]
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCrear() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = plantilla.emoji, fontSize = 20.sp)
            }
            Spacer(Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plantilla.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = when (plantilla.meta) {
                        Meta.SI_NO -> "Todos los días"
                        Meta.TIEMPO -> "${plantilla.cantidad} min al día"
                        Meta.CANTIDAD -> "${plantilla.cantidad} ${plantilla.unidad} al día"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(2.dp, color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconoMas,
                    contentDescription = "Añadir",
                    tint = color,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

/** Lupa dibujada a mano, al estilo del resto de iconos. */
val IconoLupa: androidx.compose.ui.graphics.vector.ImageVector by lazy {
    vector {
        moveTo(11f, 3.5f)
        cubicTo(15.1f, 3.5f, 18.5f, 6.9f, 18.5f, 11f)
        cubicTo(18.5f, 15.1f, 15.1f, 18.5f, 11f, 18.5f)
        cubicTo(6.9f, 18.5f, 3.5f, 15.1f, 3.5f, 11f)
        cubicTo(3.5f, 6.9f, 6.9f, 3.5f, 11f, 3.5f)
        close()
        moveTo(11f, 6f)
        cubicTo(8.2f, 6f, 6f, 8.2f, 6f, 11f)
        cubicTo(6f, 13.8f, 8.2f, 16f, 11f, 16f)
        cubicTo(13.8f, 16f, 16f, 13.8f, 16f, 11f)
        cubicTo(16f, 8.2f, 13.8f, 6f, 11f, 6f)
        close()
        moveTo(16.6f, 15.2f)
        lineTo(21f, 19.6f)
        lineTo(19.6f, 21f)
        lineTo(15.2f, 16.6f)
        close()
    }
}

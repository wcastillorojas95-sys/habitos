package com.lucas.habitos

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale

/**
 * El despertador de la tarjeta principal, dibujado con Canvas.
 *
 * Mismo motivo que los avatares: en codigo se recolorea con el tema y no hay
 * que convertir SVG a VectorDrawable a ciegas.
 */
@Composable
fun DespertadorNaranja(modifier: Modifier = Modifier, naranja: Color = Color(0xFFF26A34)) {
    Canvas(modifier = modifier) {
        val k = size.minDimension / 130f
        scale(scaleX = k, scaleY = k, pivot = Offset.Zero) { dibujarDespertador(naranja) }
    }
}

private fun DrawScope.dibujarDespertador(naranja: Color) {
    val oscuro = Color(0xFFC9451A)
    val intenso = Color(0xFFE55A21)
    val esfera = Color(0xFFFFF4EC)

    // Sombra en el suelo.
    drawOval(
        color = Color(0xFFF7CDB6).copy(alpha = 0.6f),
        topLeft = Offset(42f, 109f),
        size = Size(88f, 14f)
    )

    // Campanas.
    listOf(Offset(60f, 30f), Offset(122f, 30f)).forEachIndexed { i, c ->
        rotate(degrees = if (i == 0) -30f else 30f, pivot = c) {
            drawOval(color = intenso, topLeft = Offset(c.x - 11f, c.y - 9f), size = Size(22f, 18f))
        }
    }

    // Patas.
    listOf(Offset(76f, 100f) to 20f, Offset(107f, 100f) to -20f).forEach { (p, ang) ->
        rotate(degrees = ang, pivot = p) {
            drawRoundRect(
                color = oscuro,
                topLeft = Offset(p.x - 4f, p.y),
                size = Size(8f, 17f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
        }
    }

    // Caja y esfera.
    drawCircle(color = naranja, radius = 41f, center = Offset(91f, 58f))
    drawCircle(color = esfera, radius = 33f, center = Offset(91f, 58f))
    drawCircle(
        color = Color(0xFFFBDFD0),
        radius = 33f,
        center = Offset(91f, 58f),
        style = Stroke(width = 2f)
    )

    // Marcas de las horas.
    repeat(12) { i ->
        rotate(degrees = i * 30f, pivot = Offset(91f, 58f)) {
            drawLine(
                color = oscuro.copy(alpha = if (i % 3 == 0) 0.9f else 0.35f),
                start = Offset(91f, 30f),
                end = Offset(91f, if (i % 3 == 0) 37f else 34f),
                strokeWidth = if (i % 3 == 0) 2.6f else 1.6f,
                cap = StrokeCap.Round
            )
        }
    }

    // Agujas marcando las 9:41, como en el mockup.
    drawLine(Color(0xFF151312), Offset(91f, 58f), Offset(91f, 39f), 3.4f, StrokeCap.Round)
    drawLine(Color(0xFF151312), Offset(91f, 58f), Offset(78f, 66f), 3.4f, StrokeCap.Round)
    drawCircle(color = naranja, radius = 3.6f, center = Offset(91f, 58f))
    drawCircle(color = intenso, radius = 5f, center = Offset(91f, 16f))

    // Las zetas de dormir y un par de destellos.
    dibujarZeta(Offset(20f, 46f), 16f, Color(0xFF1B1918))
    dibujarZeta(Offset(36f, 26f), 11f, Color(0xFF1B1918))
    dibujarZeta(Offset(48f, 12f), 7f, Color(0xFF8E827A))
    dibujarDestello(Offset(133f, 52f), 7f, Color(0xFFFFC44D))
    dibujarDestello(Offset(22f, 92f), 6f, Color(0xFFFFC44D))
    dibujarDestello(Offset(128f, 98f), 4.5f, naranja)
}

/** Una "Z" hecha con tres trazos, para el sueño. */
private fun DrawScope.dibujarZeta(pos: Offset, lado: Float, color: Color) {
    val g = lado / 6f
    drawPath(
        path = Path().apply {
            moveTo(pos.x, pos.y)
            lineTo(pos.x + lado, pos.y)
            lineTo(pos.x, pos.y + lado)
            lineTo(pos.x + lado, pos.y + lado)
        },
        color = color,
        style = Stroke(width = g, cap = StrokeCap.Round)
    )
}

/** Destello de cuatro puntas. */
private fun DrawScope.dibujarDestello(centro: Offset, radio: Float, color: Color) {
    val r = radio
    val d = radio * 0.32f
    drawPath(
        path = Path().apply {
            moveTo(centro.x, centro.y - r)
            cubicTo(centro.x + d, centro.y - d, centro.x + d, centro.y - d, centro.x + r, centro.y)
            cubicTo(centro.x + d, centro.y + d, centro.x + d, centro.y + d, centro.x, centro.y + r)
            cubicTo(centro.x - d, centro.y + d, centro.x - d, centro.y + d, centro.x - r, centro.y)
            cubicTo(centro.x - d, centro.y - d, centro.x - d, centro.y - d, centro.x, centro.y - r)
            close()
        },
        color = color
    )
}

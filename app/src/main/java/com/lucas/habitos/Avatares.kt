package com.lucas.habitos

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Retratos vectoriales dibujados con Canvas de Compose.
 *
 * Van en codigo y no como SVG por dos razones: VectorDrawable no admite
 * circulos ni elipses (solo paths), asi que habria que convertirlo todo a mano
 * y a ciegas; y dibujando aqui se recolorean solos con el tema.
 *
 * El diseno esta pensado para tamaños pequeños: lo que distingue a cada persona
 * es la silueta del pelo y el color de la ropa, no la cara. A 24 dp los rasgos
 * desaparecen y aun asi se sabe quien es quien.
 */

private data class Retrato(
    val fondo: List<Color>,
    val piel: Color,
    val sombra: Color,
    val ropa: Color,
    val pelo: Color,
    val estilo: Int      // 0 rizado · 1 moño · 2 barba · 3 melena · 4 pañuelo · 5 gorro
)

private val RETRATOS = listOf(
    Retrato(listOf(Color(0xFFFFE7D6), Color(0xFFFCD2B2)), Color(0xFFE8B183), Color(0xFFCE9463), Color(0xFF2F4A7A), Color(0xFF2B1B13), 0),
    Retrato(listOf(Color(0xFFEDE8FC), Color(0xFFD6CCF8)), Color(0xFFF2C9A0), Color(0xFFDBA97F), Color(0xFFF26A34), Color(0xFF3A2416), 1),
    Retrato(listOf(Color(0xFFDEF3EA), Color(0xFFC2E8D8)), Color(0xFF96603D), Color(0xFF7C4C2C), Color(0xFF2FA575), Color(0xFF241812), 2),
    Retrato(listOf(Color(0xFFFBE2E8), Color(0xFFF3C4D1)), Color(0xFFF2C9A0), Color(0xFFDBA97F), Color(0xFFF6EFE7), Color(0xFF4A2C18), 3),
    Retrato(listOf(Color(0xFFFFF0DC), Color(0xFFFBD9AE)), Color(0xFFC98A5E), Color(0xFFAC7047), Color(0xFFC9451A), Color(0xFFE86A3E), 4),
    Retrato(listOf(Color(0xFFE4EEFB), Color(0xFFC9DCF6)), Color(0xFFF2C9A0), Color(0xFFDBA97F), Color(0xFF5A6B85), Color(0xFF6B4A2E), 0),
    Retrato(listOf(Color(0xFFFFE0D2), Color(0xFFFFC2A6)), Color(0xFF6E4127), Color(0xFF57311B), Color(0xFFFFFFFF), Color(0xFF1F1410), 3),
    Retrato(listOf(Color(0xFFF0EDE8), Color(0xFFDCD5CC)), Color(0xFFE8B183), Color(0xFFCE9463), Color(0xFF8C7BE0), Color(0xFFF26A34), 5)
)

/**
 * Dibuja el retrato numero [indice] (se repite en ciclo si te pasas).
 * El lienzo se normaliza a 80x80 para que las coordenadas no dependan del tamaño.
 */
@Composable
fun Avatar(indice: Int, modifier: Modifier = Modifier) {
    val r = RETRATOS[((indice % RETRATOS.size) + RETRATOS.size) % RETRATOS.size]
    Canvas(modifier = modifier) {
        val k = size.minDimension / 80f
        scale(scaleX = k, scaleY = k, pivot = Offset.Zero) {
            dibujarRetrato(r)
        }
    }
}

private fun DrawScope.dibujarRetrato(r: Retrato) {
    val recorte = Path().apply { addOval(Rect(Offset(0f, 0f), Size(80f, 80f))) }

    clipPath(recorte) {
        drawRect(brush = Brush.verticalGradient(r.fondo), size = Size(80f, 80f))

        // Cuello, y su sombra bajo la mandibula.
        drawRoundRectSimple(32.4f, 39f, 15.2f, 20f, r.sombra)
        drawOval(color = Color.Black.copy(alpha = 0.12f), topLeft = Offset(29.4f, 38.2f), size = Size(21.2f, 10.8f))

        // Hombros.
        drawPath(
            path = Path().apply {
                moveTo(-2f, 80f)
                cubicTo(0f, 66f, 16f, 56.4f, 40f, 56.4f)
                cubicTo(64f, 56.4f, 80f, 66f, 82f, 80f)
                close()
            },
            color = r.ropa
        )

        // Cabeza y orejas.
        drawOval(color = r.piel, topLeft = Offset(24.5f, 15.5f), size = Size(31f, 35f))
        drawOval(color = r.piel, topLeft = Offset(21.4f, 30.9f), size = Size(6.4f, 8.6f))
        drawOval(color = r.piel, topLeft = Offset(52.2f, 30.9f), size = Size(6.4f, 8.6f))

        dibujarPelo(r)

        if (r.estilo != 4) dibujarCara(r) else {
            // El pañuelo tapa el pelo y las orejas: la cara se dibuja encima.
            drawOval(color = r.piel, topLeft = Offset(27.7f, 18.6f), size = Size(24.6f, 29.2f))
            dibujarCara(r)
        }

        drawCircle(color = Color.Black.copy(alpha = 0.06f), radius = 39f, center = Offset(40f, 40f), style = Stroke(width = 2f))
    }
}

private fun DrawScope.dibujarCara(r: Retrato) {
    // Ojos con su brillo.
    drawOval(color = Color(0xFF2A1C14), topLeft = Offset(32.35f, 31.45f), size = Size(3.9f, 4.3f))
    drawOval(color = Color(0xFF2A1C14), topLeft = Offset(43.75f, 31.45f), size = Size(3.9f, 4.3f))
    drawCircle(color = Color.White.copy(alpha = 0.85f), radius = 0.6f, center = Offset(34.9f, 32.9f))
    drawCircle(color = Color.White.copy(alpha = 0.85f), radius = 0.6f, center = Offset(46.3f, 32.9f))

    // Nariz y boca, muy tenues: a tamaño pequeño solo aportan textura.
    drawPath(
        path = Path().apply { moveTo(40.3f, 35.6f); cubicTo(41f, 37.6f, 40.8f, 38.9f, 38.8f, 39.3f) },
        color = r.sombra,
        style = Stroke(width = 1.5f, cap = StrokeCap.Round)
    )
    drawPath(
        path = Path().apply { moveTo(36.5f, 42.4f); cubicTo(38.8f, 44.1f, 41.2f, 44.1f, 43.5f, 42.4f) },
        color = r.sombra,
        style = Stroke(width = 1.8f, cap = StrokeCap.Round)
    )
}

private fun DrawScope.dibujarPelo(r: Retrato) {
    when (r.estilo) {
        // Rizado corto: casquete mas bultos en el contorno.
        0 -> {
            drawPath(casquete(), r.pelo)
            listOf(
                Triple(27.6f, 21.4f, 4.7f), Triple(34.6f, 16.6f, 5.1f), Triple(43.6f, 15.8f, 5.3f),
                Triple(51.2f, 20.2f, 4.9f), Triple(55.2f, 27.4f, 4.1f), Triple(24.9f, 28f, 4.1f)
            ).forEach { (x, y, rad) -> drawCircle(r.pelo, rad, Offset(x, y)) }
        }
        // Moño alto.
        1 -> {
            drawPath(casquete(), r.pelo)
            drawPath(
                Path().apply {
                    moveTo(32.6f, 15.8f); cubicTo(34.5f, 12.2f, 37f, 10.4f, 40f, 10.4f)
                    cubicTo(43f, 10.4f, 45.5f, 12.2f, 47.4f, 15.8f); close()
                },
                r.pelo
            )
            drawCircle(r.pelo, 7.4f, Offset(40f, 10f))
        }
        // Barba corta.
        2 -> {
            drawPath(casquete(), r.pelo)
            drawPath(
                Path().apply {
                    moveTo(25.2f, 33.8f)
                    cubicTo(26f, 45f, 32f, 53.4f, 40f, 53.4f)
                    cubicTo(48f, 53.4f, 54f, 45f, 54.8f, 33.8f)
                    cubicTo(53.4f, 39f, 50f, 41.6f, 40f, 41.6f)
                    cubicTo(30f, 41.6f, 26.6f, 39f, 25.2f, 33.8f)
                    close()
                },
                r.pelo
            )
        }
        // Melena: primero el pelo de atras, luego el flequillo.
        3 -> {
            drawPath(
                Path().apply {
                    moveTo(17.6f, 78f)
                    cubicTo(13.4f, 63.2f, 14.4f, 47.6f, 19.2f, 37.6f)
                    cubicTo(23.6f, 27f, 30.8f, 21f, 40f, 21f)
                    cubicTo(49.2f, 21f, 56.4f, 27f, 60.8f, 37.6f)
                    cubicTo(65.6f, 47.6f, 66.6f, 63.2f, 62.4f, 78f)
                    cubicTo(58.8f, 76.3f, 57.2f, 71.8f, 56.4f, 65.2f)
                    cubicTo(55.5f, 57.5f, 55.4f, 48.5f, 53.8f, 41.9f)
                    cubicTo(49.5f, 45.8f, 44.7f, 47.6f, 40f, 47.6f)
                    cubicTo(35.3f, 47.6f, 30.5f, 45.8f, 26.2f, 41.9f)
                    cubicTo(24.6f, 48.5f, 24.5f, 57.5f, 23.6f, 65.2f)
                    cubicTo(22.8f, 71.8f, 21.2f, 76.3f, 17.6f, 78f)
                    close()
                },
                r.pelo
            )
            drawPath(casquete(), r.pelo)
        }
        // Pañuelo: cubre cabeza, cuello y hombros de una pieza.
        4 -> {
            drawPath(
                Path().apply {
                    moveTo(40f, 9.6f)
                    cubicTo(28.4f, 9.6f, 20.8f, 18.4f, 20.8f, 31f)
                    cubicTo(20.8f, 36.3f, 22.2f, 41f, 24.1f, 44.7f)
                    cubicTo(16.3f, 47.7f, 11.4f, 53.5f, 10f, 62.7f)
                    lineTo(9f, 80f); lineTo(71f, 80f); lineTo(70f, 62.7f)
                    cubicTo(68.6f, 53.5f, 63.7f, 47.7f, 55.9f, 44.7f)
                    cubicTo(57.8f, 41f, 59.2f, 36.3f, 59.2f, 31f)
                    cubicTo(59.2f, 18.4f, 51.6f, 9.6f, 40f, 9.6f)
                    close()
                },
                r.pelo
            )
        }
        // Gorro de lana.
        5 -> {
            drawCircle(Color(0xFFFFC44D), 5f, Offset(40f, 6.4f))
            drawPath(
                Path().apply {
                    moveTo(21.6f, 26f)
                    cubicTo(21.6f, 13.6f, 29.8f, 4.8f, 40f, 4.8f)
                    cubicTo(50.2f, 4.8f, 58.4f, 13.6f, 58.4f, 26f)
                    close()
                },
                r.pelo
            )
            drawRoundRectSimple(20f, 21f, 40f, 7.8f, Color(0xFFD9541F), 3.9f)
        }
    }
}

/** Casquete de pelo que cubre la parte alta de la cabeza. */
private fun casquete(): Path = Path().apply {
    moveTo(23.8f, 36f)
    cubicTo(22.2f, 22f, 29.8f, 12.8f, 40f, 12.8f)
    cubicTo(50.2f, 12.8f, 57.8f, 22f, 56.2f, 36f)
    cubicTo(55f, 29f, 53.4f, 25.6f, 51.3f, 24f)
    cubicTo(48f, 26.4f, 43.6f, 27.3f, 40f, 27.3f)
    cubicTo(36.4f, 27.3f, 32f, 26.4f, 28.7f, 24f)
    cubicTo(26.6f, 25.6f, 25f, 29f, 23.8f, 36f)
    close()
}

private fun DrawScope.drawRoundRectSimple(
    x: Float, y: Float, ancho: Float, alto: Float, color: Color, radio: Float = 0f
) {
    drawRoundRect(
        color = color,
        topLeft = Offset(x, y),
        size = Size(ancho, alto),
        cornerRadius = CornerRadius(radio, radio)
    )
}

/** Fila de avatares superpuestos, como en el mockup. */
@Composable
fun PilaAvatares(cuantos: Int, tamano: Dp, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(-(tamano / 3))
    ) {
        repeat(cuantos) { i -> Avatar(indice = i, modifier = Modifier.size(tamano)) }
    }
}

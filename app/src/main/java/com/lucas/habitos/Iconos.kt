package com.lucas.habitos

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Iconos dibujados a mano.
 *
 * Evita depender de la librería de iconos de Material: son cuatro trazos y así
 * la app carga menos peso y no hay que sincronizar versiones.
 */
fun vector(bloque: PathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) { bloque() }
    }.build()

val IconoMas: ImageVector by lazy {
    vector {
        moveTo(10.9f, 4f); lineTo(13.1f, 4f); lineTo(13.1f, 10.9f); lineTo(20f, 10.9f)
        lineTo(20f, 13.1f); lineTo(13.1f, 13.1f); lineTo(13.1f, 20f); lineTo(10.9f, 20f)
        lineTo(10.9f, 13.1f); lineTo(4f, 13.1f); lineTo(4f, 10.9f); lineTo(10.9f, 10.9f)
        close()
    }
}

val IconoMenos: ImageVector by lazy {
    vector {
        moveTo(4f, 10.9f); lineTo(20f, 10.9f); lineTo(20f, 13.1f); lineTo(4f, 13.1f); close()
    }
}

val IconoAtras: ImageVector by lazy {
    vector {
        moveTo(11.2f, 3.8f); lineTo(12.8f, 5.4f); lineTo(7.2f, 10.9f); lineTo(20f, 10.9f)
        lineTo(20f, 13.1f); lineTo(7.2f, 13.1f); lineTo(12.8f, 18.6f); lineTo(11.2f, 20.2f)
        lineTo(3f, 12f)
        close()
    }
}

val IconoCheck: ImageVector by lazy {
    vector {
        moveTo(9f, 16.2f); lineTo(4.8f, 12f); lineTo(3.4f, 13.4f); lineTo(9f, 19f)
        lineTo(21f, 7f); lineTo(19.6f, 5.6f)
        close()
    }
}

val IconoBasura: ImageVector by lazy {
    vector {
        moveTo(9f, 3f); lineTo(15f, 3f); lineTo(16f, 5f); lineTo(20f, 5f); lineTo(20f, 7f)
        lineTo(4f, 7f); lineTo(4f, 5f); lineTo(8f, 5f)
        close()
        moveTo(6f, 8.5f); lineTo(18f, 8.5f); lineTo(17f, 21f); lineTo(7f, 21f)
        close()
    }
}

val IconoLapiz: ImageVector by lazy {
    vector {
        moveTo(3f, 17.2f); lineTo(3f, 21f); lineTo(6.8f, 21f); lineTo(17.5f, 10.3f)
        lineTo(13.7f, 6.5f)
        close()
        moveTo(20.7f, 7.1f); lineTo(17.9f, 4.3f); lineTo(15.5f, 4.7f); lineTo(19.3f, 8.5f)
        close()
    }
}

val IconoLuna: ImageVector by lazy {
    vector {
        moveTo(13f, 3f)
        lineTo(10.5f, 4.5f); lineTo(9f, 7.5f); lineTo(9f, 11f); lineTo(10.5f, 14f)
        lineTo(13.5f, 16f); lineTo(17f, 16.5f); lineTo(20f, 15.5f)
        lineTo(18f, 19f); lineTo(14f, 21f); lineTo(9f, 20.5f); lineTo(5.5f, 17.5f)
        lineTo(4f, 13f); lineTo(5f, 8f); lineTo(8f, 4.5f)
        close()
    }
}

package com.lucas.habitos

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Poppins empaquetada en la app: geométrica, redonda, nada que ver con la fuente por defecto. */
val Poppins = FontFamily(
    Font(R.font.poppins_light, FontWeight.Light),
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

/**
 * Escala tipográfica propia. Las cifras grandes van en Light con espaciado
 * negativo (se ven más limpias); los textos pequeños en Medium con espaciado
 * positivo (se leen mejor a tamaño chico).
 */
private val TipografiaHabitos = Typography(
    displayLarge = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Light,
        fontSize = 52.sp, lineHeight = 56.sp, letterSpacing = (-1.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Light,
        fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-1).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.6).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Bold,
        fontSize = 23.sp, lineHeight = 30.sp, letterSpacing = (-0.4).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Medium,
        fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = (-0.2).sp
    ),
    titleLarge = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Bold,
        fontSize = 19.sp, lineHeight = 25.sp, letterSpacing = (-0.3).sp
    ),
    titleMedium = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Medium,
        fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = (-0.1).sp
    ),
    titleSmall = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 19.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp, lineHeight = 19.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.1.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Medium,
        fontSize = 13.sp, lineHeight = 17.sp, letterSpacing = 0.2.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Medium,
        fontSize = 11.5.sp, lineHeight = 15.sp, letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Medium,
        fontSize = 10.5.sp, lineHeight = 14.sp, letterSpacing = 0.6.sp
    )
)

/**
 * Los seis colores que el usuario puede elegir para cada hábito.
 *
 * Reordenados para que el naranja de la marca sea el primero: es el que sale
 * por defecto al crear un hábito nuevo, así que la app se ve coherente sin que
 * nadie tenga que elegir nada.
 */
val PALETA = listOf(
    Color(0xFFF26A34), // naranja de marca
    Color(0xFF37BE87), // verde
    Color(0xFF8C7BE0), // lavanda
    Color(0xFF3E7BD6), // azul
    Color(0xFFE8A33C), // ámbar
    Color(0xFFE0607F)  // rosa
)

private val Claro = lightColorScheme(
    primary = Color(0xFFE0561F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFDE4D6),
    onPrimaryContainer = Color(0xFF4A1904),
    secondary = Color(0xFF7A5C4E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFBDFD0),
    onSecondaryContainer = Color(0xFF2C1710),
    background = Color(0xFFFDECE2),
    onBackground = Color(0xFF1F1A17),
    surface = Color(0xFFFFF7F2),
    onSurface = Color(0xFF1F1A17),
    surfaceVariant = Color(0xFFFBDFD0),
    onSurfaceVariant = Color(0xFF7A6B62),
    outline = Color(0xFFDCC3B4),
    outlineVariant = Color(0xFFEFDACB),
    error = Color(0xFFC0392B)
)

private val Oscuro = darkColorScheme(
    primary = Color(0xFFFF9668),
    onPrimary = Color(0xFF4A1904),
    primaryContainer = Color(0xFF8A3410),
    onPrimaryContainer = Color(0xFFFDE4D6),
    secondary = Color(0xFFE4BFAB),
    onSecondary = Color(0xFF422A1D),
    secondaryContainer = Color(0xFF5B4032),
    onSecondaryContainer = Color(0xFFFBDFD0),
    background = Color(0xFF17110E),
    onBackground = Color(0xFFF0E5DE),
    surface = Color(0xFF17110E),
    onSurface = Color(0xFFF0E5DE),
    surfaceVariant = Color(0xFF2C221D),
    onSurfaceVariant = Color(0xFFB8A79C),
    outline = Color(0xFF4A3C34),
    outlineVariant = Color(0xFF332822),
    error = Color(0xFFFF8A80)
)

@Composable
fun HabitosTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Oscuro else Claro,
        typography = TipografiaHabitos,
        content = content
    )
}

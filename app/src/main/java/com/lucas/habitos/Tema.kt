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

/** Los seis colores que el usuario puede elegir para cada hábito. */
val PALETA = listOf(
    Color(0xFF00A870),
    Color(0xFF2D7FF9),
    Color(0xFFF08C2E),
    Color(0xFFE5484D),
    Color(0xFF8E4EC6),
    Color(0xFF00A2C7)
)

private val Claro = lightColorScheme(
    primary = Color(0xFF00875A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3F3E3),
    onPrimaryContainer = Color(0xFF00291B),
    secondary = Color(0xFF4A6358),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE9E1),
    onSecondaryContainer = Color(0xFF0A1F16),
    background = Color(0xFFFAFBFA),
    onBackground = Color(0xFF121714),
    surface = Color(0xFFFAFBFA),
    onSurface = Color(0xFF121714),
    surfaceVariant = Color(0xFFEDF1EE),
    onSurfaceVariant = Color(0xFF5A635E),
    outline = Color(0xFFC3CBC6),
    outlineVariant = Color(0xFFDDE4E0),
    error = Color(0xFFC0392B)
)

private val Oscuro = darkColorScheme(
    primary = Color(0xFF5BD6A0),
    onPrimary = Color(0xFF003823),
    primaryContainer = Color(0xFF00553A),
    onPrimaryContainer = Color(0xFFD3F3E3),
    secondary = Color(0xFFB2CCC0),
    onSecondary = Color(0xFF1D352B),
    secondaryContainer = Color(0xFF334B41),
    onSecondaryContainer = Color(0xFFDCE9E1),
    background = Color(0xFF0E1211),
    onBackground = Color(0xFFE2E6E3),
    surface = Color(0xFF0E1211),
    onSurface = Color(0xFFE2E6E3),
    surfaceVariant = Color(0xFF1C2321),
    onSurfaceVariant = Color(0xFF9FA9A4),
    outline = Color(0xFF3A433F),
    outlineVariant = Color(0xFF2A322E),
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

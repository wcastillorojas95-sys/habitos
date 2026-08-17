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

/** MK Saans, la tipografía de Lucas. Cinco pesos empaquetados en la app. */
val Saans = FontFamily(
    Font(R.font.mksaans_light, FontWeight.Light),
    Font(R.font.mksaans_regular, FontWeight.Normal),
    Font(R.font.mksaans_medium, FontWeight.Medium),
    Font(R.font.mksaans_semibold, FontWeight.SemiBold),
    Font(R.font.mksaans_bold, FontWeight.Bold)
)

/**
 * Escala tipográfica propia. Las cifras grandes van en Light con espaciado
 * negativo (se ven más limpias); los textos pequeños en Medium con espaciado
 * positivo (se leen mejor a tamaño chico).
 */
private val TipografiaHabitos = Typography(
    displayLarge = TextStyle(
        fontFamily = Saans, fontWeight = FontWeight.Light,
        fontSize = 52.sp, lineHeight = 56.sp, letterSpacing = (-1.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = Saans, fontWeight = FontWeight.Light,
        fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-1).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = Saans, fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.6).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Saans, fontWeight = FontWeight.SemiBold,
        fontSize = 23.sp, lineHeight = 30.sp, letterSpacing = (-0.4).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Saans, fontWeight = FontWeight.Medium,
        fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = (-0.2).sp
    ),
    titleLarge = TextStyle(
        fontFamily = Saans, fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp, lineHeight = 25.sp, letterSpacing = (-0.3).sp
    ),
    titleMedium = TextStyle(
        fontFamily = Saans, fontWeight = FontWeight.Medium,
        fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = (-0.1).sp
    ),
    titleSmall = TextStyle(
        fontFamily = Saans, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 19.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Saans, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Saans, fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp, lineHeight = 19.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Saans, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.1.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Saans, fontWeight = FontWeight.Medium,
        fontSize = 13.sp, lineHeight = 17.sp, letterSpacing = 0.2.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Saans, fontWeight = FontWeight.Medium,
        fontSize = 11.5.sp, lineHeight = 15.sp, letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Saans, fontWeight = FontWeight.Medium,
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
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFE1D0),
    onPrimaryContainer = Color(0xFF3D1400),
    secondary = Color(0xFF7A5C4E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFBDFD0),
    onSecondaryContainer = Color(0xFF2C1710),
    background = Color(0xFFFDECE2),
    onBackground = Color(0xFF241C17),
    surface = Color(0xFFFFF8F4),
    onSurface = Color(0xFF241C17),
    surfaceVariant = Color(0xFFF6DFD2),
    onSurfaceVariant = Color(0xFF6B5951),
    outline = Color(0xFFC9AE9F),
    outlineVariant = Color(0xFFEBD5C7),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF)
)

private val Oscuro = darkColorScheme(
    primary = Color(0xFFFFB08C),
    onPrimary = Color(0xFF3D1400),
    primaryContainer = Color(0xFF7A2E0C),
    onPrimaryContainer = Color(0xFFFFE1D0),
    secondary = Color(0xFFE7BFAA),
    onSecondary = Color(0xFF422A1D),
    secondaryContainer = Color(0xFF5B4032),
    onSecondaryContainer = Color(0xFFFBDFD0),
    background = Color(0xFF15100D),
    onBackground = Color(0xFFF5EAE3),
    surface = Color(0xFF221A15),
    onSurface = Color(0xFFF5EAE3),
    surfaceVariant = Color(0xFF302520),
    onSurfaceVariant = Color(0xFFC9B6AB),
    outline = Color(0xFF5C4A41),
    outlineVariant = Color(0xFF3A2D27),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

/**
 * Tema de la app.
 *
 * [oscuro] lo decide el ajuste, no el sistema: Lucas quiere claro por defecto
 * aunque el móvil esté en oscuro. Pasando null se respeta el ajuste del sistema.
 */
@Composable
fun HabitosTheme(oscuro: Boolean? = false, content: @Composable () -> Unit) {
    val esOscuro = oscuro ?: isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (esOscuro) Oscuro else Claro,
        typography = TipografiaHabitos,
        content = content
    )
}

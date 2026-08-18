package com.lucas.habitos

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Dos tipografías, cada una en lo suyo.
 *
 * Bricolage Grotesque tiene mucho carácter en los tamaños grandes —las letras se
 * estrechan y aprietan al crecer— pero eso mismo la hace cansada en un párrafo.
 * DM Sans es lo contrario: neutra, de formas abiertas, hecha para leerse pequeña.
 * Mezclarlas da personalidad arriba y legibilidad abajo, que es lo que se busca.
 *
 * Ambas son de Google Fonts con licencia SIL Open Font, así que se pueden
 * empaquetar y distribuir sin pedir permiso a nadie.
 */
val Titulos = FontFamily(
    Font(R.font.bricolage_regular, FontWeight.Normal),
    Font(R.font.bricolage_medium, FontWeight.Medium),
    Font(R.font.bricolage_semibold, FontWeight.SemiBold),
    Font(R.font.bricolage_bold, FontWeight.Bold),
    Font(R.font.bricolage_extrabold, FontWeight.ExtraBold)
)

val Cuerpo = FontFamily(
    Font(R.font.dmsans_regular, FontWeight.Normal),
    Font(R.font.dmsans_medium, FontWeight.Medium),
    Font(R.font.dmsans_semibold, FontWeight.SemiBold),
    Font(R.font.dmsans_bold, FontWeight.Bold),
    Font(R.font.dmsans_extrabold, FontWeight.ExtraBold)
)

/**
 * Escala tipográfica propia.
 *
 * De display a title manda Bricolage, con espaciado negativo: al apretarla un
 * poco se nota que es ella y no una grotesca cualquiera. De body a label manda
 * DM Sans con espaciado ligeramente positivo, que a doce puntos se agradece.
 *
 * Los pesos base subieron respecto a la versión anterior porque Bricolage pinta
 * más fina que MK Saans al mismo peso; con Light se veía desvaída.
 */
private val TipografiaHabitos = Typography(
    displayLarge = TextStyle(
        fontFamily = Titulos, fontWeight = FontWeight.Bold,
        fontSize = 50.sp, lineHeight = 54.sp, letterSpacing = (-1.6).sp
    ),
    displayMedium = TextStyle(
        fontFamily = Titulos, fontWeight = FontWeight.Bold,
        fontSize = 38.sp, lineHeight = 42.sp, letterSpacing = (-1.2).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = Titulos, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.8).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Titulos, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.6).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Titulos, fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = (-0.4).sp
    ),
    titleLarge = TextStyle(
        fontFamily = Titulos, fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp, lineHeight = 25.sp, letterSpacing = (-0.4).sp
    ),
    titleMedium = TextStyle(
        fontFamily = Titulos, fontWeight = FontWeight.SemiBold,
        fontSize = 16.5.sp, lineHeight = 22.sp, letterSpacing = (-0.2).sp
    ),
    titleSmall = TextStyle(
        fontFamily = Titulos, fontWeight = FontWeight.SemiBold,
        fontSize = 14.5.sp, lineHeight = 19.sp, letterSpacing = (-0.1).sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Cuerpo, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Cuerpo, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 19.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Cuerpo, fontWeight = FontWeight.Normal,
        fontSize = 12.5.sp, lineHeight = 16.sp, letterSpacing = 0.1.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Cuerpo, fontWeight = FontWeight.Medium,
        fontSize = 13.sp, lineHeight = 17.sp, letterSpacing = 0.2.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Cuerpo, fontWeight = FontWeight.Medium,
        fontSize = 11.5.sp, lineHeight = 15.sp, letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Cuerpo, fontWeight = FontWeight.SemiBold,
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
    val esquema = if (esOscuro) Oscuro else Claro
    MaterialTheme(
        colorScheme = esquema,
        typography = TipografiaHabitos
    ) {
        // Este Surface no es decoración: sin él, LocalContentColor se queda en el
        // negro puro que trae Compose por defecto, y todo el texto o icono que no
        // lleve color explícito sale negro. En claro desentona con el marrón de la
        // paleta; en oscuro es texto negro sobre fondo negro, ilegible.
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = esquema.background,
            contentColor = esquema.onBackground,
            content = content
        )
    }
}

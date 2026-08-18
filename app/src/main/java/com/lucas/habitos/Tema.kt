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
 * Subidos de saturación respecto a la versión anterior: los tonos apagados se
 * defendían bien sobre el fondo crema, pero sobre el fondo casi negro del modo
 * oscuro se apelmazaban unos con otros. Estos aguantan los dos fondos.
 */
val PALETA = listOf(
    Color(0xFFF2542D), // naranja de marca
    Color(0xFF23C08B), // verde
    Color(0xFF8B7BF0), // lavanda
    Color(0xFF3B7BE8), // azul
    Color(0xFFFFB020), // ámbar
    Color(0xFFF4635E)  // coral
)

/** El verde de "día cumplido", el mismo en los dos temas. */
val VerdeCumplido = Color(0xFF23C08B)

/**
 * Modo claro: papel cálido de fondo, tarjetas blancas puras encima.
 *
 * El contraste entre el crema del fondo y el blanco de las tarjetas es lo que
 * hace que las tarjetas floten sin necesidad de sombras.
 */
private val Claro = lightColorScheme(
    primary = Color(0xFFF2542D),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFE0D3),
    onPrimaryContainer = Color(0xFF4A1505),
    secondary = Color(0xFF3B7BE8),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD9E6FF),
    onSecondaryContainer = Color(0xFF0B2A5E),
    background = Color(0xFFF6F1E8),
    onBackground = Color(0xFF17150F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF17150F),
    surfaceVariant = Color(0xFFEDE6D9),
    onSurfaceVariant = Color(0xFF6E6656),
    outline = Color(0xFFCFC5B4),
    outlineVariant = Color(0xFFE6DED0),
    // La píldora de navegación y cualquier cosa que deba ir "al revés".
    inverseSurface = Color(0xFF17150F),
    inverseOnSurface = Color(0xFFFFFFFF),
    error = Color(0xFFD93A34),
    onError = Color(0xFFFFFFFF)
)

/**
 * Modo oscuro: gris neutro, no marrón.
 *
 * El anterior tiraba a marrón en todos sus niveles y el resultado era opaco:
 * fondo, tarjeta y borde quedaban tan cerca en luminosidad que no se distinguían.
 * Ahora los tres escalones están separados y son neutros, así el color de cada
 * hábito es lo único que tiñe la pantalla.
 */
private val Oscuro = darkColorScheme(
    primary = Color(0xFFFF7A4D),
    onPrimary = Color(0xFF3A1200),
    primaryContainer = Color(0xFF5A230D),
    onPrimaryContainer = Color(0xFFFFD9C7),
    secondary = Color(0xFF7FB0FF),
    onSecondary = Color(0xFF0B2A5E),
    secondaryContainer = Color(0xFF1E3A6B),
    onSecondaryContainer = Color(0xFFD9E6FF),
    background = Color(0xFF0E0E11),
    onBackground = Color(0xFFF4F2ED),
    surface = Color(0xFF1A1A1F),
    onSurface = Color(0xFFF4F2ED),
    surfaceVariant = Color(0xFF272730),
    onSurfaceVariant = Color(0xFFAAA69E),
    outline = Color(0xFF44444E),
    outlineVariant = Color(0xFF2E2E36),
    // En oscuro la píldora tiene que ser MÁS clara que el fondo, no más oscura:
    // negra sobre casi negro desaparecía.
    inverseSurface = Color(0xFF2A2A33),
    inverseOnSurface = Color(0xFFF4F2ED),
    error = Color(0xFFFF6B62),
    onError = Color(0xFF3A0704)
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

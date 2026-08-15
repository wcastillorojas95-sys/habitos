package com.lucas.habitos

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val ClaroVerde = lightColorScheme(
    primary = Color(0xFF1F6F4A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA8F2C6),
    onPrimaryContainer = Color(0xFF002110),
    secondary = Color(0xFF4E6355),
    background = Color(0xFFF7FBF6),
    surface = Color(0xFFF7FBF6),
    surfaceVariant = Color(0xFFDCE5DC),
    onSurfaceVariant = Color(0xFF414942)
)

private val OscuroVerde = darkColorScheme(
    primary = Color(0xFF8DD5AB),
    onPrimary = Color(0xFF00391F),
    primaryContainer = Color(0xFF005233),
    onPrimaryContainer = Color(0xFFA8F2C6),
    secondary = Color(0xFFB5CCBB),
    background = Color(0xFF101410),
    surface = Color(0xFF101410),
    surfaceVariant = Color(0xFF414942),
    onSurfaceVariant = Color(0xFFC0C9C0)
)

/**
 * En Android 12 y superiores toma los colores del fondo de pantalla del usuario.
 * En versiones anteriores usa la paleta verde de arriba.
 */
@Composable
fun HabitosTheme(content: @Composable () -> Unit) {
    val oscuro = isSystemInDarkTheme()
    val esquema = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val contexto = LocalContext.current
        if (oscuro) dynamicDarkColorScheme(contexto) else dynamicLightColorScheme(contexto)
    } else {
        if (oscuro) OscuroVerde else ClaroVerde
    }

    MaterialTheme(colorScheme = esquema, content = content)
}

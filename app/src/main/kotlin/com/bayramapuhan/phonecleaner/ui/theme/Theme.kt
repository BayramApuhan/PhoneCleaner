package com.bayramapuhan.phonecleaner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.bayramapuhan.phonecleaner.domain.model.ThemeMode

private val LightColors = lightColorScheme(
    primary = PrimaryCyan,
    onPrimary = Color.White,
    primaryContainer = PrimaryCyanLight,
    onPrimaryContainer = Color(0xFF003544),
    secondary = SecondaryAmber,
    onSecondary = Color.Black,
    tertiary = Color(0xFF8B5CF6),
    background = LightBackground,
    onBackground = Color(0xFF0F1419),
    surface = LightSurface,
    onSurface = Color(0xFF0F1419),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    error = ErrorRed,
)

private val DarkColors = darkColorScheme(
    primary = PrimaryCyan,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0E5266),
    onPrimaryContainer = PrimaryCyanLight,
    secondary = SecondaryAmber,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF7C4A00),
    onSecondaryContainer = Color(0xFFFFE0B0),
    tertiary = Color(0xFFA78BFA),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = ErrorRed,
    errorContainer = Color(0xFF5A1212),
    onErrorContainer = Color(0xFFFECACA),
)

@Composable
fun PhoneCleanerTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}

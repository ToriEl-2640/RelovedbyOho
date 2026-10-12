package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = TerracottaDarkPrimary,
    onPrimary = Color(0xFF261208),
    primaryContainer = Color(0xFF4D2412),
    onPrimaryContainer = Color(0xFFF9EAE1),
    secondary = OhoSage,
    onSecondary = Color(0xFF1E281A),
    secondaryContainer = Color(0xFF333E30),
    onSecondaryContainer = Color(0xFFE8EFE5),
    tertiary = OhoCream,
    onTertiary = OhoCharcoal,
    tertiaryContainer = Color(0xFF362C22),
    onTertiaryContainer = Color(0xFFF7EFE4),
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkText,
    onSurface = DarkText,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorder.copy(alpha = 0.6f)
)

private fun createLightColorScheme(eyeComfortMode: Boolean) = lightColorScheme(
    primary = if (eyeComfortMode) ComfortPrimary else OhoOrange,
    onPrimary = Color.White,
    primaryContainer = TerracottaPrimaryLight,
    onPrimaryContainer = Color(0xFF5A220C),
    secondary = OhoSage,
    onSecondary = Color.White,
    secondaryContainer = SageSecondaryContainer,
    onSecondaryContainer = SageDark,
    tertiary = OhoCream,
    onTertiary = OhoCharcoal,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = Color(0xFF4A250E),
    background = if (eyeComfortMode) ComfortBackground else LinenBackground,
    surface = if (eyeComfortMode) ComfortSurface else LinenSurface,
    surfaceVariant = if (eyeComfortMode) ComfortSurfaceVariant else LinenSurfaceSubtle,
    onBackground = if (eyeComfortMode) ComfortText else CharcoalText,
    onSurface = if (eyeComfortMode) ComfortText else CharcoalText,
    onSurfaceVariant = if (eyeComfortMode) ComfortTextSecondary else CharcoalTextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderSubtle.copy(alpha = 0.6f)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    eyeComfortMode: Boolean = true, // Defaults to true: exceptionally gentle on the eyes
    dynamicColor: Boolean = false,  // Keep refined eye-care brand palette
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> createLightColorScheme(eyeComfortMode)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


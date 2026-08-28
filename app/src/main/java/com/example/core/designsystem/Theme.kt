package com.example.core.designsystem

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF405AA0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE2F9),
    onPrimaryContainer = Color(0xFF001947),
    secondary = Color(0xFF006C4C),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF8BF7C7),
    onSecondaryContainer = Color(0xFF002114),
    tertiary = Color(0xFF006782),
    onTertiary = Color.White,
    background = Color(0xFFF8F9FF),
    onBackground = Color(0xFF191C1E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191C1E),
    surfaceVariant = Color(0xFFEDF0F9),
    onSurfaceVariant = Color(0xFF44474F),
    outline = Color(0xFFDEE3EB),
    outlineVariant = Color(0xFFE8EBF2),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF06B6D4),
    onPrimary = Color(0xFF0B0F19),
    primaryContainer = Color(0xFF164E63),
    onPrimaryContainer = Color(0xFFCFFAFE),
    secondary = Color(0xFF10B981),
    onSecondary = Color(0xFF0B0F19),
    secondaryContainer = Color(0xFF065F46),
    onSecondaryContainer = Color(0xFFD1FAE5),
    tertiary = Color(0xFF38BDF8),
    onTertiary = Color(0xFF0C4A6E),
    background = Color(0xFF0B0F19),
    onBackground = Color(0xFFF9FAFB),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFF9FAFB),
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = Color(0xFF374151),
    outlineVariant = Color(0xFF1F2937),
    error = Color(0xFFEF4444),
    onError = Color(0xFF450A0A)
)

@Composable
fun KeyNestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val keyNestColors = if (darkTheme) DarkKeyNestColors else LightKeyNestColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as? Activity)?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    CompositionLocalProvider(LocalKeyNestColors provides keyNestColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

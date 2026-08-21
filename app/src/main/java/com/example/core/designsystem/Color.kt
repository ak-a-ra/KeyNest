package com.example.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class KeyNestColors(
    val obsidianBg: Color,
    val obsidianSurface: Color,
    val obsidianSurfaceElevated: Color,
    val obsidianSurfaceHighlight: Color,
    val obsidianBorder: Color,
    val obsidianBorderLight: Color,
    val cyberGold: Color,
    val cyberGoldDark: Color,
    val cyberGoldLight: Color,
    val cyberEmerald: Color,
    val cyberEmeraldDark: Color,
    val cyberEmeraldLight: Color,
    val cyberCyan: Color,
    val cyberBlue: Color,
    val cyberPurple: Color,
    val cyberRose: Color,
    val cyberOrange: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textMuted: Color,
    val statusSuccess: Color,
    val statusWarning: Color,
    val statusDanger: Color,
    val statusInfo: Color,
    val vibrantPillBg: Color,
    val vibrantButtonBg: Color,
    val vibrantButtonPressed: Color,
    val vibrantAvatarBg: Color,
    val isDark: Boolean
)

val LightKeyNestColors = KeyNestColors(
    obsidianBg = Color(0xFFFFFFFF),
    obsidianSurface = Color(0xFFFFFFFF),
    obsidianSurfaceElevated = Color(0xFFFFFFFF),
    obsidianSurfaceHighlight = Color(0xFFF1F3F4),
    obsidianBorder = Color(0xFFE0E0E0),
    obsidianBorderLight = Color(0xFFF1F3F4),
    cyberGold = Color(0xFFF29900),
    cyberGoldDark = Color(0xFFE37400),
    cyberGoldLight = Color(0xFFFDE293),
    cyberEmerald = Color(0xFF1E8E3E),
    cyberEmeraldDark = Color(0xFF0F9D58),
    cyberEmeraldLight = Color(0xFFA8DAB5),
    cyberCyan = Color(0xFF12B5CB),
    cyberBlue = Color(0xFF1A73E8),
    cyberPurple = Color(0xFF9334E6),
    cyberRose = Color(0xFFD93025),
    cyberOrange = Color(0xFFE37400),
    textPrimary = Color(0xFF202124),
    textSecondary = Color(0xFF5F6368),
    textTertiary = Color(0xFF80868B),
    textMuted = Color(0xFFBDC1C6),
    statusSuccess = Color(0xFF1E8E3E),
    statusWarning = Color(0xFFF29900),
    statusDanger = Color(0xFFD93025),
    statusInfo = Color(0xFF1A73E8),
    vibrantPillBg = Color(0xFFF1F3F4),
    vibrantButtonBg = Color(0xFFF1F3F4),
    vibrantButtonPressed = Color(0xFFE8EAED),
    vibrantAvatarBg = Color(0xFFE8EAED),
    isDark = false
)

val DarkKeyNestColors = KeyNestColors(
    obsidianBg = Color(0xFF202124),
    obsidianSurface = Color(0xFF202124),
    obsidianSurfaceElevated = Color(0xFF525355),
    obsidianSurfaceHighlight = Color(0xFF3C4043),
    obsidianBorder = Color(0xFF5F6368),
    obsidianBorderLight = Color(0xFF5F6368),
    cyberGold = Color(0xFFF29900),
    cyberGoldDark = Color(0xFFE37400),
    cyberGoldLight = Color(0xFFFDE293),
    cyberEmerald = Color(0xFF1E8E3E),
    cyberEmeraldDark = Color(0xFF0F9D58),
    cyberEmeraldLight = Color(0xFFA8DAB5),
    cyberCyan = Color(0xFF12B5CB),
    cyberBlue = Color(0xFF8AB4F8),
    cyberPurple = Color(0xFFC58AF9),
    cyberRose = Color(0xFFF28B82),
    cyberOrange = Color(0xFFFCAD70),
    textPrimary = Color(0xFFE8EAED),
    textSecondary = Color(0xFF9AA0A6),
    textTertiary = Color(0xFF80868B),
    textMuted = Color(0xFF5F6368),
    statusSuccess = Color(0xFF81C995),
    statusWarning = Color(0xFFFDE293),
    statusDanger = Color(0xFFF28B82),
    statusInfo = Color(0xFF8AB4F8),
    vibrantPillBg = Color(0xFF525355),
    vibrantButtonBg = Color(0xFF525355),
    vibrantButtonPressed = Color(0xFF3C4043),
    vibrantAvatarBg = Color(0xFF3C4043),
    isDark = true
)

val LocalKeyNestColors = staticCompositionLocalOf { LightKeyNestColors }

// Vibrant Palette - Primary Surfaces & Backgrounds (Dynamic Theme Resolvers)
val ObsidianBg: Color
    @Composable
    get() = LocalKeyNestColors.current.obsidianBg

val ObsidianSurface: Color
    @Composable
    get() = LocalKeyNestColors.current.obsidianSurface

val ObsidianSurfaceElevated: Color
    @Composable
    get() = LocalKeyNestColors.current.obsidianSurfaceElevated

val ObsidianSurfaceHighlight: Color
    @Composable
    get() = LocalKeyNestColors.current.obsidianSurfaceHighlight

val ObsidianBorder: Color
    @Composable
    get() = LocalKeyNestColors.current.obsidianBorder

val ObsidianBorderLight: Color
    @Composable
    get() = LocalKeyNestColors.current.obsidianBorderLight

// Vibrant Palette - Cobalt / Indigo & Accent Colors
val CyberGold: Color
    @Composable
    get() = LocalKeyNestColors.current.cyberGold

val CyberGoldDark: Color
    @Composable
    get() = LocalKeyNestColors.current.cyberGoldDark

val CyberGoldLight: Color
    @Composable
    get() = LocalKeyNestColors.current.cyberGoldLight

val CyberEmerald: Color
    @Composable
    get() = LocalKeyNestColors.current.cyberEmerald

val CyberEmeraldDark: Color
    @Composable
    get() = LocalKeyNestColors.current.cyberEmeraldDark

val CyberEmeraldLight: Color
    @Composable
    get() = LocalKeyNestColors.current.cyberEmeraldLight

val CyberCyan: Color
    @Composable
    get() = LocalKeyNestColors.current.cyberCyan

val CyberBlue: Color
    @Composable
    get() = LocalKeyNestColors.current.cyberBlue

val CyberPurple: Color
    @Composable
    get() = LocalKeyNestColors.current.cyberPurple

val CyberRose: Color
    @Composable
    get() = LocalKeyNestColors.current.cyberRose

val CyberOrange: Color
    @Composable
    get() = LocalKeyNestColors.current.cyberOrange

// Vibrant Palette - Text Colors
val TextPrimary: Color
    @Composable
    get() = LocalKeyNestColors.current.textPrimary

val TextSecondary: Color
    @Composable
    get() = LocalKeyNestColors.current.textSecondary

val TextTertiary: Color
    @Composable
    get() = LocalKeyNestColors.current.textTertiary

val TextMuted: Color
    @Composable
    get() = LocalKeyNestColors.current.textMuted

// Vibrant Palette - Status Colors
val StatusSuccess: Color
    @Composable
    get() = LocalKeyNestColors.current.statusSuccess

val StatusWarning: Color
    @Composable
    get() = LocalKeyNestColors.current.statusWarning

val StatusDanger: Color
    @Composable
    get() = LocalKeyNestColors.current.statusDanger

val StatusInfo: Color
    @Composable
    get() = LocalKeyNestColors.current.statusInfo

// Vibrant Palette Component Tints
val VibrantPillBg: Color
    @Composable
    get() = LocalKeyNestColors.current.vibrantPillBg

val VibrantButtonBg: Color
    @Composable
    get() = LocalKeyNestColors.current.vibrantButtonBg

val VibrantButtonPressed: Color
    @Composable
    get() = LocalKeyNestColors.current.vibrantButtonPressed

val VibrantAvatarBg: Color
    @Composable
    get() = LocalKeyNestColors.current.vibrantAvatarBg

enum class VaultCardColor(
    val hex: String?,
    val lightBg: Long,
    val darkBg: Long,
    val label: String
) {
    DEFAULT(null, 0xFFFFFFFF, 0xFF202124, "Default"),
    CORAL("#F28B82", 0xFFFCE8E6, 0xFF492120, "Coral"),
    SAND("#FBBC04", 0xFFFEF7E0, 0xFF4A3E17, "Sand"),
    SAGE("#CCFF90", 0xFFE6F4EA, 0xFF1E3A27, "Sage"),
    FOG("#A7FFEB", 0xFFE0F7FA, 0xFF143B39, "Fog"),
    STORM("#CBF0F8", 0xFFE8F0FE, 0xFF1E384D, "Storm"),
    DUSK("#D7AEFB", 0xFFF3E8FD, 0xFF352048, "Dusk"),
    BLOSSOM("#FDCFE8", 0xFFFCE8F3, 0xFF441C34, "Blossom");

    companion object {
        fun fromHex(hex: String?): VaultCardColor {
            if (hex.isNullOrBlank()) return DEFAULT
            return entries.firstOrNull { it.hex.equals(hex, ignoreCase = true) } ?: DEFAULT
        }
    }
}


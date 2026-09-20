package com.arghya.alarmapp.ui.theme

import androidx.compose.ui.graphics.Color

// Light theme
val LightPrimary = Color(0xFF3E64FF)
val LightBackground = Color(0xFFF7F8FC)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF1B1C1F)

// Default dark (soft black, not pure #000)
val DarkPrimary = Color(0xFF8AA6FF)
val DarkBackground = Color(0xFF121318)
val DarkSurface = Color(0xFF1C1D22)
val DarkOnSurface = Color(0xFFE4E2E6)

// Night color variants — alternatives to plain black
enum class NightPalette(val displayName: String, val background: Color, val surface: Color, val primary: Color, val onSurface: Color) {
    MIDNIGHT_BLACK("Midnight Black", Color(0xFF0B0B0F), Color(0xFF17171C), Color(0xFF9CA8FF), Color(0xFFE7E7EA)),
    DEEP_NAVY("Deep Navy", Color(0xFF0B1226), Color(0xFF141C36), Color(0xFF7FA8FF), Color(0xFFE3E8FF)),
    FOREST("Forest", Color(0xFF0D1710), Color(0xFF15251A), Color(0xFF7ED9A0), Color(0xFFE0F3E5)),
    AMBER_DUSK("Amber Dusk", Color(0xFF1A1209), Color(0xFF261B0E), Color(0xFFFFB86B), Color(0xFFF6E7D8)),
    PLUM("Plum", Color(0xFF160B1A), Color(0xFF23152A), Color(0xFFD79CFF), Color(0xFFF0E3F5)),
}

// Liquid-glass overlay tint used across cards/sheets
val GlassTintLight = Color(0x33FFFFFF)
val GlassTintDark = Color(0x22FFFFFF)

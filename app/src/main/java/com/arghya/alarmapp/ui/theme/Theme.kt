package com.arghya.alarmapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class AppThemeMode { LIGHT, DARK, SYSTEM }

@Composable
fun AlarmAppTheme(
    themeMode: AppThemeMode,
    systemDark: Boolean,
    nightPalette: NightPalette,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> systemDark
    }

    val colorScheme = if (useDark) {
        darkColorScheme(
            primary = nightPalette.primary,
            background = nightPalette.background,
            surface = nightPalette.surface,
            onSurface = nightPalette.onSurface,
            onBackground = nightPalette.onSurface
        )
    } else {
        lightColorScheme(
            primary = LightPrimary,
            background = LightBackground,
            surface = LightSurface,
            onSurface = LightOnSurface,
            onBackground = LightOnSurface
        )
    }

    MaterialTheme(colorScheme = colorScheme, typography = MaterialTheme.typography, content = content)
}

/** "Liquid glass" card look: translucent gradient + soft blur-like border, no external asset needed. */
fun Modifier.liquidGlass(isDark: Boolean, corner: Int = 24): Modifier {
    val tint = if (isDark) GlassTintDark else GlassTintLight
    return this
        .clip(RoundedCornerShape(corner.dp))
        .background(
            Brush.verticalGradient(
                listOf(tint, tint.copy(alpha = tint.alpha * 0.4f))
            )
        )
        .border(
            width = 1.dp,
            brush = Brush.verticalGradient(
                listOf(Color.White.copy(alpha = if (isDark) 0.18f else 0.5f), Color.Transparent)
            ),
            shape = RoundedCornerShape(corner.dp)
        )
}

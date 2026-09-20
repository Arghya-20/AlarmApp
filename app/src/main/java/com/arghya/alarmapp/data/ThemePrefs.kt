package com.arghya.alarmapp.data

import android.content.Context
import com.arghya.alarmapp.ui.theme.AppThemeMode
import com.arghya.alarmapp.ui.theme.NightPalette

class ThemePrefs(context: Context) {
    private val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    var themeMode: AppThemeMode
        get() = AppThemeMode.valueOf(prefs.getString("theme_mode", AppThemeMode.SYSTEM.name)!!)
        set(value) = prefs.edit().putString("theme_mode", value.name).apply()

    var nightPalette: NightPalette
        get() = NightPalette.valueOf(prefs.getString("night_palette", NightPalette.MIDNIGHT_BLACK.name)!!)
        set(value) = prefs.edit().putString("night_palette", value.name).apply()
}

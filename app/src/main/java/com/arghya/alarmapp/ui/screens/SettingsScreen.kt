package com.arghya.alarmapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arghya.alarmapp.data.BedtimeSettings
import com.arghya.alarmapp.ui.theme.AppThemeMode
import com.arghya.alarmapp.ui.theme.NightPalette
import com.arghya.alarmapp.ui.theme.liquidGlass

@Composable
fun SettingsScreen(
    isDark: Boolean,
    themeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit,
    nightPalette: NightPalette,
    onNightPaletteChange: (NightPalette) -> Unit,
    bedtime: BedtimeSettings,
    onBedtimeChange: (BedtimeSettings) -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Appearance", style = MaterialTheme.typography.titleLarge)

        Column(Modifier.fillMaxWidth().liquidGlass(isDark).padding(16.dp)) {
            Text("Theme", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = themeMode == mode,
                        onClick = { onThemeModeChange(mode) },
                        label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            if (themeMode == AppThemeMode.DARK) {
                Spacer(Modifier.height(16.dp))
                Text("Night color", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                NightPalette.entries.forEach { palette ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = nightPalette == palette, onClick = { onNightPaletteChange(palette) })
                        Box(
                            Modifier.size(20.dp)
                        ) {
                            Surface(color = palette.background, modifier = Modifier.fillMaxSize()) {}
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(palette.displayName)
                    }
                }
            }
        }

        Text("Bedtime Mode", style = MaterialTheme.typography.titleLarge)
        Column(Modifier.fillMaxWidth().liquidGlass(isDark).padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Enable bedtime mode")
                Switch(checked = bedtime.enabled, onCheckedChange = { onBedtimeChange(bedtime.copy(enabled = it)) })
            }
            Spacer(Modifier.height(8.dp))
            Text("Screen dims from ${fmt(bedtime.startHour, bedtime.startMinute)} to ${fmt(bedtime.endHour, bedtime.endMinute)}")
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = bedtime.startHour.toString(),
                    onValueChange = { onBedtimeChange(bedtime.copy(startHour = it.toIntOrNull()?.coerceIn(0, 23) ?: bedtime.startHour)) },
                    label = { Text("Start hr") }, modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = bedtime.startMinute.toString(),
                    onValueChange = { onBedtimeChange(bedtime.copy(startMinute = it.toIntOrNull()?.coerceIn(0, 59) ?: bedtime.startMinute)) },
                    label = { Text("Start min") }, modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = bedtime.endHour.toString(),
                    onValueChange = { onBedtimeChange(bedtime.copy(endHour = it.toIntOrNull()?.coerceIn(0, 23) ?: bedtime.endHour)) },
                    label = { Text("End hr") }, modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = bedtime.endMinute.toString(),
                    onValueChange = { onBedtimeChange(bedtime.copy(endMinute = it.toIntOrNull()?.coerceIn(0, 59) ?: bedtime.endMinute)) },
                    label = { Text("End min") }, modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))
            val isPaused = bedtime.pausedForDateMillis != null &&
                isSameNight(bedtime.pausedForDateMillis, System.currentTimeMillis())

            Button(
                onClick = {
                    onBedtimeChange(
                        if (isPaused) bedtime.copy(pausedForDateMillis = null)
                        else bedtime.copy(pausedForDateMillis = System.currentTimeMillis())
                    )
                },
                enabled = bedtime.enabled
            ) {
                Text(if (isPaused) "Resume bedtime tonight" else "Pause bedtime for tonight")
            }
            Text(
                "Pausing skips tonight's auto screen-dim only \u2014 it won't turn bedtime mode off entirely.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun fmt(h: Int, m: Int) = String.format("%02d:%02d", h, m)

private fun isSameNight(a: Long, b: Long): Boolean {
    val cal1 = java.util.Calendar.getInstance().apply { timeInMillis = a }
    val cal2 = java.util.Calendar.getInstance().apply { timeInMillis = b }
    return cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR) &&
        cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR)
}

package com.arghya.alarmapp

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.arghya.alarmapp.alarm.AlarmScheduler
import com.arghya.alarmapp.data.*
import com.arghya.alarmapp.ui.screens.AlarmsScreen
import com.arghya.alarmapp.ui.screens.CalendarScreen
import com.arghya.alarmapp.ui.screens.SettingsScreen
import com.arghya.alarmapp.ui.screens.TasksScreen
import com.arghya.alarmapp.ui.theme.AlarmAppTheme
import com.arghya.alarmapp.ui.theme.AppThemeMode
import com.arghya.alarmapp.ui.theme.NightPalette
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var db: AppDatabase
    private lateinit var prefs: ThemePrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.get(this)
        prefs = ThemePrefs(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
                .launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        requestExactAlarmPermissionIfNeeded()

        setContent {
            var themeMode by remember { mutableStateOf(prefs.themeMode) }
            var nightPalette by remember { mutableStateOf(prefs.nightPalette) }
            val systemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
                AppThemeMode.SYSTEM -> systemDark
            }

            AlarmAppTheme(themeMode, systemDark, nightPalette) {
                AppRoot(
                    db = db,
                    isDark = isDark,
                    themeMode = themeMode,
                    onThemeModeChange = { themeMode = it; prefs.themeMode = it },
                    nightPalette = nightPalette,
                    onNightPaletteChange = { nightPalette = it; prefs.nightPalette = it }
                )
            }
        }
    }

    private fun requestExactAlarmPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!am.canScheduleExactAlarms()) {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
            }
        }
    }
}

private enum class Tab(val label: String) { ALARMS("Alarms"), CALENDAR("Calendar"), TASKS("Tasks"), SETTINGS("Settings") }

@Composable
private fun AppRoot(
    db: AppDatabase,
    isDark: Boolean,
    themeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit,
    nightPalette: NightPalette,
    onNightPaletteChange: (NightPalette) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    var tab by remember { mutableStateOf(Tab.ALARMS) }

    val alarms by db.alarmDao().getAll().collectAsState(initial = emptyList())
    val reminders by db.reminderDao().getAll().collectAsState(initial = emptyList())
    val todos by db.todoDao().getAll().collectAsState(initial = emptyList())
    val bedtimeFlow by db.bedtimeDao().get().collectAsState(initial = null)
    val bedtime = bedtimeFlow ?: BedtimeSettings()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = tab == Tab.ALARMS, onClick = { tab = Tab.ALARMS },
                    icon = { Icon(Icons.Filled.Alarm, null) }, label = { Text("Alarms") })
                NavigationBarItem(selected = tab == Tab.CALENDAR, onClick = { tab = Tab.CALENDAR },
                    icon = { Icon(Icons.Filled.CalendarMonth, null) }, label = { Text("Calendar") })
                NavigationBarItem(selected = tab == Tab.TASKS, onClick = { tab = Tab.TASKS },
                    icon = { Icon(Icons.Filled.CheckCircle, null) }, label = { Text("Tasks") })
                NavigationBarItem(selected = tab == Tab.SETTINGS, onClick = { tab = Tab.SETTINGS },
                    icon = { Icon(Icons.Filled.Settings, null) }, label = { Text("Settings") })
            }
        }
    ) { padding ->
        androidx.compose.foundation.layout.Box(Modifier.padding(padding)) {
            when (tab) {
                Tab.ALARMS -> AlarmsScreen(
                    alarms = alarms,
                    isDark = isDark,
                    onSave = { alarm ->
                        scope.launch {
                            val id = db.alarmDao().upsert(alarm)
                            val saved = if (alarm.id == 0) alarm.copy(id = id.toInt()) else alarm
                            AlarmScheduler.schedule(context, saved)
                        }
                    },
                    onDelete = { alarm ->
                        scope.launch {
                            AlarmScheduler.cancel(context, alarm)
                            db.alarmDao().delete(alarm)
                        }
                    },
                    onToggle = { alarm, enabled ->
                        scope.launch {
                            val updated = alarm.copy(enabled = enabled)
                            db.alarmDao().upsert(updated)
                            if (enabled) AlarmScheduler.schedule(context, updated)
                            else AlarmScheduler.cancel(context, updated)
                        }
                    }
                )
                Tab.CALENDAR -> CalendarScreen(alarms = alarms, reminders = reminders, isDark = isDark)
                Tab.TASKS -> TasksScreen(
                    reminders = reminders,
                    todos = todos,
                    isDark = isDark,
                    onAddReminder = { r -> scope.launch { db.reminderDao().upsert(r) } },
                    onToggleReminder = { r -> scope.launch { db.reminderDao().upsert(r.copy(done = !r.done)) } },
                    onDeleteReminder = { r -> scope.launch { db.reminderDao().delete(r) } },
                    onAddTodo = { t -> scope.launch { db.todoDao().upsert(t) } },
                    onToggleTodo = { t -> scope.launch { db.todoDao().upsert(t.copy(done = !t.done)) } },
                    onDeleteTodo = { t -> scope.launch { db.todoDao().delete(t) } }
                )
                Tab.SETTINGS -> SettingsScreen(
                    isDark = isDark,
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange,
                    nightPalette = nightPalette,
                    onNightPaletteChange = onNightPaletteChange,
                    bedtime = bedtime,
                    onBedtimeChange = { updated -> scope.launch { db.bedtimeDao().upsert(updated) } }
                )
            }
        }
    }
}

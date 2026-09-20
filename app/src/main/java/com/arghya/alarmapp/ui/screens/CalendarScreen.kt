package com.arghya.alarmapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.arghya.alarmapp.data.Alarm
import com.arghya.alarmapp.data.Reminder
import com.arghya.alarmapp.ui.theme.liquidGlass
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(alarms: List<Alarm>, reminders: List<Reminder>, isDark: Boolean) {
    var monthCursor by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDay by remember { mutableStateOf<Calendar?>(null) }

    val alarmDates = remember(alarms) {
        alarms.mapNotNull { it.specificDateMillis }.map { dayKey(it) }.toSet()
    }
    val reminderDates = remember(reminders) {
        reminders.map { dayKey(it.dueMillis) }.toSet()
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { monthCursor = (monthCursor.clone() as Calendar).apply { add(Calendar.MONTH, -1) } }) {
                Text("< Prev")
            }
            Text(
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(monthCursor.time),
                style = MaterialTheme.typography.titleLarge
            )
            TextButton(onClick = { monthCursor = (monthCursor.clone() as Calendar).apply { add(Calendar.MONTH, 1) } }) {
                Text("Next >")
            }
        }

        Spacer(Modifier.height(12.dp))

        val daysInMonth = (monthCursor.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
        val firstDayOfWeek = daysInMonth.get(Calendar.DAY_OF_WEEK) // 1=Sun
        val numDays = daysInMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        val leadingBlanks = firstDayOfWeek - 1

        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.weight(1f)) {
            items(leadingBlanks) { Box(Modifier.height(48.dp)) }
            items(numDays) { i ->
                val dayNum = i + 1
                val cellCal = (daysInMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, dayNum) }
                val key = dayKey(cellCal.timeInMillis)
                val hasAlarm = alarmDates.contains(key)
                val hasReminder = reminderDates.contains(key)
                val isSelected = selectedDay?.let { dayKey(it.timeInMillis) == key } ?: false

                Box(
                    Modifier
                        .height(48.dp)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable { selectedDay = cellCal },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(dayNum.toString())
                        Row {
                            if (hasAlarm) Dot(MaterialTheme.colorScheme.primary)
                            if (hasReminder) Dot(MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
            }
        }

        selectedDay?.let { day ->
            Spacer(Modifier.height(12.dp))
            val key = dayKey(day.timeInMillis)
            val dayAlarms = alarms.filter { it.specificDateMillis != null && dayKey(it.specificDateMillis) == key }
            val dayReminders = reminders.filter { dayKey(it.dueMillis) == key }

            Column(Modifier.fillMaxWidth().liquidGlass(isDark).padding(16.dp)) {
                Text(
                    SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(day.time),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                if (dayAlarms.isEmpty() && dayReminders.isEmpty()) {
                    Text("Nothing scheduled this day.")
                } else {
                    dayAlarms.forEach { Text("⏰ ${String.format("%02d:%02d", it.hour, it.minute)} — ${it.label.ifBlank { "Alarm" }}") }
                    dayReminders.forEach { Text("📝 ${it.title}") }
                }
            }
        }
    }
}

@Composable
private fun Dot(color: androidx.compose.ui.graphics.Color) {
    Box(
        Modifier
            .padding(1.dp)
            .size(6.dp)
            .clip(CircleShape)
            .background(color)
    )
}

private fun dayKey(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
}

package com.arghya.alarmapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arghya.alarmapp.alarm.AlarmScheduler
import com.arghya.alarmapp.data.Alarm
import com.arghya.alarmapp.ui.theme.liquidGlass
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(
    alarms: List<Alarm>,
    isDark: Boolean,
    onSave: (Alarm) -> Unit,
    onDelete: (Alarm) -> Unit,
    onToggle: (Alarm, Boolean) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Alarm?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = null; showDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add alarm")
            }
        }
    ) { padding ->
        if (alarms.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No alarms yet. Tap + to add one.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(alarms) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        isDark = isDark,
                        onClick = { editing = alarm; showDialog = true },
                        onToggle = { onToggle(alarm, it) },
                        onDelete = { onDelete(alarm) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AlarmEditDialog(
            existing = editing,
            onDismiss = { showDialog = false },
            onSave = {
                onSave(it)
                showDialog = false
            }
        )
    }
}

@Composable
private fun AlarmCard(
    alarm: Alarm,
    isDark: Boolean,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlass(isDark)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).clickable { onClick() }) {
            Text(
                String.format("%02d:%02d", alarm.hour, alarm.minute),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(alarm.label.ifBlank { "Alarm" }, style = MaterialTheme.typography.bodyMedium)
            Text(subtitleFor(alarm), style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = alarm.enabled, onCheckedChange = onToggle)
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete")
        }
    }
}

private fun subtitleFor(alarm: Alarm): String {
    if (alarm.specificDateMillis != null) {
        val fmt = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return "On ${fmt.format(Date(alarm.specificDateMillis))}"
    }
    if (alarm.daysOfWeek == 0) return "One-time"
    val names = listOf("Mon" to 1, "Tue" to 2, "Wed" to 4, "Thu" to 8, "Fri" to 16, "Sat" to 32, "Sun" to 64)
    return names.filter { (alarm.daysOfWeek and it.second) != 0 }.joinToString(" ") { it.first }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlarmEditDialog(
    existing: Alarm?,
    onDismiss: () -> Unit,
    onSave: (Alarm) -> Unit
) {
    var label by remember { mutableStateOf(existing?.label ?: "") }
    var hour by remember { mutableStateOf(existing?.hour ?: 7) }
    var minute by remember { mutableStateOf(existing?.minute ?: 0) }
    var shakeToStop by remember { mutableStateOf(existing?.shakeToStop ?: true) }
    var vibrate by remember { mutableStateOf(existing?.vibrate ?: true) }
    var useSpecificDate by remember { mutableStateOf(existing?.specificDateMillis != null) }
    var specificDate by remember { mutableStateOf(existing?.specificDateMillis) }
    var daysOfWeek by remember { mutableStateOf(existing?.daysOfWeek ?: 0) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = specificDate)
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "New Alarm" else "Edit Alarm") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label") })

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hour.toString(), onValueChange = { hour = it.toIntOrNull()?.coerceIn(0, 23) ?: hour },
                        label = { Text("Hour") }, modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minute.toString(), onValueChange = { minute = it.toIntOrNull()?.coerceIn(0, 59) ?: minute },
                        label = { Text("Minute") }, modifier = Modifier.weight(1f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = useSpecificDate, onCheckedChange = { useSpecificDate = it })
                    Text("Set for a specific calendar date")
                }

                if (useSpecificDate) {
                    OutlinedButton(onClick = { showDatePicker = true }) {
                        val fmt = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                        Text(specificDate?.let { fmt.format(Date(it)) } ?: "Pick a date")
                    }
                } else {
                    Text("Repeat on:", style = MaterialTheme.typography.labelLarge)
                    Row {
                        listOf("M" to 1, "T" to 2, "W" to 4, "T" to 8, "F" to 16, "S" to 32, "S" to 64).forEach { (n, bit) ->
                            val selected = (daysOfWeek and bit) != 0
                            FilterChip(
                                selected = selected,
                                onClick = { daysOfWeek = daysOfWeek xor bit },
                                label = { Text(n) },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = shakeToStop, onCheckedChange = { shakeToStop = it })
                    Text("Shake to stop")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = vibrate, onCheckedChange = { vibrate = it })
                    Text("Vibrate")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    Alarm(
                        id = existing?.id ?: 0,
                        label = label,
                        hour = hour,
                        minute = minute,
                        specificDateMillis = if (useSpecificDate) specificDate else null,
                        daysOfWeek = if (useSpecificDate) 0 else daysOfWeek,
                        enabled = existing?.enabled ?: true,
                        shakeToStop = shakeToStop,
                        vibrate = vibrate
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    specificDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.arghya.alarmapp.data.Reminder
import com.arghya.alarmapp.data.Todo
import com.arghya.alarmapp.ui.theme.liquidGlass
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TasksScreen(
    reminders: List<Reminder>,
    todos: List<Todo>,
    isDark: Boolean,
    onAddReminder: (Reminder) -> Unit,
    onToggleReminder: (Reminder) -> Unit,
    onDeleteReminder: (Reminder) -> Unit,
    onAddTodo: (Todo) -> Unit,
    onToggleTodo: (Todo) -> Unit,
    onDeleteTodo: (Todo) -> Unit
) {
    var tab by remember { mutableStateOf(0) }
    var showAddReminder by remember { mutableStateOf(false) }
    var showAddTodo by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (tab == 0) showAddReminder = true else showAddTodo = true
            }) { Icon(Icons.Filled.Add, contentDescription = "Add") }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Reminders") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("To-Do") })
            }

            if (tab == 0) {
                LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(reminders) { r ->
                        ReminderRow(r, isDark, onToggleReminder, onDeleteReminder)
                    }
                }
            } else {
                LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(todos) { t ->
                        TodoRow(t, isDark, onToggleTodo, onDeleteTodo)
                    }
                }
            }
        }
    }

    if (showAddReminder) {
        AddReminderDialog(onDismiss = { showAddReminder = false }, onSave = { onAddReminder(it); showAddReminder = false })
    }
    if (showAddTodo) {
        AddTodoDialog(onDismiss = { showAddTodo = false }, onSave = { onAddTodo(it); showAddTodo = false })
    }
}

@Composable
private fun ReminderRow(r: Reminder, isDark: Boolean, onToggle: (Reminder) -> Unit, onDelete: (Reminder) -> Unit) {
    Row(
        Modifier.fillMaxWidth().liquidGlass(isDark).padding(14.dp).clickable { onToggle(r) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = r.done, onCheckedChange = { onToggle(r) })
        Column(Modifier.weight(1f)) {
            Text(
                r.title,
                textDecoration = if (r.done) TextDecoration.LineThrough else null,
                style = MaterialTheme.typography.bodyLarge
            )
            val fmt = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            Text(fmt.format(Date(r.dueMillis)), style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = { onDelete(r) }) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
    }
}

@Composable
private fun TodoRow(t: Todo, isDark: Boolean, onToggle: (Todo) -> Unit, onDelete: (Todo) -> Unit) {
    Row(
        Modifier.fillMaxWidth().liquidGlass(isDark).padding(14.dp).clickable { onToggle(t) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = t.done, onCheckedChange = { onToggle(t) })
        Text(
            t.title,
            modifier = Modifier.weight(1f),
            textDecoration = if (t.done) TextDecoration.LineThrough else null,
            style = MaterialTheme.typography.bodyLarge
        )
        IconButton(onClick = { onDelete(t) }) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
    }
}

@Composable
private fun AddReminderDialog(onDismiss: () -> Unit, onSave: (Reminder) -> Unit) {
    var title by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Reminder") },
        text = {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank()) {
                    onSave(Reminder(title = title, dueMillis = System.currentTimeMillis() + 3600_000))
                }
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddTodoDialog(onDismiss: () -> Unit, onSave: (Todo) -> Unit) {
    var title by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New To-Do") },
        text = {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Task") })
        },
        confirmButton = {
            TextButton(onClick = { if (title.isNotBlank()) onSave(Todo(title = title)) }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

package com.arghya.alarmapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String,
    val hour: Int,
    val minute: Int,
    // Specific date (epoch millis, midnight) if one-off; null if recurring by daysOfWeek
    val specificDateMillis: Long? = null,
    // Bitmask: 1=Mon .. 64=Sun, 0 if one-off/specific-date alarm
    val daysOfWeek: Int = 0,
    val enabled: Boolean = true,
    val shakeToStop: Boolean = true,
    val soundUri: String? = null,
    val vibrate: Boolean = true
)

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val note: String = "",
    val dueMillis: Long,
    val done: Boolean = false
)

@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val done: Boolean = false,
    val createdMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "bedtime_settings")
data class BedtimeSettings(
    @PrimaryKey val id: Int = 0,
    val enabled: Boolean = false,
    val startHour: Int = 22,
    val startMinute: Int = 30,
    val endHour: Int = 6,
    val endMinute: Int = 30,
    // If set to a specific date's millis (midnight), bedtime is paused just for that night
    val pausedForDateMillis: Long? = null
)

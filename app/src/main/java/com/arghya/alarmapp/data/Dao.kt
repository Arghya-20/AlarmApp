package com.arghya.alarmapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hour, minute")
    fun getAll(): Flow<List<Alarm>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(alarm: Alarm): Long

    @Delete
    suspend fun delete(alarm: Alarm)

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getById(id: Int): Alarm?
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY dueMillis")
    fun getAll(): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(reminder: Reminder): Long

    @Delete
    suspend fun delete(reminder: Reminder)
}

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY createdMillis DESC")
    fun getAll(): Flow<List<Todo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(todo: Todo): Long

    @Delete
    suspend fun delete(todo: Todo)
}

@Dao
interface BedtimeDao {
    @Query("SELECT * FROM bedtime_settings WHERE id = 0")
    fun get(): Flow<BedtimeSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: BedtimeSettings)
}

package com.arghya.alarmapp.alarm

import com.arghya.alarmapp.data.BedtimeSettings
import java.util.Calendar

object BedtimeManager {

    /** True if right now falls inside the configured bedtime window and it hasn't been paused for tonight. */
    fun isBedtimeActiveNow(settings: BedtimeSettings, now: Calendar = Calendar.getInstance()): Boolean {
        if (!settings.enabled) return false

        // Check if paused for "tonight" - i.e. the bedtime window that started on pausedForDateMillis' day
        if (settings.pausedForDateMillis != null) {
            val pausedDay = Calendar.getInstance().apply {
                timeInMillis = settings.pausedForDateMillis
            }
            val startOfWindow = windowStart(settings, pausedDay)
            val endOfWindow = windowEnd(settings, pausedDay)
            if (now.timeInMillis in startOfWindow..endOfWindow) {
                return false // paused for tonight
            }
        }

        // Determine bedtime window anchored to "today" and "yesterday" (window may cross midnight)
        val todayStart = windowStart(settings, now)
        val todayEnd = windowEnd(settings, now)
        if (now.timeInMillis in todayStart..todayEnd) return true

        val yesterday = now.clone() as Calendar
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        val yStart = windowStart(settings, yesterday)
        val yEnd = windowEnd(settings, yesterday)
        return now.timeInMillis in yStart..yEnd
    }

    private fun windowStart(settings: BedtimeSettings, anchorDay: Calendar): Long {
        val c = anchorDay.clone() as Calendar
        c.set(Calendar.HOUR_OF_DAY, settings.startHour)
        c.set(Calendar.MINUTE, settings.startMinute)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    private fun windowEnd(settings: BedtimeSettings, anchorDay: Calendar): Long {
        val c = anchorDay.clone() as Calendar
        c.set(Calendar.HOUR_OF_DAY, settings.endHour)
        c.set(Calendar.MINUTE, settings.endMinute)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        // end is on the next day if it's <= start (crosses midnight)
        if (c.timeInMillis <= windowStart(settings, anchorDay)) {
            c.add(Calendar.DAY_OF_YEAR, 1)
        }
        return c.timeInMillis
    }

    /** Call when user taps "Pause tonight" — records today's date so tonight's window is skipped. */
    fun pauseForTonight(settings: BedtimeSettings): BedtimeSettings {
        return settings.copy(pausedForDateMillis = System.currentTimeMillis())
    }

    fun clearPause(settings: BedtimeSettings): BedtimeSettings {
        return settings.copy(pausedForDateMillis = null)
    }
}

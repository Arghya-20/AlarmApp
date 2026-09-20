package com.arghya.alarmapp.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.arghya.alarmapp.data.Alarm
import com.arghya.alarmapp.receiver.AlarmReceiver
import java.util.Calendar

object AlarmScheduler {

    private fun pendingIntent(context: Context, alarm: Alarm): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarm.id)
            putExtra("label", alarm.label)
            putExtra("shake_to_stop", alarm.shakeToStop)
            putExtra("vibrate", alarm.vibrate)
            alarm.soundUri?.let { putExtra("sound_uri", it) }
        }
        return PendingIntent.getBroadcast(
            context, alarm.id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** Computes the next trigger time honoring specific-date or recurring days-of-week. */
    fun nextTriggerMillis(alarm: Alarm, now: Calendar = Calendar.getInstance()): Long {
        val cal = Calendar.getInstance()

        if (alarm.specificDateMillis != null) {
            cal.timeInMillis = alarm.specificDateMillis
            cal.set(Calendar.HOUR_OF_DAY, alarm.hour)
            cal.set(Calendar.MINUTE, alarm.minute)
            cal.set(Calendar.SECOND, 0)
            return cal.timeInMillis
        }

        cal.set(Calendar.HOUR_OF_DAY, alarm.hour)
        cal.set(Calendar.MINUTE, alarm.minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        if (alarm.daysOfWeek == 0) {
            // one-off, today or tomorrow
            if (cal.timeInMillis <= now.timeInMillis) cal.add(Calendar.DAY_OF_YEAR, 1)
            return cal.timeInMillis
        }

        // recurring: find the next matching day-of-week bit
        for (i in 0..7) {
            val candidate = cal.clone() as Calendar
            candidate.add(Calendar.DAY_OF_YEAR, i)
            val dow = candidate.get(Calendar.DAY_OF_WEEK) // 1=Sun..7=Sat
            val bit = dayOfWeekToBit(dow)
            val matches = (alarm.daysOfWeek and bit) != 0
            if (matches && (i > 0 || candidate.timeInMillis > now.timeInMillis)) {
                return candidate.timeInMillis
            }
        }
        return cal.timeInMillis
    }

    private fun dayOfWeekToBit(calendarDayOfWeek: Int): Int {
        // Calendar.SUNDAY=1 ... Calendar.SATURDAY=7  -> Mon=1,Tue=2,Wed=4,Thu=8,Fri=16,Sat=32,Sun=64
        return when (calendarDayOfWeek) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 4
            Calendar.THURSDAY -> 8
            Calendar.FRIDAY -> 16
            Calendar.SATURDAY -> 32
            Calendar.SUNDAY -> 64
            else -> 0
        }
    }

    fun schedule(context: Context, alarm: Alarm) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = nextTriggerMillis(alarm)
        val pi = pendingIntent(context, alarm)
        am.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, pi), pi)
    }

    fun cancel(context: Context, alarm: Alarm) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(context, alarm))
    }
}

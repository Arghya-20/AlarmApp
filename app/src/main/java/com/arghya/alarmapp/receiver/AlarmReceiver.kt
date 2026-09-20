package com.arghya.alarmapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.arghya.alarmapp.alarm.AlarmRingActivity
import com.arghya.alarmapp.alarm.AlarmRingService
import com.arghya.alarmapp.alarm.AlarmScheduler
import com.arghya.alarmapp.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, AlarmRingService::class.java).apply {
            putExtras(intent)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // Launch the ringing screen directly. A full-screen notification only
        // auto-launches when the device is locked; when unlocked, Android just
        // shows a heads-up notification and requires a tap. Since we're reacting
        // to an AlarmManager alarm, we get a temporary allowance to start an
        // activity from the background, so this covers the unlocked case too.
        val activityIntent = Intent(context, AlarmRingActivity::class.java).apply {
            putExtras(intent)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(activityIntent)

        // Reschedule if it's a recurring alarm (next matching day-of-week)
        val alarmId = intent.getIntExtra("alarm_id", -1)
        if (alarmId != -1) {
            CoroutineScope(Dispatchers.IO).launch {
                val dao = AppDatabase.get(context).alarmDao()
                val alarm = dao.getById(alarmId)
                if (alarm != null && alarm.daysOfWeek != 0 && alarm.enabled) {
                    AlarmScheduler.schedule(context, alarm)
                }
            }
        }
    }
}


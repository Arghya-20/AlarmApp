package com.arghya.alarmapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.arghya.alarmapp.alarm.AlarmScheduler
import com.arghya.alarmapp.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        CoroutineScope(Dispatchers.IO).launch {
            val dao = AppDatabase.get(context).alarmDao()
            val alarms = dao.getAll().first()
            alarms.filter { it.enabled }.forEach { AlarmScheduler.schedule(context, it) }
        }
    }
}

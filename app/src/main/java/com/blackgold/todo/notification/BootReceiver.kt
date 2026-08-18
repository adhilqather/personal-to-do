package com.blackgold.todo.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.blackgold.todo.data.TodoDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            // Reschedule all pending reminders after reboot
            CoroutineScope(Dispatchers.IO).launch {
                val db = TodoDatabase.getInstance(context)
                val reminders = db.todoDao().getActiveReminders()
                val scheduler = ReminderScheduler(context)
                reminders.forEach { todo ->
                    todo.reminderTime?.let { time ->
                        if (time > System.currentTimeMillis()) {
                            scheduler.scheduleReminder(todo)
                        }
                    }
                }
            }
        }
    }
}
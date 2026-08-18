package com.blackgold.todo.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.blackgold.todo.data.TodoDatabase
import com.blackgold.todo.data.TodoEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminder(todo: TodoEntity) {
        val reminderTime = todo.reminderTime ?: return
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(NotificationReceiver.EXTRA_TODO_ID, todo.id)
            putExtra(NotificationReceiver.EXTRA_TODO_TITLE, todo.title)
            putExtra(NotificationReceiver.EXTRA_TODO_DESCRIPTION, todo.description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            todo.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
        }
    }

    fun cancelReminder(todoId: Long) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            todoId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    companion object {
        fun combineDateTime(dateMillis: Long, timeMillis: Long): Long {
            val dateCal = Calendar.getInstance().apply { timeInMillis = dateMillis }
            val timeCal = Calendar.getInstance().apply { timeInMillis = timeMillis }
            val merged = Calendar.getInstance().apply {
                set(Calendar.YEAR, dateCal.get(Calendar.YEAR))
                set(Calendar.MONTH, dateCal.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, dateCal.get(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return merged.timeInMillis
        }

        // Static method for scheduling from fragments/activities
        fun scheduleReminder(
            context: Context,
            todoId: Long,
            title: String,
            reminderTime: Long
        ) {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra(NotificationReceiver.EXTRA_TODO_ID, todoId)
                putExtra(NotificationReceiver.EXTRA_TODO_TITLE, title)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                todoId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            }
        }

        // Reschedule all active reminders (call on boot or app start)
        fun rescheduleAllReminders(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = TodoDatabase.getInstance(context)
                val reminders = db.todoDao().getActiveReminders()
                reminders.forEach { todo ->
                    if (todo.reminderEnabled && todo.reminderTime != null) {
                        scheduleReminder(context, todo.id, todo.title, todo.reminderTime!!)
                    }
                }
            }
        }
    }
}

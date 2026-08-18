package com.blackgold.todo.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.blackgold.todo.R
import com.blackgold.todo.TodoApplication

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_TODO_ID = "extra_todo_id"
        const val EXTRA_TODO_TITLE = "extra_todo_title"
        const val EXTRA_TODO_DESCRIPTION = "extra_todo_description"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val todoId = intent.getLongExtra(EXTRA_TODO_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TODO_TITLE) ?: "Todo Reminder"
        val description = intent.getStringExtra(EXTRA_TODO_DESCRIPTION)

        // Open the app and navigate to the specific todo
        val openIntent = Intent(context, Class.forName("com.blackgold.todo.MainActivity")).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("todo_id", todoId)
        }

        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            todoId.toInt(),
            openIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(context, TodoApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.todo_reminder))
            .setContentText(title)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        if (description.isNullOrEmpty()) {
                            context.getString(R.string.your_task_is_due)
                        } else {
                            "$title\n${context.getString(R.string.your_task_is_due)}"
                        }
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setColor(0xD4AF37)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(android.net.Uri.parse("android.resource://${context.packageName}/raw/notification_sound"))
            .setVibrate(longArrayOf(0, 250, 250, 250))

        notificationManager.notify(todoId.toInt(), builder.build())
    }
}
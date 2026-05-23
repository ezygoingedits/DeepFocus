package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "AlarmReceiver"
        private const val CHANNEL_ID = "discipline_alarms_channel"
        private const val CHANNEL_NAME = "Discipline Alarms"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        Log.d(TAG, "Received broadcast with action: $action")

        if (action == "ACTION_DISMISS_NOTIFICATION") {
            val notificationId = intent.getIntExtra("notification_id", -1)
            if (notificationId != -1) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(notificationId)
                Log.d(TAG, "Notification $notificationId dismissed")
            }
            return
        }

        if (action == "ACTION_TRIGGER_ALARM" || action == Intent.ACTION_BOOT_COMPLETED) {
            if (action == Intent.ACTION_BOOT_COMPLETED) {
                // Alarms need to be rescheduled if the device reboots
                return
            }

            val id = intent.getIntExtra("id", 0)
            val type = intent.getStringExtra("type") ?: "task"
            val title = intent.getStringExtra("title") ?: "Discipline Reminder"
            val time = intent.getStringExtra("time") ?: ""

            Log.d(TAG, "Alarm triggered: $type $id - $title at $time")

            // Show Ongoing Notification
            showOngoingNotification(context, id, type, title, time)

            // For habits, they recur tomorrow! Re-schedule it.
            if (type == "habit" && time.isNotEmpty()) {
                AlarmScheduler.scheduleAlarm(context, id, type, title, time)
            }
        }
    }

    private fun showOngoingNotification(
        context: Context,
        id: Int,
        type: String,
        title: String,
        time: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Undeletable notifications and alarms for daily discipline"
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = (type + id).hashCode()

        // Create Dismiss Intent
        val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = "ACTION_DISMISS_NOTIFICATION"
            putExtra("notification_id", notificationId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create Content Intent (opens main activity)
        val openIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val openPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 1000,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val displayContent = if (type == "habit") {
            "Time to complete habit: $title"
        } else {
            "Action item cue: $title ($time)"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("DISCIPLINE INSTRUCTION")
            .setContentText(displayContent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true) // THIS MAKES IT UNDELETABLE BY SWIPING!
            .setAutoCancel(false)
            .setContentIntent(openPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "DISMISS ALARM",
                dismissPendingIntent
            )
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setColor(0xFFFF5252.toInt()) // High visibility color
        }

        notificationManager.notify(notificationId, builder.build())
    }
}

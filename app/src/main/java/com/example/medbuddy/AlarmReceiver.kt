package com.example.medbuddy

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import android.app.PendingIntent
import android.app.AlarmManager

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medicineName = intent.getStringExtra("medicineName") ?: "Medicine"
        val startDateMillis = intent.getLongExtra("startDateMillis", 0L)
        val totalDays = intent.getIntExtra("totalDays", 0)
        val doseIndex = intent.getIntExtra("doseIndex", 0)

        // Check if medicine duration expired
        val daysSinceStart = ((System.currentTimeMillis() - startDateMillis) / (1000 * 60 * 60 * 24)).toInt()
        if (totalDays > 0 && daysSinceStart >= totalDays) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val cancelIntent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                (medicineName + doseIndex).hashCode(),
                cancelIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            return
        }

        // Notification setup
        val channelId = "medbuddy_alarm"
        val channelName = "Medicine Reminders"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), null)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Medicine Reminder")
            .setContentText("Time to take: $medicineName")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .build()

        notificationManager.notify((medicineName + doseIndex + System.currentTimeMillis()).hashCode(), notification)
    }
}

package io.github.aloussase.booksdownloader.services

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

abstract class BaseApplicationService : Service() {
    abstract val CHANNEL_ID: String

    abstract val NOTIFICATION_CHANNEL_NAME: String

    /**
     * Create the pending intent for this service.
     */
    protected inline fun <reified T : Activity> createPendingIntent(): PendingIntent {
        return PendingIntent.getActivity(
            this,
            0,
            Intent(this, T::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Create the notification for this service.
     */
    protected fun createNotification(
        title: String,
        content: String,
        pendingIntent: PendingIntent,
        icon: Int,
    ): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setContentText(title)
            .setContentTitle(content)
            .setSmallIcon(icon)
            .build()
    }

    /**
     * Create the notification channel if necessary.
     */
    protected fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(serviceChannel)
        }
    }
}
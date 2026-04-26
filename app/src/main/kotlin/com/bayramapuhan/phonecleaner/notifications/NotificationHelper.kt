package com.bayramapuhan.phonecleaner.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bayramapuhan.phonecleaner.MainActivity
import com.bayramapuhan.phonecleaner.R

object NotificationHelper {
    const val CHANNEL_CLEANUP = "cleanup_reminders"
    const val NOTIF_STORAGE_LOW = 1001
    private const val ACCENT_COLOR = 0xFF06B6D4.toInt()

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val mgr = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_CLEANUP,
            context.getString(R.string.notif_channel_cleanup),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.notif_channel_cleanup_desc)
        }
        mgr.createNotificationChannel(channel)
    }

    fun showStorageLow(context: Context, freeText: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notif = NotificationCompat.Builder(context, CHANNEL_CLEANUP)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ACCENT_COLOR)
            .setContentTitle(context.getString(R.string.notif_storage_low_title))
            .setContentText(context.getString(R.string.notif_storage_low_body, freeText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        runCatching {
            NotificationManagerCompat.from(context).notify(NOTIF_STORAGE_LOW, notif)
        }
    }
}

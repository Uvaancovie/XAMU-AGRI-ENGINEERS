package com.example.xamu_wil_project.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.xamu_wil_project.R

object Notifier {
    private const val NOT_SYNCED = "NOT_SYNCED_CHANNEL_ID"
    private const val SYNCED = "SYNCED_CHANNEL_ID"

    fun createChannels(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(NotificationChannel(NOT_SYNCED, "Data Not Synced", NotificationManager.IMPORTANCE_DEFAULT))
            nm.createNotificationChannel(NotificationChannel(SYNCED, "Data Synced", NotificationManager.IMPORTANCE_LOW))
        }
    }

    fun showNotSynced(ctx: Context, pendingIntent: PendingIntent) {
        val builder = NotificationCompat.Builder(ctx, NOT_SYNCED)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Data Not Synced")
            .setContentText("Your data is not synced to the cloud")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        notify(ctx, 125, builder)
    }

    fun showSynced(ctx: Context, pendingIntent: PendingIntent) {
        val builder = NotificationCompat.Builder(ctx, SYNCED)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Data Synced")
            .setContentText("Your data has been synced to the cloud")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        notify(ctx, 126, builder)
    }

    private fun notify(ctx: Context, id: Int, builder: NotificationCompat.Builder) {
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        NotificationManagerCompat.from(ctx).notify(id, builder.build())
    }
}

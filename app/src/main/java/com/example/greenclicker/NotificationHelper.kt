package com.example.greenclicker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {
    fun createNotificationChannel(ctx: Context): String {
        val channelId = "green_clicker_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(channelId, "GreenClicker", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(channel)
        }
        return channelId
    }
}

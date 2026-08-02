package com.unasrolitas.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class UnasRolitasApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = constVal.CHANNEL_ID
            val name = "Reproducción de Música"
            val descriptionText = "Controles de reproducción para ¿Unas Rolitas?"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    object constVal {
        const val CHANNEL_ID = "unas_rolitas_playback_channel"
        const val NOTIFICATION_ID = 1001
    }
}

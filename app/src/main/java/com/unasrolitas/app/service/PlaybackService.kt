package com.unasrolitas.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.unasrolitas.app.R
import com.unasrolitas.app.player.MusicPlayerManager
import com.unasrolitas.app.util.AppLogger

class PlaybackService : MediaSessionService() {

    companion object {
        private const val CHANNEL_ID = "unasrolitas_playback"
        private const val NOTIFICATION_ID = 1001
    }

    private lateinit var musicPlayerManager: MusicPlayerManager

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        AppLogger.i("SERVICE", "PlaybackService.onCreate iniciado")

        createNotificationChannel()

        try {
            startForeground(
                NOTIFICATION_ID,
                createStartupNotification()
            )

            AppLogger.i(
                "SERVICE",
                "PlaybackService convertido a foreground correctamente"
            )
        } catch (e: Exception) {
            AppLogger.e(
                "SERVICE",
                "ERROR al ejecutar startForeground",
                e
            )
        }

        musicPlayerManager =
            MusicPlayerManager.getInstance(applicationContext)

        AppLogger.i(
            "SERVICE",
            "MusicPlayerManager obtenido por PlaybackService"
        )
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? {
        return musicPlayerManager.mediaSession
    }

    override fun onDestroy() {
        AppLogger.i("SERVICE", "PlaybackService.onDestroy")
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Reproducción de Unas Rolitas",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controles de reproducción de Unas Rolitas"
                setShowBadge(false)
            }

            val manager =
                getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(channel)
        }
    }

    private fun createStartupNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Unas Rolitas")
            .setContentText("Preparando reproducción…")
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}

package com.unasrolitas.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.ServiceInfo
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

        /*
         * IMPORTANTE:
         * El servicio fue iniciado mediante startForegroundService().
         * Android exige que pase a foreground rápidamente.
         *
         * No esperamos a que ExoPlayer comience a reproducir ni a obtener
         * el MusicPlayerManager para cumplir esta obligación.
         */
        val notification = createStartupNotification()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(
                    NOTIFICATION_ID,
                    notification
                )
            }

            AppLogger.i(
                "SERVICE",
                "PlaybackService puesto en foreground correctamente"
            )
        } catch (e: Exception) {
            AppLogger.e(
                "SERVICE",
                "ERROR al ejecutar startForeground()",
                e
            )
            throw e
        }

        /*
         * Solo después de cumplir la obligación del foreground service
         * obtenemos el MusicPlayerManager.
         */
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
        val session = musicPlayerManager.mediaSession

        AppLogger.i(
            "SERVICE",
            "onGetSession -> session=${session != null}"
        )

        return session
    }

    override fun onDestroy() {
        AppLogger.i(
            "SERVICE",
            "PlaybackService.onDestroy"
        )

        super.onDestroy()
    }

    private fun createStartupNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Unas Rolitas")
            .setContentText("Reproducción de música")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
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

            AppLogger.i(
                "SERVICE",
                "NotificationChannel creado/verificado"
            )
        }
    }
}

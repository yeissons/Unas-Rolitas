package com.unasrolitas.app.service

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.unasrolitas.app.player.MusicPlayerManager
import com.unasrolitas.app.util.AppLogger

@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {

    private lateinit var musicPlayerManager: MusicPlayerManager

    override fun onCreate() {
        super.onCreate()

        AppLogger.i(
            "SERVICE",
            "PlaybackService.onCreate iniciado"
        )

        /*
         * MusicPlayerManager contiene el ExoPlayer y la MediaSession.
         * MediaSessionService será responsable de exponer la sesión
         * al sistema Android y administrar la notificación multimedia.
         */
        musicPlayerManager =
            MusicPlayerManager.getInstance(applicationContext)

        AppLogger.i(
            "SERVICE",
            "MusicPlayerManager obtenido"
        )

        AppLogger.i(
            "SERVICE",
            "MediaSession disponible=${musicPlayerManager.mediaSession != null}"
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
}

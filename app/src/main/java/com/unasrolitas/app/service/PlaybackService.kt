package com.unasrolitas.app.service

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.unasrolitas.app.player.MusicPlayerManager

class PlaybackService : MediaSessionService() {

    private lateinit var musicPlayerManager: MusicPlayerManager

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        musicPlayerManager = MusicPlayerManager.getInstance(applicationContext)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return musicPlayerManager.mediaSession
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

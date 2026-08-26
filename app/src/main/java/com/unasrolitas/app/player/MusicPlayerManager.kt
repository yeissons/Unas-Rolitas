package com.unasrolitas.app.player

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.unasrolitas.app.MainActivity
import com.unasrolitas.app.audio.AudioDspManager
import com.unasrolitas.app.data.model.Song
import com.unasrolitas.app.util.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@OptIn(UnstableApi::class)
class MusicPlayerManager private constructor(private val context: Context) {

    private val _exoPlayer = ExoPlayer.Builder(context).build()
    val exoPlayer: ExoPlayer get() = _exoPlayer

    var mediaSession: MediaSession? = null
        private set

    val dspManager = AudioDspManager()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playlist = MutableStateFlow<List<Song>>(emptyList())
    val playlist: StateFlow<List<Song>> = _playlist.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null

    init {
        try {
            val sessionActivityIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val sessionActivityPendingIntent = PendingIntent.getActivity(
                context,
                0,
                sessionActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            mediaSession = MediaSession.Builder(context, _exoPlayer)
                .setId("UnasRolitasMediaSession")
                .setSessionActivity(sessionActivityPendingIntent)
                .build()

            AppLogger.i("PLAYER", "MediaSession creada")
        } catch (e: Exception) {
            AppLogger.e("PLAYER", "No se pudo crear MediaSession", e)
        }

        _exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying

                AppLogger.i(
                    "PLAYER",
                    "isPlaying=$isPlaying audioSessionId=${_exoPlayer.audioSessionId}"
                )

                if (isPlaying) {
                    startProgressTracker()

                    val audioSessionId = _exoPlayer.audioSessionId
                    if (audioSessionId > 0) {
                        AppLogger.i(
                            "PLAYER",
                            "AudioSession disponible: $audioSessionId"
                        )
                        dspManager.attachToAudioSession(audioSessionId)
                    } else {
                        AppLogger.w(
                            "PLAYER",
                            "ExoPlayer comenzó a reproducir sin AudioSession válida: $audioSessionId"
                        )
                    }
                } else {
                    stopProgressTracker()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val index = _exoPlayer.currentMediaItemIndex

                AppLogger.i(
                    "PLAYER",
                    "Transicion mediaItem index=$index reason=$reason"
                )

                if (index in _playlist.value.indices) {
                    _currentIndex.value = index
                    _currentSong.value = _playlist.value[index]
                    _durationMs.value = _playlist.value[index].durationMs
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                AppLogger.i(
                    "PLAYER",
                    "playbackState=$playbackState audioSessionId=${_exoPlayer.audioSessionId}"
                )

                if (playbackState == Player.STATE_READY) {
                    _durationMs.value =
                        if (_exoPlayer.duration > 0) {
                            _exoPlayer.duration
                        } else {
                            _currentSong.value?.durationMs ?: 0L
                        }

                    val audioSessionId = _exoPlayer.audioSessionId
                    if (audioSessionId > 0) {
                        dspManager.attachToAudioSession(audioSessionId)
                    }
                }
            }
        })
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = coroutineScope.launch {
            while (_isPlaying.value) {
                _currentPositionMs.value = _exoPlayer.currentPosition
                delay(500)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
    }

    fun setQueue(songs: List<Song>, startPosition: Int = 0, autoPlay: Boolean = true) {
        if (songs.isEmpty()) return

        _playlist.value = songs
        _currentIndex.value = startPosition
        val mediaItems = songs.map { song ->
            val metadata = MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)
                .setAlbumTitle(song.album)
                .setArtworkUri(song.artworkUri)
                .build()

            MediaItem.Builder()
                .setUri(song.uri)
                .setMediaId(song.id.toString())
                .setMediaMetadata(metadata)
                .build()
        }
        AppLogger.i(
            "PLAYER",
            "setQueue size=${songs.size} start=$startPosition autoPlay=$autoPlay"
        )

        _exoPlayer.setMediaItems(mediaItems, startPosition, 0L)
        _exoPlayer.prepare()
        _exoPlayer.playWhenReady = autoPlay
        if (startPosition in songs.indices) {
            _currentSong.value = songs[startPosition]
            _durationMs.value = songs[startPosition].durationMs
        }
    }

    fun insertSongAsNext(song: Song) {
        val currentList = _playlist.value.toMutableList()
        val insertIndex = if (currentList.isNotEmpty()) _currentIndex.value + 1 else 0
        currentList.add(insertIndex, song)
        _playlist.value = currentList

        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(song.artworkUri)
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(song.uri)
            .setMediaId(song.id.toString())
            .setMediaMetadata(metadata)
            .build()

        _exoPlayer.addMediaItem(insertIndex, mediaItem)
    }

    fun addSongToQueue(song: Song) {
        val currentList = _playlist.value.toMutableList()
        currentList.add(song)
        _playlist.value = currentList

        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(song.artworkUri)
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(song.uri)
            .setMediaId(song.id.toString())
            .setMediaMetadata(metadata)
            .build()

        _exoPlayer.addMediaItem(mediaItem)
    }

    fun play() {
        AppLogger.i("PLAYER", "play()")
        _exoPlayer.play()
    }

    fun pause() {
        AppLogger.i("PLAYER", "pause()")
        _exoPlayer.pause()
    }
    fun togglePlay() {
        if (_exoPlayer.isPlaying) pause() else play()
    }

    fun playNext() {
        if (_exoPlayer.hasNextMediaItem()) {
            _exoPlayer.seekToNextMediaItem()
        }
    }

    fun playPrevious() {
        if (_exoPlayer.currentPosition > 3000) {
            _exoPlayer.seekTo(0)
        } else if (_exoPlayer.hasPreviousMediaItem()) {
            _exoPlayer.seekToPreviousMediaItem()
        } else {
            _exoPlayer.seekTo(0)
        }
    }

    fun seekTo(positionMs: Long) {
        _exoPlayer.seekTo(positionMs)
        _currentPositionMs.value = positionMs
    }

    fun toggleShuffle() {
        val newShuffle = !_isShuffle.value
        _isShuffle.value = newShuffle
        _exoPlayer.shuffleModeEnabled = newShuffle
    }

    fun toggleRepeatMode() {
        val nextMode = when (_repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        _repeatMode.value = nextMode
        _exoPlayer.repeatMode = nextMode
    }

    fun setPlaybackSpeed(speed: Float) {
        _exoPlayer.setPlaybackSpeed(speed.coerceIn(0.25f, 4f))
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) return
        sleepTimerJob = coroutineScope.launch {
            delay(minutes * 60_000L)
            _exoPlayer.pause()
            _currentPositionMs.value = _exoPlayer.currentPosition
            sleepTimerJob = null
        }
    }

    fun release() {
        AppLogger.i("PLAYER", "release() iniciado")

        stopProgressTracker()

        sleepTimerJob?.cancel()
        sleepTimerJob = null

        dspManager.release()

        mediaSession?.release()
        mediaSession = null

        coroutineScope.cancel()

        _exoPlayer.release()

        synchronized(MusicPlayerManager::class.java) {
            if (INSTANCE === this) {
                INSTANCE = null
            }
        }

        AppLogger.i("PLAYER", "release() terminado")
    }

    companion object {
        @Volatile
        private var INSTANCE: MusicPlayerManager? = null

        fun getInstance(context: Context): MusicPlayerManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MusicPlayerManager(context.applicationContext).also {
                    INSTANCE = it
                    AppLogger.i("PLAYER", "MusicPlayerManager creado")
                }
            }
        }
    }
}

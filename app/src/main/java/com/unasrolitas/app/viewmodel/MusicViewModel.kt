package com.unasrolitas.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unasrolitas.app.data.model.AudioSettings
import com.unasrolitas.app.data.model.EqualizerProfile
import com.unasrolitas.app.data.model.Lyrics
import com.unasrolitas.app.data.model.Playlist
import com.unasrolitas.app.data.model.Song
import com.unasrolitas.app.data.repository.CoverArtRepository
import com.unasrolitas.app.data.repository.LyricsRepository
import com.unasrolitas.app.data.repository.MediaStoreRepository
import com.unasrolitas.app.data.repository.PreferencesRepository
import com.unasrolitas.app.player.MusicPlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    val playerManager = MusicPlayerManager.getInstance(application)

    private val mediaStoreRepository = MediaStoreRepository(application)
    private val lyricsRepository = LyricsRepository(application)
    private val coverArtRepository = CoverArtRepository(application)
    private val prefsRepository = PreferencesRepository(application)

    private val _allSongs = MutableStateFlow<List<Song>>(emptyList())
    val allSongs: StateFlow<List<Song>> = _allSongs.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeTab = MutableStateFlow("SONGS") // SONGS, PLAYLISTS, FAVORITES
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    private val _audioSettings = MutableStateFlow(prefsRepository.getAudioSettings())
    val audioSettings: StateFlow<AudioSettings> = _audioSettings.asStateFlow()

    private val _activeLyrics = MutableStateFlow<Lyrics?>(null)
    val activeLyrics: StateFlow<Lyrics?> = _activeLyrics.asStateFlow()

    private val _sleepTimerMinutes = MutableStateFlow(0)
    val sleepTimerMinutes: StateFlow<Int> = _sleepTimerMinutes.asStateFlow()

    private val _hasStoragePermission = MutableStateFlow(false)
    val hasStoragePermission: StateFlow<Boolean> = _hasStoragePermission.asStateFlow()

    private val _selectedSongForMenu = MutableStateFlow<Song?>(null)
    val selectedSongForMenu: StateFlow<Song?> = _selectedSongForMenu.asStateFlow()

    val currentSong: StateFlow<Song?> = playerManager.currentSong
    val isPlaying: StateFlow<Boolean> = playerManager.isPlaying
    val queue: StateFlow<List<Song>> = playerManager.playlist
    val currentIndex: StateFlow<Int> = playerManager.currentIndex
    val currentPositionMs: StateFlow<Long> = playerManager.currentPositionMs
    val durationMs: StateFlow<Long> = playerManager.durationMs
    val isShuffle: StateFlow<Boolean> = playerManager.isShuffle
    val repeatMode: StateFlow<Int> = playerManager.repeatMode

    val equalizerPresets = listOf(
        EqualizerProfile("Salsa & Cumbia", listOf(4f, 2f, -1f, 3f, 5f)),
        EqualizerProfile("Rock", listOf(5f, 3f, -2f, 4f, 6f)),
        EqualizerProfile("Pop", listOf(1f, 3f, 5f, 3f, 1f)),
        EqualizerProfile("Reggaeton", listOf(6f, 4f, 1f, 2f, 3f)),
        EqualizerProfile("Acústico", listOf(3f, 1f, 2f, 4f, 3f)),
        EqualizerProfile("Plano / Normal", listOf(0f, 0f, 0f, 0f, 0f))
    )

    init {
        applyAudioSettingsToDsp(_audioSettings.value)

        viewModelScope.launch {
            currentSong.collect { song ->
                if (song != null) {
                    loadSongInfoAndLyrics(song)
                } else {
                    _activeLyrics.value = null
                }
            }
        }
    }

    fun setPermissionGranted(granted: Boolean) {
        _hasStoragePermission.value = granted
        if (granted) {
            loadLibrary()
        }
    }

    fun loadLibrary() {
        viewModelScope.launch {
            val songs = mediaStoreRepository.getAllSongs()
            _allSongs.value = songs
            _playlists.value = mediaStoreRepository.getPlaylists(songs)

            if (playerManager.playlist.value.isEmpty() && songs.isNotEmpty()) {
                playerManager.setQueue(songs, 0, autoPlay = false)
            }
        }
    }

    private fun loadSongInfoAndLyrics(song: Song) {
        viewModelScope.launch {
            // Load real artwork asynchronously if needed
            val artUri = coverArtRepository.getArtworkForSong(song)
            if (artUri != null && artUri != song.artworkUri) {
                val updatedSong = song.copy(artworkUri = artUri)
                _allSongs.value = _allSongs.value.map { if (it.id == song.id) updatedSong else it }
            }

            // Load real LRC lyrics asynchronously from LRCLIB or cache
            val realLyrics = lyricsRepository.getLyricsForSong(song)
            _activeLyrics.value = realLyrics
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setActiveTab(tab: String) {
        _activeTab.value = tab
    }

    fun playSong(song: Song) {
        val list = _allSongs.value
        val index = list.indexOfFirst { it.id == song.id }
        if (index != -1) {
            playerManager.setQueue(list, index, autoPlay = true)
        } else {
            playerManager.setQueue(listOf(song), 0, autoPlay = true)
        }
    }

    fun playNextSong(song: Song) {
        playerManager.insertSongAsNext(song)
    }

    fun addSongToQueue(song: Song) {
        playerManager.addSongToQueue(song)
    }

    fun playExternalUri(uri: android.net.Uri) {
        val externalSong = Song(
            id = System.currentTimeMillis(),
            uri = uri,
            title = uri.lastPathSegment ?: "Archivo Externo",
            artist = "Audio Externo",
            album = "Reproducción Externa",
            albumId = 0,
            durationMs = 0,
            year = null,
            artworkUri = null,
            mimeType = "audio/*",
            sizeBytes = 0,
            filePath = "",
            isFavorite = false
        )

        playerManager.setQueue(
            songs = listOf(externalSong),
            startPosition = 0,
            autoPlay = true
        )
    }

    fun playQueueIndex(index: Int) {
        val q = queue.value
        if (index in q.indices) {
            playerManager.setQueue(q, index)
        }
    }

    fun togglePlay() = playerManager.togglePlay()
    fun playNext() = playerManager.playNext()
    fun playPrevious() = playerManager.playPrevious()
    fun seekTo(positionMs: Long) = playerManager.seekTo(positionMs)
    fun toggleShuffle() = playerManager.toggleShuffle()
    fun toggleRepeatMode() = playerManager.toggleRepeatMode()

    fun toggleFavorite(song: Song) {
        val newFavState = prefsRepository.toggleFavoriteSongId(song.id)
        val updated = _allSongs.value.map {
            if (it.id == song.id) it.copy(isFavorite = newFavState) else it
        }
        _allSongs.value = updated
        viewModelScope.launch {
            _playlists.value = mediaStoreRepository.getPlaylists(updated)
        }
    }

    fun setSelectedSongForMenu(song: Song?) {
        _selectedSongForMenu.value = song
    }

    fun setEqualizerPreset(profile: EqualizerProfile) {
        val updated = _audioSettings.value.copy(
            activePreset = profile.name,
            bandGains = profile.bandGains
        )
        _audioSettings.value = updated
        prefsRepository.saveAudioSettings(updated)
        applyAudioSettingsToDsp(updated)
    }

    fun setBandGain(bandIndex: Int, gain: Float) {
        val currentGains = _audioSettings.value.bandGains.toMutableList()
        if (bandIndex in currentGains.indices) {
            currentGains[bandIndex] = gain
            val updated = _audioSettings.value.copy(
                activePreset = "Personalizado",
                bandGains = currentGains
            )
            _audioSettings.value = updated
            prefsRepository.saveAudioSettings(updated)
            applyAudioSettingsToDsp(updated)
        }
    }

    fun setBassBoost(strength: Int) {
        val updated = _audioSettings.value.copy(bassBoost = strength)
        _audioSettings.value = updated
        prefsRepository.saveAudioSettings(updated)
        playerManager.dspManager.setBassBoost(strength.toShort())
    }

    fun setVirtualizer(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        val updated = _audioSettings.value.copy(virtualizer = clamped)
        _audioSettings.value = updated
        prefsRepository.saveAudioSettings(updated)
        playerManager.dspManager.setVirtualizer(clamped.toShort())
    }

    fun toggleEqualizer(enabled: Boolean) {
        val updated = _audioSettings.value.copy(isEqualizerEnabled = enabled)
        _audioSettings.value = updated
        prefsRepository.saveAudioSettings(updated)
        playerManager.dspManager.setEnabled(enabled)
    }

    private fun applyAudioSettingsToDsp(settings: AudioSettings) {
        playerManager.dspManager.setEnabled(settings.isEqualizerEnabled)
        if (!settings.isEqualizerEnabled) return
        settings.bandGains.forEachIndexed { band, gain ->
            val levelMb = (gain * 100).toInt().toShort()
            playerManager.dspManager.setBandGain(band.toShort(), levelMb)
        }
        playerManager.dspManager.setBassBoost(settings.bassBoost.coerceIn(0, 1000).toShort())
        playerManager.dspManager.setVirtualizer(settings.virtualizer.coerceIn(0, 1000).toShort())
    }

    fun setSleepTimer(minutes: Int) {
        val normalized = minutes.coerceAtLeast(0)
        _sleepTimerMinutes.value = normalized
        playerManager.setSleepTimer(normalized)
    }
}

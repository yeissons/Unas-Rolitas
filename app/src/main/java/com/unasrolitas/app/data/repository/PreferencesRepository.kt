package com.unasrolitas.app.data.repository

import android.content.Context
import com.unasrolitas.app.data.model.AudioSettings

class PreferencesRepository(context: Context) {

    private val prefs = context.getSharedPreferences(
        "unas_rolitas_prefs",
        Context.MODE_PRIVATE
    )

    fun getFavoriteSongIds(): Set<Long> {
        return prefs.getStringSet("favorite_song_ids", emptySet())
            ?.mapNotNull { it.toLongOrNull() }
            ?.toSet()
            ?: emptySet()
    }

    fun toggleFavoriteSongId(id: Long): Boolean {
        val current = getFavoriteSongIds().toMutableSet()

        val isFavorite = if (current.contains(id)) {
            current.remove(id)
            false
        } else {
            current.add(id)
            true
        }

        prefs.edit()
            .putStringSet(
                "favorite_song_ids",
                current.map { it.toString() }.toSet()
            )
            .apply()

        return isFavorite
    }

    fun getPlayCounts(): Map<Long, Int> {
        return prefs.getStringSet("play_counts", emptySet())
            ?.mapNotNull { entry ->
                val parts = entry.split(":")
                if (parts.size == 2) {
                    val id = parts[0].toLongOrNull()
                    val count = parts[1].toIntOrNull()
                    if (id != null && count != null) id to count else null
                } else {
                    null
                }
            }
            ?.toMap()
            ?: emptyMap()
    }

    fun incrementPlayCount(id: Long) {
        val counts = getPlayCounts().toMutableMap()
        counts[id] = (counts[id] ?: 0) + 1

        prefs.edit()
            .putStringSet(
                "play_counts",
                counts.map { "${it.key}:${it.value}" }.toSet()
            )
            .apply()
    }

    fun getRecentlyPlayed(): List<Long> {
        return prefs.getString("recently_played", "")
            ?.split(",")
            ?.mapNotNull { it.toLongOrNull() }
            ?.filter { it > 0 }
            ?: emptyList()
    }

    fun registerRecentlyPlayed(id: Long) {
        val current = getRecentlyPlayed()
            .filter { it != id }
            .toMutableList()

        current.add(0, id)

        prefs.edit()
            .putString(
                "recently_played",
                current.take(100).joinToString(",")
            )
            .apply()
    }

    // ------------------------------------------------------------
    // Playlists persistentes creadas por el usuario
    // ------------------------------------------------------------

    fun getUserPlaylists(): List<com.unasrolitas.app.data.model.Playlist> {
        val raw = prefs.getStringSet("user_playlists", emptySet()) ?: emptySet()

        val playlists = raw.mapNotNull { entry ->
            val parts = entry.split("|")

            if (parts.size < 3) return@mapNotNull null

            val id = parts[0]
            val name = parts[1]

            // Una playlist solamente puede existir si fue creada
            // explícitamente por el usuario o importada desde un archivo.
            if (
                id.isBlank() ||
                name.isBlank() ||
                !(id.startsWith("user_") || id.startsWith("file_"))
            ) {
                return@mapNotNull null
            }

            val ids = parts[2]
                .split(",")
                .mapNotNull { it.toLongOrNull() }

            val isExternalFile = parts.getOrNull(3) == "external"

            val sourceUri = parts.getOrNull(4)
                ?.takeIf { it.isNotBlank() }
                ?.let { android.net.Uri.parse(it) }

            val sourceFormat = parts.getOrNull(5)
                ?.takeIf { it.isNotBlank() }

            com.unasrolitas.app.data.model.Playlist(
                id = id,
                name = name,
                description = if (isExternalFile) {
                    "Playlist importada"
                } else {
                    "Lista de reproducción"
                },
                songIds = ids,
                isSystemPlaylist = false,
                isExternalFile = isExternalFile,
                sourceUri = sourceUri,
                sourceFormat = sourceFormat
            )
        }.sortedBy { it.name.lowercase() }

        // Sustituye el contenido almacenado por únicamente las
        // playlists válidas del modelo actual.
        saveUserPlaylists(playlists)

        return playlists
    }


    fun saveUserPlaylists(
        playlists: List<com.unasrolitas.app.data.model.Playlist>
    ) {
        val raw = playlists.map { playlist ->
            buildString {
                append(playlist.id)
                append("|")
                append(playlist.name)
                append("|")
                append(playlist.songIds.joinToString(","))

                if (playlist.isExternalFile) {
                    append("|external")
                    append("|")
                    append(playlist.sourceUri?.toString() ?: "")
                    append("|")
                    append(playlist.sourceFormat ?: "")
                }
            }
        }.toSet()

        prefs.edit()
            .putStringSet("user_playlists", raw)
            .apply()
    }

    fun createPlaylist(name: String): com.unasrolitas.app.data.model.Playlist? {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return null

        val current = getUserPlaylists().toMutableList()

        if (current.any { it.name.equals(cleanName, ignoreCase = true) }) {
            return null
        }

        val playlist = com.unasrolitas.app.data.model.Playlist(
            id = "user_${System.currentTimeMillis()}",
            name = cleanName,
            description = "Lista de reproducción",
            songIds = emptyList(),
            isSystemPlaylist = false
        )

        current.add(playlist)
        saveUserPlaylists(current)
        return playlist
    }

    fun addSongToPlaylist(playlistId: String, songId: Long): Boolean {
        val current = getUserPlaylists().toMutableList()
        val index = current.indexOfFirst { it.id == playlistId }

        if (index < 0) return false
        if (songId in current[index].songIds) return true

        current[index] = current[index].copy(
            songIds = current[index].songIds + songId
        )

        saveUserPlaylists(current)
        return true
    }

    fun removeSongFromPlaylist(playlistId: String, songId: Long): Boolean {
        val current = getUserPlaylists().toMutableList()
        val index = current.indexOfFirst { it.id == playlistId }

        if (index < 0) return false

        current[index] = current[index].copy(
            songIds = current[index].songIds.filterNot { it == songId }
        )

        saveUserPlaylists(current)
        return true
    }

    fun renamePlaylist(playlistId: String, newName: String): Boolean {
        val cleanName = newName.trim()
        if (cleanName.isBlank()) return false

        val current = getUserPlaylists().toMutableList()
        val index = current.indexOfFirst { it.id == playlistId }

        if (index < 0) return false

        if (current.any {
                it.id != playlistId &&
                it.name.equals(cleanName, ignoreCase = true)
            }) {
            return false
        }

        current[index] = current[index].copy(name = cleanName)
        saveUserPlaylists(current)
        return true
    }

    fun deletePlaylist(playlistId: String): Boolean {
        val current = getUserPlaylists()
        val updated = current.filterNot { it.id == playlistId }

        if (updated.size == current.size) return false

        saveUserPlaylists(updated)
        return true
    }

    fun getAudioSettings(): AudioSettings {
        return AudioSettings(
            isEqualizerEnabled = prefs.getBoolean(
                "eq_enabled",
                false
            ),
            activePreset = prefs.getString(
                "eq_preset",
                "Plano / Normal"
            ) ?: "Plano / Normal",
            bandGains = prefs.getString(
                "eq_bands",
                "0,0,0,0,0"
            )!!
                .split(",")
                .mapNotNull { it.toFloatOrNull() }
                .let {
                    if (it.size == 5) it
                    else listOf(0f, 0f, 0f, 0f, 0f)
                },
            bassBoost = prefs.getInt("bass_boost", 0),
            virtualizer = prefs.getInt("virtualizer", 0)
        )
    }

    fun saveAudioSettings(settings: AudioSettings) {
        prefs.edit()
            .putBoolean(
                "eq_enabled",
                settings.isEqualizerEnabled
            )
            .putString(
                "eq_preset",
                settings.activePreset
            )
            .putString(
                "eq_bands",
                settings.bandGains.joinToString(",")
            )
            .putInt(
                "bass_boost",
                settings.bassBoost
            )
            .putInt(
                "virtualizer",
                settings.virtualizer
            )
            .apply()
    }
}

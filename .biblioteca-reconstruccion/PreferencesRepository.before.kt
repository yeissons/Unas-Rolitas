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

package com.unasrolitas.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.unasrolitas.app.data.model.AudioSettings
import org.json.JSONArray

class PreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("unas_rolitas_prefs", Context.MODE_PRIVATE)

    fun getFavoriteSongIds(): Set<Long> {
        val stringSet = prefs.getStringSet("favorite_song_ids", emptySet()) ?: emptySet()
        return stringSet.mapNotNull { it.toLongOrNull() }.toSet()
    }

    fun toggleFavoriteSongId(songId: Long): Boolean {
        val current = getFavoriteSongIds().toMutableSet()
        val isFav = if (current.contains(songId)) {
            current.remove(songId)
            false
        } else {
            current.add(songId)
            true
        }
        prefs.edit().putStringSet("favorite_song_ids", current.map { it.toString() }.toSet()).apply()
        return isFav
    }

    fun saveAudioSettings(settings: AudioSettings) {
        val jsonGains = JSONArray(settings.bandGains).toString()
        prefs.edit()
            .putBoolean("eq_enabled", settings.isEqualizerEnabled)
            .putString("eq_preset", settings.activePreset)
            .putString("eq_gains", jsonGains)
            .putInt("eq_bass", settings.bassBoost)
            .putInt("eq_virt", settings.virtualizer)
            .apply()
    }

    fun getAudioSettings(): AudioSettings {
        val enabled = prefs.getBoolean("eq_enabled", true)
        val preset = prefs.getString("eq_preset", "Salsa & Cumbia") ?: "Salsa & Cumbia"
        val gainsStr = prefs.getString("eq_gains", null)
        val bass = prefs.getInt("eq_bass", 200)
        val virt = prefs.getInt("eq_virt", 100)

        val gains = mutableListOf<Float>()
        if (!gainsStr.isNullOrEmpty()) {
            try {
                val array = JSONArray(gainsStr)
                for (i in 0 until array.length()) {
                    gains.add(array.getDouble(i).toFloat())
                }
            } catch (_: Exception) {}
        }
        if (gains.size != 5) {
            gains.clear()
            gains.addAll(listOf(4f, 2f, -1f, 3f, 5f))
        }

        return AudioSettings(
            isEqualizerEnabled = enabled,
            activePreset = preset,
            bandGains = gains,
            bassBoost = bass,
            virtualizer = virt
        )
    }
}

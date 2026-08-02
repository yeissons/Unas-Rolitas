package com.unasrolitas.app.audio

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log

class AudioDspManager {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    fun attachToAudioSession(audioSessionId: Int) {
        if (audioSessionId == 0) return
        release()
        try {
            equalizer = Equalizer(0, audioSessionId).apply { enabled = true }
            bassBoost = BassBoost(0, audioSessionId).apply { enabled = true }
            virtualizer = Virtualizer(0, audioSessionId).apply { enabled = true }
        } catch (e: Exception) {
            Log.e("AudioDspManager", "Error initializing audio effects", e)
            release()
        }
    }

    fun setEnabled(enabled: Boolean) {
        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
            virtualizer?.enabled = enabled
        } catch (e: Exception) {
            Log.e("AudioDspManager", "Failed to change audio effects state", e)
        }
    }

    fun setBandGain(band: Short, levelMb: Short) {
        try {
            equalizer?.setBandLevel(band, levelMb)
        } catch (e: Exception) {
            Log.e("AudioDspManager", "Failed to set band level", e)
        }
    }

    fun setBassBoost(strength: Short) {
        try {
            bassBoost?.setStrength(strength.coerceIn(0, 1000).toShort())
        } catch (e: Exception) {
            Log.e("AudioDspManager", "Failed to set bass boost", e)
        }
    }

    fun setVirtualizer(strength: Short) {
        try {
            virtualizer?.setStrength(strength.coerceIn(0, 1000).toShort())
        } catch (e: Exception) {
            Log.e("AudioDspManager", "Failed to set virtualizer", e)
        }
    }

    fun release() {
        equalizer?.release()
        bassBoost?.release()
        virtualizer?.release()
        equalizer = null
        bassBoost = null
        virtualizer = null
    }
}

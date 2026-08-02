package com.unasrolitas.app.data.model

data class EqualizerProfile(
    val name: String,
    val bandGains: List<Float>, // 60Hz, 230Hz, 910Hz, 3600Hz, 14000Hz
    val isPreset: Boolean = true
)

data class AudioSettings(
    val isEqualizerEnabled: Boolean = true,
    val activePreset: String = "Normal",
    val bandGains: List<Float> = listOf(0f, 0f, 0f, 0f, 0f),
    val bassBoost: Int = 300, // 0 - 1000 millibels
    val virtualizer: Int = 200, // 0 - 1000 millibels
    val reverbPreset: Int = 0,
    val playbackSpeed: Float = 1.0f,
    val pitch: Float = 1.0f,
    val isCrossfadeEnabled: Boolean = false,
    val crossfadeDurationSec: Int = 3,
    val gaplessPlayback: Boolean = true,
    val replayGain: Boolean = false
)

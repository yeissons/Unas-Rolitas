package com.unasrolitas.app.audio

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import com.unasrolitas.app.util.AppLogger

/**
 * Administrador seguro de efectos DSP.
 *
 * Los AudioEffect dependen directamente del audioSessionId de ExoPlayer.
 * No se deben crear/recrear innecesariamente en cada cambio de estado.
 */
class AudioDspManager {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    private var currentAudioSessionId: Int = 0

    private var enabled: Boolean = true
    private val pendingBandGains = MutableList(5) { 0f }
    private var pendingBassBoost: Int = 0
    private var pendingVirtualizer: Int = 0

    @Synchronized
    fun attachToAudioSession(audioSessionId: Int) {
        if (audioSessionId <= 0) {
            AppLogger.w(
                "DSP",
                "AudioSession inválido: $audioSessionId"
            )
            return
        }

        if (currentAudioSessionId == audioSessionId &&
            (equalizer != null || bassBoost != null || virtualizer != null)
        ) {
            applyPendingSettings()
            return
        }

        AppLogger.i(
            "DSP",
            "Conectando DSP a audioSessionId=$audioSessionId"
        )

        releaseEffectsOnly()

        currentAudioSessionId = audioSessionId

        try {
            equalizer = runCatching {
                Equalizer(0, audioSessionId)
            }.getOrNull()

            bassBoost = runCatching {
                BassBoost(0, audioSessionId)
            }.getOrNull()

            virtualizer = runCatching {
                Virtualizer(0, audioSessionId)
            }.getOrNull()

            AppLogger.i(
                "DSP",
                "Efectos creados: EQ=${equalizer != null}, " +
                    "BassBoost=${bassBoost != null}, " +
                    "Virtualizer=${virtualizer != null}"
            )

            applyPendingSettings()
        } catch (e: Exception) {
            AppLogger.e(
                "DSP",
                "Error creando AudioEffects para session=$audioSessionId",
                e
            )
            releaseEffectsOnly()
            currentAudioSessionId = 0
        }
    }

    @Synchronized
    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        applyPendingSettings()
    }

    @Synchronized
    fun setBandGain(band: Short, levelMb: Short) {
        val index = band.toInt()

        if (index !in pendingBandGains.indices) {
            AppLogger.w(
                "DSP",
                "Banda fuera de rango: $index"
            )
            return
        }

        pendingBandGains[index] = levelMb.toFloat() / 100f

        try {
            equalizer?.let { eq ->
                if (index < eq.numberOfBands) {
                    val range = eq.bandLevelRange
                    val clamped = levelMb
                        .toInt()
                        .coerceIn(range[0].toInt(), range[1].toInt())
                        .toShort()

                    eq.setBandLevel(band, clamped)
                }
            }
        } catch (e: Exception) {
            AppLogger.e(
                "DSP",
                "No se pudo cambiar banda=$index nivel=$levelMb",
                e
            )
        }
    }

    @Synchronized
    fun setBassBoost(strength: Short) {
        val value = strength
            .toInt()
            .coerceIn(0, 1000)

        pendingBassBoost = value

        try {
            bassBoost?.setStrength(value.toShort())
        } catch (e: Exception) {
            AppLogger.e(
                "DSP",
                "No se pudo cambiar BassBoost=$value",
                e
            )
        }
    }

    @Synchronized
    fun setVirtualizer(strength: Short) {
        val value = strength
            .toInt()
            .coerceIn(0, 1000)

        pendingVirtualizer = value

        try {
            virtualizer?.setStrength(value.toShort())
        } catch (e: Exception) {
            AppLogger.e(
                "DSP",
                "No se pudo cambiar Virtualizer=$value",
                e
            )
        }
    }

    @Synchronized
    private fun applyPendingSettings() {
        try {
            equalizer?.let { eq ->
                eq.enabled = enabled

                val range = eq.bandLevelRange

                pendingBandGains.forEachIndexed { index, gain ->
                    if (index < eq.numberOfBands) {
                        val requestedMb = (gain * 100f).toInt()
                        val clamped = requestedMb
                            .coerceIn(
                                range[0].toInt(),
                                range[1].toInt()
                            )
                            .toShort()

                        eq.setBandLevel(index.toShort(), clamped)
                    }
                }
            }
        } catch (e: Exception) {
            AppLogger.e(
                "DSP",
                "Error aplicando configuración del ecualizador",
                e
            )
        }

        try {
            bassBoost?.let {
                it.enabled = enabled
                it.setStrength(
                    pendingBassBoost
                        .coerceIn(0, 1000)
                        .toShort()
                )
            }
        } catch (e: Exception) {
            AppLogger.e(
                "DSP",
                "Error aplicando BassBoost",
                e
            )
        }

        try {
            virtualizer?.let {
                it.enabled = enabled
                it.setStrength(
                    pendingVirtualizer
                        .coerceIn(0, 1000)
                        .toShort()
                )
            }
        } catch (e: Exception) {
            AppLogger.e(
                "DSP",
                "Error aplicando Virtualizer",
                e
            )
        }
    }

    @Synchronized
    private fun releaseEffectsOnly() {
        runCatching {
            equalizer?.release()
        }

        runCatching {
            bassBoost?.release()
        }

        runCatching {
            virtualizer?.release()
        }

        equalizer = null
        bassBoost = null
        virtualizer = null
    }

    @Synchronized
    fun release() {
        AppLogger.i(
            "DSP",
            "Liberando DSP audioSessionId=$currentAudioSessionId"
        )

        releaseEffectsOnly()
        currentAudioSessionId = 0
    }
}

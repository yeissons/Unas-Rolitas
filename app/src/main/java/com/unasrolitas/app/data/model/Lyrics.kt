package com.unasrolitas.app.data.model

data class LyricLine(
    val timestampMs: Long,
    val text: String,
    val translation: String? = null
)

data class Lyrics(
    val songId: Long,
    val isSynced: Boolean,
    val lines: List<LyricLine>,
    val plainText: String,
    val source: String = "LRC"
)

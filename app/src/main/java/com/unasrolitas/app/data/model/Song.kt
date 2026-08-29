package com.unasrolitas.app.data.model

import android.net.Uri

data class Song(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val genre: String? = null,
    val year: Int? = null,
    val artworkUri: Uri? = null,
    val mimeType: String? = "audio/mpeg",
    val bitrateKbps: Int? = null,
    val sampleRateHz: Int? = null,
    val bitDepth: Int? = null,
    val composer: String? = null,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val sizeBytes: Long = 0L,
    val dateModified: Long = 0L,
    val dateAdded: Long = 0L,
    val filePath: String = ""
)

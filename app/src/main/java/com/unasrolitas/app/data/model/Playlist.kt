package com.unasrolitas.app.data.model

import android.net.Uri

data class Playlist(
    val id: String,
    val name: String,
    val description: String,
    val songIds: List<Long>,
    val coverUri: Uri? = null,
    val isSystemPlaylist: Boolean = false
)

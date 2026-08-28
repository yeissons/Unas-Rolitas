package com.unasrolitas.app.data.model

import android.net.Uri

data class Playlist(
    val id: String,
    val name: String,
    val description: String = "",
    val songIds: List<Long>,
    val coverUri: Uri? = null,
    val isSystemPlaylist: Boolean = false,

    // Playlist creada dentro de ¿Unas Rolitas? o importada desde un archivo.
    val isExternalFile: Boolean = false,

    // Uri persistente del archivo de playlist cuando procede del almacenamiento.
    val sourceUri: Uri? = null,

    // Extensión/formato original: m3u, m3u8, pls o wpl.
    val sourceFormat: String? = null
)

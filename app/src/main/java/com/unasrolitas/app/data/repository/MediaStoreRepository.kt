package com.unasrolitas.app.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.unasrolitas.app.data.model.Playlist
import com.unasrolitas.app.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreRepository(private val context: Context) {

    private val prefsRepository = PreferencesRepository(context)

    suspend fun getAllSongs(): List<Song> = withContext(Dispatchers.IO) {
        val songList = mutableListOf<Song>()
        val favoriteIds = prefsRepository.getFavoriteSongIds()

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.GENRE
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
                val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                val genreColumn = cursor.getColumnIndex(MediaStore.Audio.Media.GENRE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val contentUri = ContentUris.withAppendedId(collection, id)
                    val albumId = cursor.getLong(albumIdColumn)
                    val artworkUri = if (albumId > 0) {
                        ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"),
                            albumId
                        )
                    } else null

                    songList.add(
                        Song(
                            id = id,
                            uri = contentUri,
                            title = cursor.getString(titleColumn)?.takeIf { it.isNotBlank() } ?: "Desconocido",
                            artist = cursor.getString(artistColumn)?.takeIf { it.isNotBlank() } ?: "<Artista Desconocido>",
                            album = cursor.getString(albumColumn)?.takeIf { it.isNotBlank() } ?: "<Álbum Desconocido>",
                            albumId = albumId,
                            durationMs = cursor.getLong(durationColumn).coerceAtLeast(0L),
                            genre = genreColumn.takeIf { it >= 0 }?.let { cursor.getString(it) },
                            year = cursor.getInt(yearColumn).takeIf { it > 0 },
                            artworkUri = artworkUri,
                            mimeType = cursor.getString(mimeColumn),
                            sizeBytes = cursor.getLong(sizeColumn).coerceAtLeast(0L),
                            dateModified = cursor.getLong(dateModifiedColumn).coerceAtLeast(0L),
                            isFavorite = id in favoriteIds
                        )
                    )
                }
            }
        } catch (_: SecurityException) {
            // Permission is handled by PermissionHandler; return an empty library if revoked.
        } catch (_: Exception) {
            // A malformed or unavailable MediaStore entry must not crash the library screen.
        }

        songList
    }

    suspend fun getPlaylists(songs: List<Song>): List<Playlist> =
        withContext(Dispatchers.IO) {
            if (songs.isEmpty()) return@withContext emptyList()

            // Las playlists mostradas por la biblioteca deben representar
            // agrupaciones reales y reproducibles, no listas ficticias.
            val playlists = mutableListOf<Playlist>()

            val favoriteSongs = songs.filter { it.isFavorite }
            if (favoriteSongs.isNotEmpty()) {
                playlists.add(
                    Playlist(
                        id = "pl_favorites",
                        name = "Mis Favoritas",
                        description = "Canciones marcadas con me gusta",
                        songIds = favoriteSongs.map { it.id },
                        isSystemPlaylist = true
                    )
                )
            }

            songs.groupBy { it.album.trim() }
                .filterKeys {
                    it.isNotBlank() && !it.equals(
                        "<Álbum Desconocido>",
                        ignoreCase = true
                    )
                }
                .toSortedMap(String.CASE_INSENSITIVE_ORDER)
                .forEach { (album, albumSongs) ->
                    playlists.add(
                        Playlist(
                            id = "pl_album_${album.hashCode()}",
                            name = album,
                            description = "Álbum con ${albumSongs.size} rolitas",
                            songIds = albumSongs
                                .sortedBy { it.title.trim().lowercase() }
                                .map { it.id },
                            isSystemPlaylist = true
                        )
                    )
                }

            playlists
        }
}

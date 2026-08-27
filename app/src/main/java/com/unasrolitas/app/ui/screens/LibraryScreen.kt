package com.unasrolitas.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.unasrolitas.app.data.model.Playlist
import com.unasrolitas.app.data.model.Song
import com.unasrolitas.app.ui.components.SongRowItem
import com.unasrolitas.app.ui.theme.*

@Composable
fun LibraryScreen(
    songs: List<Song>,
    playlists: List<Playlist>,
    currentSong: Song?,
    isPlaying: Boolean,
    searchQuery: String,
    activeTab: String,
    onSongSelect: (Song) -> Unit,
    onFavoriteToggle: (Song) -> Unit,
    onSongMenuClick: (Song) -> Unit,
    onPlaybackContextChanged: (List<Song>?) -> Unit
) {
    val filteredSongs = remember(songs, searchQuery) {
        if (searchQuery.isBlank()) {
            songs
        } else {
            songs.filter { song ->
                song.title.contains(searchQuery, ignoreCase = true) ||
                song.artist.contains(searchQuery, ignoreCase = true) ||
                song.album.contains(searchQuery, ignoreCase = true) ||
                song.genre?.contains(searchQuery, ignoreCase = true) == true ||
                song.filePath.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    /*
     * selectedGroup representa el CONTEXTO interno de la biblioteca.
     *
     * La pestaña Álbumes, Artistas, Géneros, Carpetas, etc. muestra
     * primero una lista de grupos. Al entrar a uno de ellos, el
     * contexto cambia a sus canciones.
     *
     * No existe una pestaña "Biblioteca" adicional.
     */
    var selectedGroup by remember(activeTab) {
        androidx.compose.runtime.mutableStateOf<LibraryGroup?>(null)
    }

    val detailSongs = remember(
        filteredSongs,
        selectedGroup,
        activeTab
    ) {
        selectedGroup?.songs?.let { groupSongs ->
            val ids = groupSongs.map { it.id }.toSet()
            filteredSongs.filter { it.id in ids }
        }
    }

    /*
     * El HeaderBar necesita saber si el contexto actual permite
     * "Reproducir aleatorio".
     *
     * Lista de grupos = null => botón desactivado.
     * Lista de canciones = lista real => botón disponible.
     */
    androidx.compose.runtime.LaunchedEffect(
        activeTab,
        selectedGroup,
        filteredSongs,
        detailSongs
    ) {
        onPlaybackContextChanged(
            when {
                selectedGroup != null -> detailSongs ?: emptyList()
                activeTab == "SONGS" -> filteredSongs
                activeTab == "FAVORITES" -> filteredSongs
                activeTab == "MOST_PLAYED" -> filteredSongs
                activeTab == "RECENTLY_ADDED" -> filteredSongs
                activeTab == "HISTORY" -> filteredSongs
                activeTab == "DOWNLOADED" -> filteredSongs
                activeTab == "PODCASTS" -> filteredSongs
                activeTab == "AUDIOBOOKS" -> filteredSongs
                activeTab == "PLAYLISTS" -> null
                activeTab == "ALBUMS" -> null
                activeTab == "ARTISTS" -> null
                activeTab == "GENRES" -> null
                activeTab == "FOLDERS" -> null
                else -> filteredSongs
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
    ) {
        if (selectedGroup != null) {
            val group = selectedGroup!!
            val songsInGroup = detailSongs ?: emptyList()

            /*
             * DETALLE DEL GRUPO
             *
             * El botón aleatorio NO aparece aquí. Sigue estando
             * exclusivamente en HeaderBar. Aquí solamente mostramos
             * el contenido contextual.
             */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 14.dp,
                        vertical = 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        selectedGroup = null
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = group.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    Text(
                        text = group.subtitle,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }

            if (songsInGroup.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.MusicOff,
                    title = "No hay canciones",
                    subtitle = "No se encontraron canciones en este grupo."
                )
            } else {
                SongListView(
                    songs = songsInGroup,
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    onSongSelect = { song ->
                        /*
                         * Importante:
                         * MainActivity recibe la canción y el ViewModel
                         * utilizará el contexto actual para construir
                         * la cola completa.
                         */
                        onSongSelect(song)
                    },
                    onFavoriteToggle = onFavoriteToggle,
                    onSongMenuClick = onSongMenuClick,
                    showHeader = true
                )
            }
        } else {
            when (activeTab) {

                "PLAYLISTS" -> {
                    if (playlists.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.QueueMusic,
                            title = "No hay listas de reproducción",
                            subtitle = "Crea o añade canciones a una lista para verla aquí."
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 12.dp,
                                bottom = 120.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(playlists) { playlist ->
                                PlaylistCardItem(
                                    playlist = playlist,
                                    songs = songs,
                                    onClick = {
                                        val playlistSongs = playlist.songIds.mapNotNull { id ->
                                            songs.find { it.id == id }
                                        }

                                        if (playlistSongs.isNotEmpty()) {
                                            onPlaybackContextChanged(playlistSongs)
                                            onSongSelect(playlistSongs.first())
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                "ALBUMS" -> {
                    AlbumLibraryView(
                        songs = filteredSongs,
                        onGroupSelected = { selectedGroup = it }
                    )
                }

                "ARTISTS" -> {
                    ArtistLibraryView(
                        songs = filteredSongs,
                        onGroupSelected = { selectedGroup = it }
                    )
                }

                "GENRES" -> {
                    GenreLibraryView(
                        songs = filteredSongs,
                        onGroupSelected = { selectedGroup = it }
                    )
                }

                "FOLDERS" -> {
                    FolderLibraryView(
                        songs = filteredSongs,
                        onGroupSelected = { selectedGroup = it }
                    )
                }

                "AUDIOBOOKS" -> {
                    AudiobookLibraryView(
                        songs = filteredSongs,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        onSongSelect = onSongSelect,
                        onFavoriteToggle = onFavoriteToggle,
                        onSongMenuClick = onSongMenuClick
                    )
                }

                "FAVORITES" -> {
                    if (filteredSongs.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.FavoriteBorder,
                            title = "Sin Rolitas Favoritas",
                            subtitle = "Toca el corazón en cualquier canción para guardarla aquí."
                        )
                    } else {
                        SongListView(
                            songs = filteredSongs,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            onSongSelect = onSongSelect,
                            onFavoriteToggle = onFavoriteToggle,
                            onSongMenuClick = onSongMenuClick
                        )
                    }
                }

                else -> {
                    if (filteredSongs.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.MusicOff,
                            title = "No se encontraron canciones",
                            subtitle = "No hay canciones disponibles en esta categoría."
                        )
                    } else {
                        SongListView(
                            songs = filteredSongs,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            onSongSelect = onSongSelect,
                            onFavoriteToggle = onFavoriteToggle,
                            onSongMenuClick = onSongMenuClick,
                            showHeader = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SongListView(
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onSongSelect: (Song) -> Unit,
    onFavoriteToggle: (Song) -> Unit,
    onSongMenuClick: (Song) -> Unit,
    showHeader: Boolean = false
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 120.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (showHeader) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${songs.size} Rolitas disponibles",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )

                }
            }
        }

        items(
            items = songs,
            key = { it.id }
        ) { song ->
            SongRowItem(
                song = song,
                isCurrentSong = currentSong?.id == song.id,
                isPlaying = isPlaying,
                onClick = { onSongSelect(song) },
                onFavoriteToggle = { onFavoriteToggle(song) },
                onMenuClick = { onSongMenuClick(song) }
            )
        }
    }
}

@Composable
private fun AudiobookLibraryView(
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onSongSelect: (Song) -> Unit,
    onFavoriteToggle: (Song) -> Unit,
    onSongMenuClick: (Song) -> Unit
) {
    if (songs.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.MenuBook,
            title = "No hay audiolibros",
            subtitle = "No se encontraron archivos identificados como audiolibros."
        )
    } else {
        val chapters = remember(songs) {
            songs.sortedWith(
                compareBy<Song> {
                    it.album.trim().ifBlank { "Audiolibro desconocido" }
                        .lowercase()
                }.thenBy {
                    it.title.trim().lowercase()
                }
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 120.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${chapters.size} capítulos",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )

                        Text(
                            text = "Audiolibros",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            items(
                items = chapters,
                key = { it.id }
            ) { song ->
                SongRowItem(
                    song = song,
                    isCurrentSong = currentSong?.id == song.id,
                    isPlaying = isPlaying,
                    onClick = { onSongSelect(song) },
                    onFavoriteToggle = { onFavoriteToggle(song) },
                    onMenuClick = { onSongMenuClick(song) }
                )
            }
        }
    }
}

@Composable
private fun AlbumLibraryView(
    songs: List<Song>,
    onGroupSelected: (LibraryGroup) -> Unit
) {
    val albums = remember(songs) {
        songs
            .groupBy {
                "${it.album.trim()}|${it.artist.trim()}"
            }
            .map { (_, tracks) ->
                LibraryGroup(
                    title = tracks.first().album.ifBlank { "Álbum desconocido" },
                    subtitle = tracks.first().artist.ifBlank { "Artista desconocido" },
                    count = tracks.size,
                    song = tracks.first(),
                    songs = tracks
                )
            }
            .sortedWith(
                compareBy<LibraryGroup> { it.title.lowercase() }
                    .thenBy { it.subtitle.lowercase() }
            )
    }

    if (albums.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.Album,
            title = "No hay álbumes",
            subtitle = "No se encontraron álbumes en la biblioteca."
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 120.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                albums,
                key = { it.title + "|" + it.subtitle }
            ) { album ->
                GroupRowItem(
                    group = album,
                    icon = Icons.Default.Album,
                    onClick = { onGroupSelected(album) }
                )
            }
        }
    }
}

@Composable
private fun ArtistLibraryView(
    songs: List<Song>,
    onGroupSelected: (LibraryGroup) -> Unit
) {
    val artists = remember(songs) {
        songs
            .groupBy {
                it.artist.trim().ifBlank { "Artista desconocido" }
            }
            .map { (artist, tracks) ->
                LibraryGroup(
                    title = artist,
                    subtitle = "${tracks.size} rolitas",
                    count = tracks.size,
                    song = tracks.first(),
                    songs = tracks
                )
            }
            .sortedBy { it.title.lowercase() }
    }

    if (artists.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.Person,
            title = "No hay artistas",
            subtitle = "No se encontraron artistas en la biblioteca."
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 120.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(artists, key = { it.title }) { artist ->
                GroupRowItem(
                    group = artist,
                    icon = Icons.Default.Person,
                    onClick = { onGroupSelected(artist) }
                )
            }
        }
    }
}

@Composable
private fun GenreLibraryView(
    songs: List<Song>,
    onGroupSelected: (LibraryGroup) -> Unit
) {
    val genres = remember(songs) {
        songs
            .groupBy {
                it.genre?.trim().takeUnless { value ->
                    value.isNullOrBlank()
                } ?: "Sin género"
            }
            .map { (genre, tracks) ->
                LibraryGroup(
                    title = genre,
                    subtitle = "${tracks.size} rolitas",
                    count = tracks.size,
                    song = tracks.first(),
                    songs = tracks
                )
            }
            .sortedBy { it.title.lowercase() }
    }

    if (genres.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.Category,
            title = "No hay géneros",
            subtitle = "No se encontraron géneros en la biblioteca."
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 120.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(genres, key = { it.title }) { genre ->
                GroupRowItem(
                    group = genre,
                    icon = Icons.Default.Category,
                    onClick = { onGroupSelected(genre) }
                )
            }
        }
    }
}

@Composable
private fun FolderLibraryView(
    songs: List<Song>,
    onGroupSelected: (LibraryGroup) -> Unit
) {
    val folders = remember(songs) {
        songs
            .groupBy { folderName(it.filePath) }
            .map { (folder, tracks) ->
                LibraryGroup(
                    title = folder,
                    subtitle = "${tracks.size} rolitas",
                    count = tracks.size,
                    song = tracks.first(),
                    songs = tracks
                )
            }
            .sortedBy { it.title.lowercase() }
    }

    if (folders.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.Folder,
            title = "No hay carpetas",
            subtitle = "No se encontraron carpetas con archivos de audio."
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 120.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(folders, key = { it.title }) { folder ->
                GroupRowItem(
                    group = folder,
                    icon = Icons.Default.Folder,
                    onClick = { onGroupSelected(folder) }
                )
            }
        }
    }
}

private fun folderName(path: String): String {
    if (path.isBlank()) return "Almacenamiento"

    val normalized = path
        .replace('\\', '/')
        .trimEnd('/')

    val lastSlash = normalized.lastIndexOf('/')

    if (lastSlash <= 0) {
        return "Almacenamiento"
    }

    val parent = normalized.substring(0, lastSlash)
    val parentSlash = parent.lastIndexOf('/')

    return parent.substring(parentSlash + 1)
        .ifBlank { "Almacenamiento" }
}

private data class LibraryGroup(
    val title: String,
    val subtitle: String,
    val count: Int,
    val song: Song,
    val songs: List<Song>
)

@Composable
private fun GroupRowItem(
    group: LibraryGroup,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkCard),
            contentAlignment = Alignment.Center
        ) {
            if (group.song.artworkUri != null) {
                AsyncImage(
                    model = group.song.artworkUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = group.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )

            Text(
                text = group.subtitle,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Abrir",
            tint = TextSecondary,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun PlaylistCardItem(
    playlist: Playlist,
    songs: List<Song>,
    onClick: () -> Unit
) {
    val coverSong = playlist.songIds
        .asSequence()
        .mapNotNull { id -> songs.find { it.id == id } }
        .firstOrNull()

    Surface(
        color = DarkCard,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurface),
                contentAlignment = Alignment.Center
            ) {
                if (playlist.coverUri != null || coverSong?.artworkUri != null) {
                    AsyncImage(
                        model = playlist.coverUri ?: coverSong?.artworkUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = playlist.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )

            Text(
                text = "${playlist.songIds.size} rolitas",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    subtitle: String,
    buttonText: String? = null,
    onButtonClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(DarkCard),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OrangePrimary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        if (buttonText != null && onButtonClick != null) {
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onButtonClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = buttonText,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

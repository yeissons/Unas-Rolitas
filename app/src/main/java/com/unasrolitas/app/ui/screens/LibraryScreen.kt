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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    onScanMediaStore: () -> Unit
) {
    val filteredSongs = songs.filter { song ->
        searchQuery.isEmpty() ||
                song.title.contains(searchQuery, ignoreCase = true) ||
                song.artist.contains(searchQuery, ignoreCase = true) ||
                song.album.contains(searchQuery, ignoreCase = true) ||
                (song.genre?.contains(searchQuery, ignoreCase = true) == true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
    ) {
        when (activeTab) {
            "PLAYLISTS" -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(playlists) { playlist ->
                        PlaylistCardItem(playlist = playlist, onClick = {
                            val firstSongId = playlist.songIds.firstOrNull()
                            val match = songs.find { it.id == firstSongId } ?: songs.firstOrNull()
                            match?.let { onSongSelect(it) }
                        })
                    }
                }
            }

            "FAVORITES" -> {
                val favSongs = filteredSongs.filter { it.isFavorite }
                if (favSongs.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.FavoriteBorder,
                        title = "Sin Rolitas Favoritas",
                        subtitle = "Toca el corazón en cualquier canción para guardarla aquí."
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 120.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(favSongs) { song ->
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

            else -> { // SONGS, ALBUMS, ARTISTS, FOLDERS
                if (filteredSongs.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.MusicOff,
                        title = "No se encontraron canciones",
                        subtitle = "Escanea tu almacenamiento interno para detectar archivos de audio.",
                        buttonText = "Escanear Almacenamiento",
                        onButtonClick = onScanMediaStore
                    )
                } else {
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
                                Text(
                                    text = "${filteredSongs.size} Rolitas disponibles",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                                Button(
                                    onClick = onScanMediaStore,
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Actualizar",
                                        tint = OrangePrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Re-escanear", fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }

                        items(filteredSongs) { song ->
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
        }
    }
}

@Composable
fun PlaylistCardItem(playlist: Playlist, onClick: () -> Unit) {
    Surface(
        color = DarkCard,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QueueMusic,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(48.dp)
                )
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(text = buttonText, fontWeight = FontWeight.Bold)
            }
        }
    }
}

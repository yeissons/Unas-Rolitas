package com.unasrolitas.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.unasrolitas.app.data.model.Song
import com.unasrolitas.app.ui.theme.*

@Composable
fun SongRowItem(
    song: Song,
    isCurrentSong: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onMenuClick: () -> Unit
) {
    Surface(
        color = if (isCurrentSong) DarkCard else DarkSurface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Song Artwork
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkCanvas),
                contentAlignment = Alignment.Center
            ) {
                if (song.artworkUri != null) {
                    AsyncImage(
                        model = song.artworkUri,
                        contentDescription = song.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                if (isCurrentSong) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Equalizer else Icons.Default.PlayArrow,
                            contentDescription = "Sonando",
                            tint = OrangePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title, Artist, and Tech Badge
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrentSong) OrangePrimary else TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${song.artist} • ${song.album}",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(OrangePrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = song.bitrateKbps?.let { "$it kbps" } ?: "Audio",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangePrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatDuration(song.durationMs),
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            // Favorite Button
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorito",
                    tint = if (song.isFavorite) HeartRed else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            // 3-dots Menu Button
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Opciones",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

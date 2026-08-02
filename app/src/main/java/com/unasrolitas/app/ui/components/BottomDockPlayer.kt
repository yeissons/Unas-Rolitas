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
fun BottomDockPlayer(
    song: Song?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onOpenPlayer: () -> Unit,
    onTogglePlay: () -> Unit,
    onSkipNext: () -> Unit
) {
    if (song == null) return

    val progress = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    Surface(
        color = DarkCard,
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
        shadowElevation = 12.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenPlayer() }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Seek Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = OrangePrimary,
                trackColor = DividerColor
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vinyl Artwork Preview
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
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
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Song details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = song.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Play / Pause Button
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(OrangePrimary)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Skip Next Button
                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Siguiente",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

package com.unasrolitas.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.unasrolitas.app.data.model.Song
import com.unasrolitas.app.ui.components.formatDuration
import com.unasrolitas.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    song: Song?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isShuffle: Boolean,
    repeatMode: Int,
    sleepTimerMinutes: Int,
    onBack: () -> Unit,
    onTogglePlay: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrev: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onFavoriteToggle: (Song) -> Unit,
    onOpenQueue: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onSetSleepTimer: (Int) -> Unit
) {
    if (song == null) return

    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableStateOf(1.0f) }

    // Vinyl Rotation Animation
    val infiniteTransition = rememberInfiniteTransition(label = "VinylRotate")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkCanvas,
                        Color(0xFF1E1510),
                        DarkCanvas
                    )
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkCard)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Cerrar",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "REPRODUCIENDO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = song.album,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = { onFavoriteToggle(song) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkCard)
            ) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorito",
                    tint = if (song.isFavorite) HeartRed else Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Center Vinyl Artwork Disc
        Box(
            modifier = Modifier
                .padding(vertical = 20.dp)
                .size(280.dp),
            contentAlignment = Alignment.Center
        ) {
            // Vinyl Record Ring
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(if (isPlaying) rotationAngle else 0f)
                    .clip(CircleShape)
                    .background(Color(0xFF111115))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Vinyl Groove Lines
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF222228),
                                    Color(0xFF151518),
                                    Color(0xFF2B2B32),
                                    Color(0xFF111114)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Album Center Image
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(DarkSurface),
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
                                modifier = Modifier.size(54.dp)
                            )
                        }

                        // Center Vinyl Spindle Hole
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(DarkCanvas)
                        )
                    }
                }
            }
        }

        // Song Info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = song.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = song.artist,
                fontSize = 16.sp,
                color = OrangePrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Seekbar
            val sliderValue = if (durationMs > 0) currentPositionMs.toFloat() else 0f
            val maxSlider = if (durationMs > 0) durationMs.toFloat() else 1f

            Slider(
                value = sliderValue.coerceIn(0f, maxSlider),
                onValueChange = { onSeekTo(it.toLong()) },
                valueRange = 0f..maxSlider,
                colors = SliderDefaults.colors(
                    thumbColor = OrangePrimary,
                    activeTrackColor = OrangePrimary,
                    inactiveTrackColor = DividerColor
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDuration(currentPositionMs),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = formatDuration(durationMs),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // Main Transport Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle
            IconButton(onClick = onToggleShuffle) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Aleatorio",
                    tint = if (isShuffle) OrangePrimary else TextMuted
                )
            }

            // Skip Prev
            IconButton(onClick = onSkipPrev) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Anterior",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Play / Pause Circular Button
            IconButton(
                onClick = onTogglePlay,
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(OrangePrimary)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Skip Next
            IconButton(onClick = onSkipNext) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Siguiente",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Repeat Mode
            IconButton(onClick = onToggleRepeat) {
                val tint = if (repeatMode != Player.REPEAT_MODE_OFF) OrangePrimary else TextMuted
                val icon = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat
                Icon(
                    imageVector = icon,
                    contentDescription = "Repetir",
                    tint = tint
                )
            }
        }

        // Quick Tools Row (Queue, Lyrics, Equalizer, Sleep Timer, Speed)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(DarkCard)
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onOpenQueue) {
                Icon(Icons.Default.QueueMusic, contentDescription = "Cola", tint = Color.White)
            }

            IconButton(onClick = onOpenLyrics) {
                Icon(Icons.Default.Subtitles, contentDescription = "Letras", tint = OrangePrimary)
            }

            IconButton(onClick = onOpenEqualizer) {
                Icon(Icons.Default.GraphicEq, contentDescription = "Ecualizador", tint = AmberAccent)
            }

            IconButton(onClick = { showSleepTimerDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Temporizador",
                    tint = if (sleepTimerMinutes > 0) OrangePrimary else Color.White
                )
            }

            // Speed Selector Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .clickable {
                        playbackSpeed = when (playbackSpeed) {
                            1.0f -> 1.25f
                            1.25f -> 1.5f
                            1.5f -> 2.0f
                            2.0f -> 0.75f
                            else -> 1.0f
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${playbackSpeed}x",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimary
                )
            }
        }
    }

    // Sleep Timer Dialog Modal
    if (showSleepTimerDialog) {
        AlertDialog(
            onDismissRequest = { showSleepTimerDialog = false },
            containerColor = DarkCard,
            title = { Text("Temporizador de Apagado", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Apagar la música automáticamente en:", color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    listOf(0, 15, 30, 45, 60).forEach { mins ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSetSleepTimer(mins)
                                    showSleepTimerDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = sleepTimerMinutes == mins,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = OrangePrimary)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (mins == 0) "Desactivado" else "$mins Minutos",
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSleepTimerDialog = false }) {
                    Text("Cerrar", color = OrangePrimary)
                }
            }
        )
    }
}

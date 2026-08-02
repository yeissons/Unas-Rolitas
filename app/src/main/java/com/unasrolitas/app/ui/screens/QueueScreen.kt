package com.unasrolitas.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unasrolitas.app.data.model.Song
import com.unasrolitas.app.ui.components.formatDuration
import com.unasrolitas.app.ui.theme.*

@Composable
fun QueueScreen(
    queue: List<Song>,
    currentIndex: Int,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onSelectQueueIndex: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
    ) {
        // Queue Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(DarkCard)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Atrás",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Cola de Reproducción",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${queue.size} rolitas en cola",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(OrangePrimary.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Guardar Lista",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimary
                )
            }
        }

        HorizontalDivider(color = DividerColor)

        // Queue Items List
        LazyColumn(
            contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(queue) { index, song ->
                val isCurrent = index == currentIndex
                Surface(
                    color = if (isCurrent) DarkCard else DarkSurface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .clickable { onSelectQueueIndex(index) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Track index / playing indicator
                        Box(
                            modifier = Modifier.width(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCurrent) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Equalizer else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = OrangePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) OrangePrimary else Color.White
                            )
                            Text(
                                text = "${song.artist} • ${song.album}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Text(
                            text = formatDuration(song.durationMs),
                            fontSize = 12.sp,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = "Reordenar",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

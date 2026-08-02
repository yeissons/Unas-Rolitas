package com.unasrolitas.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unasrolitas.app.data.model.LyricLine
import com.unasrolitas.app.data.model.Lyrics
import com.unasrolitas.app.data.model.Song
import com.unasrolitas.app.ui.theme.*

@Composable
fun LyricsScreen(
    song: Song?,
    lyrics: Lyrics?,
    currentPositionMs: Long,
    onBack: () -> Unit,
    onSeekToTimestamp: (Long) -> Unit
) {
    var offsetMs by remember { mutableStateOf(0L) }
    var fontSizeSp by remember { mutableStateOf(18) }
    val listState = rememberLazyListState()

    val adjustedPosition = currentPositionMs + offsetMs

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
    ) {
        // Top Bar
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
                        text = "Letras Sincronizadas",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = song?.title ?: "Desconocido",
                        fontSize = 12.sp,
                        color = OrangePrimary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Font Size Toggle
                IconButton(
                    onClick = { fontSizeSp = if (fontSizeSp >= 22) 16 else fontSizeSp + 3 },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkCard)
                ) {
                    Text(text = "A", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // Timing adjustment bar (-0.5s / +0.5s)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Ajuste de Sincronización LRC:",
                fontSize = 12.sp,
                color = TextSecondary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { offsetMs -= 500L },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "-0.5s", tint = OrangePrimary)
                }
                Text(
                    text = "${offsetMs / 1000.0}s",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
                IconButton(
                    onClick = { offsetMs += 500L },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "+0.5s", tint = OrangePrimary)
                }
            }
        }

        // Lyrics List
        if (lyrics == null || lyrics.lines.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay letras LRC sincronizadas para esta canción.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            val currentLineIndex = lyrics.lines.indexOfLast { it.timestampMs <= adjustedPosition }

            LaunchedEffect(currentLineIndex) {
                if (currentLineIndex >= 0) {
                    listState.animateScrollToItem(currentLineIndex)
                }
            }

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(vertical = 32.dp, horizontal = 20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(lyrics.lines) { line ->
                    val isActive = lyrics.lines.indexOf(line) == currentLineIndex

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isActive) DarkCard else Color.Transparent)
                            .clickable { onSeekToTimestamp(line.timestampMs) }
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = line.text,
                            fontSize = if (isActive) (fontSizeSp + 2).sp else fontSizeSp.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) OrangePrimary else TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        if (line.translation != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = line.translation,
                                fontSize = (fontSizeSp - 4).sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

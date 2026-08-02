package com.unasrolitas.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unasrolitas.app.data.model.Song
import com.unasrolitas.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextMenuBottomSheet(
    song: Song?,
    onDismiss: () -> Unit,
    onPlayNow: (Song) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onShowInfo: (Song) -> Unit
) {
    if (song == null) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(TextMuted, RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
        ) {
            Text(
                text = song.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "${song.artist} • ${song.album}",
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = DividerColor)
            Spacer(modifier = Modifier.height(12.dp))

            ContextOptionRow(
                icon = Icons.Default.PlayArrow,
                title = "Reproducir ahora",
                color = OrangePrimary,
                onClick = {
                    onPlayNow(song)
                    onDismiss()
                }
            )

            ContextOptionRow(
                icon = Icons.Default.QueuePlayNext,
                title = "Reproducir a continuación",
                color = Color.White,
                onClick = {
                    onPlayNext(song)
                    onDismiss()
                }
            )

            ContextOptionRow(
                icon = Icons.Default.PlaylistAdd,
                title = "Añadir a la cola",
                color = Color.White,
                onClick = {
                    onAddToQueue(song)
                    onDismiss()
                }
            )

            ContextOptionRow(
                icon = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                title = if (song.isFavorite) "Quitar de Favoritos" else "Marcar como Favorito",
                color = if (song.isFavorite) HeartRed else Color.White,
                onClick = {
                    onToggleFavorite(song)
                    onDismiss()
                }
            )

            ContextOptionRow(
                icon = Icons.Default.Info,
                title = "Información técnica de audio",
                color = AmberAccent,
                onClick = {
                    onShowInfo(song)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
fun ContextOptionRow(
    icon: ImageVector,
    title: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

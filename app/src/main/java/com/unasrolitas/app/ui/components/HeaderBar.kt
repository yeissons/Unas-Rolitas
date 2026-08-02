package com.unasrolitas.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.unasrolitas.app.ui.theme.*

@Composable
fun HeaderBar(
    currentRoute: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    activeTab: String,
    onTabSelected: (String) -> Unit,
    onOpenControlCenter: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenAudioTools: () -> Unit,
    onShuffleAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCanvas)
            .padding(top = 12.dp, bottom = 8.dp)
    ) {
        // Top row with app logo title and control center button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(OrangePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "¿Unas Rolitas?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Reproductor Nativo Android",
                        fontSize = 11.sp,
                        color = OrangePrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = onOpenEqualizer,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkCard)
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Ecualizador",
                        tint = OrangePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onOpenAudioTools,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkCard)
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Herramientas",
                        tint = AmberAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onOpenControlCenter,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkCard)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Ajustes",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Buscar por canción, artista, género...", color = TextSecondary, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = TextSecondary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = TextSecondary)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkCard,
                unfocusedContainerColor = DarkSurface,
                focusedBorderColor = OrangePrimary,
                unfocusedBorderColor = DividerColor,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category filter scrollable pills
        if (currentRoute == "library") {
            val tabs = listOf(
                "SONGS" to "Canciones",
                "PLAYLISTS" to "Listas",
                "ALBUMS" to "Álbumes",
                "ARTISTS" to "Artistas",
                "FAVORITES" to "Favoritas",
                "FOLDERS" to "Carpetas"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEach { (key, label) ->
                    val isSelected = activeTab == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) OrangePrimary else DarkCard)
                            .clickable { onTabSelected(key) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextSecondary
                        )
                    }
                }

                // Quick Shuffle All button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(OrangeSecondary.copy(alpha = 0.2f))
                        .clickable { onShuffleAll() }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Aleatorio",
                            tint = OrangeSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Aleatorio",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangeSecondary
                        )
                    }
                }
            }
        }
    }
}

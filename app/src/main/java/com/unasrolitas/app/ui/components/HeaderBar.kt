package com.unasrolitas.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
    shuffleEnabled: Boolean,
    onShuffleAll: () -> Unit,
    onSortClick: () -> Unit
) {
    var isSearchOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCanvas)
    ) {

        /*
         * CABECERA PRINCIPAL
         *
         * [☰]          ¿Unas Rolitas?          [🔍]
         */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onOpenControlCenter,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menú",
                    tint = TextPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "¿Unas Rolitas?",
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = {
                    isSearchOpen = !isSearchOpen

                    if (!isSearchOpen) {
                        onSearchQueryChange("")
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = if (isSearchOpen) {
                        Icons.Default.Close
                    } else {
                        Icons.Default.Search
                    },
                    contentDescription = if (isSearchOpen) {
                        "Cerrar búsqueda"
                    } else {
                        "Buscar"
                    },
                    tint = TextPrimary,
                    modifier = Modifier.size(25.dp)
                )
            }
        }

        /*
         * BÚSQUEDA
         *
         * Solo aparece cuando el usuario toca la lupa.
         * searchQuery permanece vacío mientras la búsqueda
         * no esté activa.
         */
        if (isSearchOpen) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 14.dp,
                        vertical = 4.dp
                    )
                    .height(52.dp),
                placeholder = {
                    Text(
                        text = "Buscar por canción, artista, álbum o género...",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                onSearchQueryChange("")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar búsqueda",
                                tint = TextSecondary
                            )
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
                    unfocusedTextColor = Color.White,
                    cursorColor = OrangePrimary
                )
            )
        }

        /*
         * CATEGORÍAS DE LA BIBLIOTECA
         *
         * Siempre visibles:
         * Canciones → Álbumes → Artistas → Favoritos
         *
         * La quinta posición NO es una pestaña.
         * Es únicamente una flecha que despliega las demás categorías
         * horizontalmente hacia la derecha, en la misma fila.
         */
        val primaryTabs = listOf(
            "SONGS" to "Canciones",
            "ALBUMS" to "Álbumes",
            "ARTISTS" to "Artistas",
            "FAVORITES" to "Favoritos"
        )

        val secondaryTabs = listOf(
            "PLAYLISTS" to "Listas de reproducción",
            "GENRES" to "Géneros",
            "FOLDERS" to "Carpetas",
            "MOST_PLAYED" to "Más reproducidas",
            "RECENTLY_ADDED" to "Recientemente añadidas",
            "HISTORY" to "Historial",
            "DOWNLOADED" to "Descargadas",
            "PODCASTS" to "Podcasts",
            "AUDIOBOOKS" to "Audiolibros"
        )

        var showMoreTabs by rememberSaveable { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            primaryTabs.forEach { (key, label) ->
                val selected = activeTab == key

                Surface(
                    onClick = {
                        onTabSelected(key)
                        showMoreTabs = false
                    },
                    color = if (selected) OrangePrimary else DarkCard,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(
                            horizontal = 14.dp,
                            vertical = 8.dp
                        ),
                        color = if (selected) Color.White else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (selected) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Medium
                        },
                        maxLines = 1
                    )
                }
            }

            /*
             * Botón de expansión.
             *
             * NO es una pestaña:
             * - no contiene texto
             * - no usa una pastilla de selección
             * - solamente muestra la flecha
             */
            IconButton(
                onClick = { showMoreTabs = !showMoreTabs },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (showMoreTabs) {
                        Icons.Default.ChevronLeft
                    } else {
                        Icons.Default.ChevronRight
                    },
                    contentDescription = if (showMoreTabs) {
                        "Ocultar categorías"
                    } else {
                        "Mostrar más categorías"
                    },
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            /*
             * Las categorías secundarias aparecen AQUÍ MISMO,
             * después de la flecha y en la misma fila.
             */
            if (showMoreTabs) {
                secondaryTabs.forEach { (key, label) ->
                    val selected = activeTab == key

                    Surface(
                        onClick = {
                            onTabSelected(key)
                            // Se mantiene expandido al seleccionar
                            // una categoría secundaria.
                            showMoreTabs = true
                        },
                        color = if (selected) OrangePrimary else DarkCard,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 8.dp
                            ),
                            color = if (selected) Color.White else TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = if (selected) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Medium
                            },
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        /*
         * ACCIONES RÁPIDAS
         */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onShuffleAll,
                enabled = shuffleEnabled
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Reproducir aleatorio",
                    tint = OrangePrimary,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Reproducir aleatorio",
                    color = OrangePrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.weight(1f))


            TextButton(
                onClick = onSortClick
            ) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = "Ordenar",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Ordenar",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

        }

        HorizontalDivider(
            color = DividerColor.copy(alpha = 0.5f),
            thickness = 1.dp
        )
    }
}

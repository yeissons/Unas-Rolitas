package com.unasrolitas.app.ui.components

import com.unasrolitas.app.viewmodel.MusicViewModel
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.PaddingValues
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
    sortMode: MusicViewModel.SortMode,
    sortDescending: Boolean,
    onSortSelected: (MusicViewModel.SortMode, Boolean) -> Unit
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
            if (!showMoreTabs) {
                IconButton(
                    onClick = { showMoreTabs = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Mostrar más categorías",
                        tint = TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
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
           *
           * Reproducir aleatorio se habilita únicamente cuando
           * la vista actual representa canciones concretas.
           *
           * Ordenar tiene aquí su propio menú y utiliza directamente
           * el estado real del ViewModel.
           */

          Row(
              modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp),
              verticalAlignment = Alignment.CenterVertically
          ) {

              val shuffleColor =
                  if (shuffleEnabled) {
                      OrangePrimary
                  } else {
                      TextSecondary.copy(alpha = 0.38f)
                  }

              TextButton(
                  onClick = onShuffleAll,
                  enabled = shuffleEnabled,
                  colors = ButtonDefaults.textButtonColors(
                      contentColor = OrangePrimary,
                      disabledContentColor = TextSecondary.copy(alpha = 0.38f),
                      containerColor = DarkCard,
                      disabledContainerColor = DarkCard
                  ),
                  shape = RoundedCornerShape(18.dp),
                  contentPadding = PaddingValues(
                      horizontal = 12.dp,
                      vertical = 6.dp
                  )
              ) {
                  Icon(
                      imageVector = Icons.Default.Shuffle,
                      contentDescription = if (shuffleEnabled) {
                          "Reproducir aleatorio"
                      } else {
                          "Reproducir aleatorio no disponible en esta vista"
                      },
                      tint = shuffleColor,
                      modifier = Modifier.size(18.dp)
                  )

                  Spacer(modifier = Modifier.width(6.dp))

                  Text(
                      text = "Aleatorio",
                      color = shuffleColor,
                      fontSize = 13.sp,
                      fontWeight = FontWeight.SemiBold
                  )
              }

              Spacer(modifier = Modifier.weight(1f))

              var showSortMenu by rememberSaveable {
                  mutableStateOf(false)
              }

              Box {
                  TextButton(
                      onClick = {
                          showSortMenu = true
                      },
                      colors = ButtonDefaults.textButtonColors(
                          contentColor = TextSecondary,
                          containerColor = DarkCard
                      ),
                      shape = RoundedCornerShape(18.dp),
                      contentPadding = PaddingValues(
                          horizontal = 12.dp,
                          vertical = 6.dp
                      )
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

                  DropdownMenu(
                      expanded = showSortMenu,
                      onDismissRequest = {
                          showSortMenu = false
                      }
                  ) {
                      Text(
                          text = "Ordenar por",
                          modifier = Modifier.padding(
                              horizontal = 16.dp,
                              vertical = 8.dp
                          ),
                          color = TextPrimary,
                          fontWeight = FontWeight.Bold
                      )

                      DropdownMenuItem(
                          text = {
                              Text(
                                  when {
                                      sortMode == MusicViewModel.SortMode.TITLE &&
                                          sortDescending ->
                                          "✓ Título (Z → A)"

                                      sortMode == MusicViewModel.SortMode.TITLE ->
                                          "✓ Título (A → Z)"

                                      else ->
                                          "Título (A → Z)"
                                  }
                              )
                          },
                          onClick = {
                              onSortSelected(
                                  MusicViewModel.SortMode.TITLE,
                                  if (sortMode == MusicViewModel.SortMode.TITLE) {
                                      !sortDescending
                                  } else {
                                      false
                                  }
                              )
                              showSortMenu = false
                          }
                      )

                      DropdownMenuItem(
                          text = {
                              Text(
                                  when {
                                      sortMode == MusicViewModel.SortMode.ARTIST &&
                                          sortDescending ->
                                          "✓ Artista (Z → A)"

                                      sortMode == MusicViewModel.SortMode.ARTIST ->
                                          "✓ Artista (A → Z)"

                                      else ->
                                          "Artista (A → Z)"
                                  }
                              )
                          },
                          onClick = {
                              onSortSelected(
                                  MusicViewModel.SortMode.ARTIST,
                                  if (sortMode == MusicViewModel.SortMode.ARTIST) {
                                      !sortDescending
                                  } else {
                                      false
                                  }
                              )
                              showSortMenu = false
                          }
                      )

                      DropdownMenuItem(
                          text = {
                              Text(
                                  when {
                                      sortMode == MusicViewModel.SortMode.ALBUM &&
                                          sortDescending ->
                                          "✓ Álbum (Z → A)"

                                      sortMode == MusicViewModel.SortMode.ALBUM ->
                                          "✓ Álbum (A → Z)"

                                      else ->
                                          "Álbum (A → Z)"
                                  }
                              )
                          },
                          onClick = {
                              onSortSelected(
                                  MusicViewModel.SortMode.ALBUM,
                                  if (sortMode == MusicViewModel.SortMode.ALBUM) {
                                      !sortDescending
                                  } else {
                                      false
                                  }
                              )
                              showSortMenu = false
                          }
                      )

                      DropdownMenuItem(
                          text = {
                              Text(
                                  when {
                                      sortMode == MusicViewModel.SortMode.GENRE &&
                                          sortDescending ->
                                          "✓ Género (Z → A)"

                                      sortMode == MusicViewModel.SortMode.GENRE ->
                                          "✓ Género (A → Z)"

                                      else ->
                                          "Género (A → Z)"
                                  }
                              )
                          },
                          onClick = {
                              onSortSelected(
                                  MusicViewModel.SortMode.GENRE,
                                  if (sortMode == MusicViewModel.SortMode.GENRE) {
                                      !sortDescending
                                  } else {
                                      false
                                  }
                              )
                              showSortMenu = false
                          }
                      )

                      DropdownMenuItem(
                          text = {
                              Text(
                                  when {
                                      sortMode == MusicViewModel.SortMode.DATE &&
                                          sortDescending ->
                                          "✓ Fecha (más nuevo → más viejo)"

                                      sortMode == MusicViewModel.SortMode.DATE ->
                                          "✓ Fecha (más viejo → más nuevo)"

                                      else ->
                                          "Fecha (más viejo → más nuevo)"
                                  }
                              )
                          },
                          onClick = {
                              onSortSelected(
                                  MusicViewModel.SortMode.DATE,
                                  if (sortMode == MusicViewModel.SortMode.DATE) {
                                      !sortDescending
                                  } else {
                                      true
                                  }
                              )
                              showSortMenu = false
                          }
                      )

                      DropdownMenuItem(
                          text = {
                              Text(
                                  when {
                                      sortMode == MusicViewModel.SortMode.DURATION &&
                                          sortDescending ->
                                          "✓ Duración (más larga → más corta)"

                                      sortMode == MusicViewModel.SortMode.DURATION ->
                                          "✓ Duración (más corta → más larga)"

                                      else ->
                                          "Duración (más corta → más larga)"
                                  }
                              )
                          },
                          onClick = {
                              onSortSelected(
                                  MusicViewModel.SortMode.DURATION,
                                  if (sortMode == MusicViewModel.SortMode.DURATION) {
                                      !sortDescending
                                  } else {
                                      false
                                  }
                              )
                              showSortMenu = false
                          }
                      )

                      DropdownMenuItem(
                          text = {
                              Text(
                                  when {
                                      sortMode == MusicViewModel.SortMode.FILE_SIZE &&
                                          sortDescending ->
                                          "✓ Tamaño (más grande → más pequeño)"

                                      sortMode == MusicViewModel.SortMode.FILE_SIZE ->
                                          "✓ Tamaño (más pequeño → más grande)"

                                      else ->
                                          "Tamaño (más pequeño → más grande)"
                                  }
                              )
                          },
                          onClick = {
                              onSortSelected(
                                  MusicViewModel.SortMode.FILE_SIZE,
                                  if (sortMode == MusicViewModel.SortMode.FILE_SIZE) {
                                      !sortDescending
                                  } else {
                                      false
                                  }
                              )
                              showSortMenu = false
                          }
                      )
                  }
              }
          }

          Spacer(modifier = Modifier.height(10.dp))

          HorizontalDivider(
            color = DividerColor.copy(alpha = 0.5f),
            thickness = 1.dp
        )
    }
}

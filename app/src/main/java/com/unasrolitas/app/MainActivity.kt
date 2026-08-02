package com.unasrolitas.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.unasrolitas.app.data.model.Song
import com.unasrolitas.app.navigation.NavRoutes
import com.unasrolitas.app.permissions.PermissionHandler
import com.unasrolitas.app.ui.components.BottomDockPlayer
import com.unasrolitas.app.ui.components.ContextMenuBottomSheet
import com.unasrolitas.app.ui.components.HeaderBar
import com.unasrolitas.app.ui.screens.*
import com.unasrolitas.app.ui.theme.DarkCanvas
import com.unasrolitas.app.ui.theme.DarkCard
import com.unasrolitas.app.ui.theme.OrangePrimary
import com.unasrolitas.app.ui.theme.TextSecondary
import com.unasrolitas.app.ui.theme.UnasRolitasTheme
import com.unasrolitas.app.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {

    private var musicViewModel: MusicViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate()
        val incomingUri = intent?.data

        setContent {
            UnasRolitasTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkCanvas
                ) {
                    val vm: MusicViewModel = viewModel()
                    musicViewModel = vm

                    LaunchedEffect(incomingUri) {
                        if (incomingUri != null) {
                            vm.playExternalUri(incomingUri)
                        }
                    }

                    PermissionHandler(
                        onPermissionGranted = {
                            vm.setPermissionGranted(true)
                        }
                    ) {
                        UnasRolitasMainContent(viewModel = vm)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val incomingUri = intent.data
        if (incomingUri != null) {
            musicViewModel?.playExternalUri(incomingUri)
        }
    }
}

@Composable
fun UnasRolitasMainContent(viewModel: MusicViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavRoutes.Library.route

    val songs by viewModel.allSongs.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val queue by viewModel.queue.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val isShuffle by viewModel.isShuffle.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val audioSettings by viewModel.audioSettings.collectAsState()
    val activeLyrics by viewModel.activeLyrics.collectAsState()
    val sleepTimerMinutes by viewModel.sleepTimerMinutes.collectAsState()
    val selectedSongForMenu by viewModel.selectedSongForMenu.collectAsState()

    var songForInfoDialog by remember { mutableStateOf<Song?>(null) }

    Scaffold(
        topBar = {
            if (currentRoute == NavRoutes.Library.route) {
                HeaderBar(
                    currentRoute = currentRoute,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    activeTab = activeTab,
                    onTabSelected = { viewModel.setActiveTab(it) },
                    onOpenControlCenter = { navController.navigate(NavRoutes.ControlCenter.route) },
                    onOpenEqualizer = { navController.navigate(NavRoutes.Equalizer.route) },
                    onOpenAudioTools = { navController.navigate(NavRoutes.AudioTools.route) },
                    onShuffleAll = {
                        viewModel.toggleShuffle()
                        if (songs.isNotEmpty()) {
                            viewModel.playSong(songs.random())
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (currentRoute != NavRoutes.Player.route && currentSong != null) {
                BottomDockPlayer(
                    song = currentSong,
                    isPlaying = isPlaying,
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    onOpenPlayer = { navController.navigate(NavRoutes.Player.route) },
                    onTogglePlay = { viewModel.togglePlay() },
                    onSkipNext = { viewModel.playNext() }
                )
            }
        },
        containerColor = DarkCanvas
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = NavRoutes.Library.route
            ) {
                composable(NavRoutes.Library.route) {
                    LibraryScreen(
                        songs = songs,
                        playlists = playlists,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        searchQuery = searchQuery,
                        activeTab = activeTab,
                        onSongSelect = { song ->
                            viewModel.playSong(song)
                        },
                        onFavoriteToggle = { song ->
                            viewModel.toggleFavorite(song)
                        },
                        onSongMenuClick = { song ->
                            viewModel.setSelectedSongForMenu(song)
                        },
                        onScanMediaStore = {
                            viewModel.loadLibrary()
                        }
                    )
                }

                composable(NavRoutes.Player.route) {
                    PlayerScreen(
                        song = currentSong,
                        isPlaying = isPlaying,
                        currentPositionMs = currentPositionMs,
                        durationMs = durationMs,
                        isShuffle = isShuffle,
                        repeatMode = repeatMode,
                        sleepTimerMinutes = sleepTimerMinutes,
                        onBack = { navController.popBackStack() },
                        onTogglePlay = { viewModel.togglePlay() },
                        onSkipNext = { viewModel.playNext() },
                        onSkipPrev = { viewModel.playPrevious() },
                        onSeekTo = { pos -> viewModel.seekTo(pos) },
                        onToggleShuffle = { viewModel.toggleShuffle() },
                        onToggleRepeat = { viewModel.toggleRepeatMode() },
                        onFavoriteToggle = { song -> viewModel.toggleFavorite(song) },
                        onOpenQueue = { navController.navigate(NavRoutes.Queue.route) },
                        onOpenLyrics = { navController.navigate(NavRoutes.Lyrics.route) },
                        onOpenEqualizer = { navController.navigate(NavRoutes.Equalizer.route) },
                        onSetSleepTimer = { mins -> viewModel.setSleepTimer(mins) }
                    )
                }

                composable(NavRoutes.Queue.route) {
                    QueueScreen(
                        queue = queue,
                        currentIndex = currentIndex,
                        isPlaying = isPlaying,
                        onBack = { navController.popBackStack() },
                        onSelectQueueIndex = { idx -> viewModel.playQueueIndex(idx) }
                    )
                }

                composable(NavRoutes.Lyrics.route) {
                    LyricsScreen(
                        song = currentSong,
                        lyrics = activeLyrics,
                        currentPositionMs = currentPositionMs,
                        onBack = { navController.popBackStack() },
                        onSeekToTimestamp = { ts -> viewModel.seekTo(ts) }
                    )
                }

                composable(NavRoutes.Equalizer.route) {
                    EqualizerScreen(
                        audioSettings = audioSettings,
                        presets = viewModel.equalizerPresets,
                        onBack = { navController.popBackStack() },
                        onToggleEqualizer = { enabled -> viewModel.toggleEqualizer(enabled) },
                        onSelectPreset = { preset -> viewModel.setEqualizerPreset(preset) },
                        onBandGainChange = { band, gain -> viewModel.setBandGain(band, gain) },
                        onBassBoostChange = { strength -> viewModel.setBassBoost(strength) },
                        onVirtualizerChange = { strength -> viewModel.setVirtualizer(strength) }
                    )
                }

                composable(NavRoutes.AudioTools.route) {
                    AudioToolsScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(NavRoutes.ControlCenter.route) {
                    ControlCenterScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            // Context Menu Bottom Sheet
            if (selectedSongForMenu != null) {
                ContextMenuBottomSheet(
                    song = selectedSongForMenu,
                    onDismiss = { viewModel.setSelectedSongForMenu(null) },
                    onPlayNow = { song -> viewModel.playSong(song) },
                    onPlayNext = { song -> viewModel.playNextSong(song) },
                    onAddToQueue = { song -> viewModel.addSongToQueue(song) },
                    onToggleFavorite = { song -> viewModel.toggleFavorite(song) },
                    onShowInfo = { song -> songForInfoDialog = song }
                )
            }

            // Song Technical Info Dialog
            if (songForInfoDialog != null) {
                val song = songForInfoDialog!!
                AlertDialog(
                    onDismissRequest = { songForInfoDialog = null },
                    containerColor = DarkCard,
                    title = {
                        Text(
                            text = "Detalles Técnicos del Audio",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            InfoLine("Título", song.title)
                            InfoLine("Artista", song.artist)
                            InfoLine("Álbum", song.album)
                            InfoLine("Año", song.year?.toString() ?: "Desconocido")
                            InfoLine("Duración", formatTime(song.durationMs))
                            InfoLine("MIME Type", song.mimeType ?: "audio/*")
                            InfoLine("Tamaño", "${song.sizeBytes / (1024 * 1024)} MB (${song.sizeBytes} bytes)")
                            InfoLine("Ubicación", song.filePath.ifEmpty { song.uri.toString() })
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { songForInfoDialog = null }) {
                            Text("Cerrar", color = OrangePrimary, fontWeight = FontWeight.Bold)
                        }
                    },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
        Text(text = value, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

private fun formatTime(ms: Long): String {
    val totalSecs = ms / 1000
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    return String.format("%02d:%02d", mins, secs)
}

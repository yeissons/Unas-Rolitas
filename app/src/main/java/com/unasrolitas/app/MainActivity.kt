package com.unasrolitas.app

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
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
import com.unasrolitas.app.data.model.Playlist
import com.unasrolitas.app.navigation.NavRoutes
import com.unasrolitas.app.permissions.PermissionHandler
import com.unasrolitas.app.service.PlaybackService
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
import com.unasrolitas.app.util.AppLogger
import androidx.media3.session.SessionToken

class MainActivity : ComponentActivity() {

    private var musicViewModel: MusicViewModel? = null
    private var mediaController: MediaController? = null
    private var mediaControllerFuture: com.google.common.util.concurrent.ListenableFuture<MediaController>? = null

    private fun connectToPlaybackService() {
        try {
            val sessionToken = SessionToken(
                this,
                ComponentName(this, PlaybackService::class.java)
            )

            AppLogger.i(
                "MEDIA_CONTROLLER",
                "Conectando MediaController a PlaybackService"
            )

            mediaControllerFuture =
                MediaController.Builder(this, sessionToken)
                    .buildAsync()

            mediaControllerFuture?.addListener(
                {
                    try {
                        mediaController = mediaControllerFuture?.get()

                        AppLogger.i(
                            "MEDIA_CONTROLLER",
                            "MediaController conectado correctamente"
                        )
                    } catch (e: Exception) {
                        AppLogger.e(
                            "MEDIA_CONTROLLER",
                            "No se pudo conectar MediaController",
                            e
                        )
                    }
                },
                ContextCompat.getMainExecutor(this)
            )
        } catch (e: Exception) {
            AppLogger.e(
                "MEDIA_CONTROLLER",
                "Error iniciando conexión con PlaybackService",
                e
            )
        }
    }

    override fun onStart() {
        super.onStart()
        connectToPlaybackService()
    }

    override fun onStop() {
        AppLogger.i(
            "MEDIA_CONTROLLER",
            "MainActivity.onStop"
        )

        mediaController?.release()
        mediaController = null

        mediaControllerFuture?.cancel(false)
        mediaControllerFuture = null

        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        setIntent(intent)

        val incomingUri = intent.data
        if (incomingUri != null) {
            musicViewModel?.playExternalUri(incomingUri)
        }
    }
}

@Composable
fun UnasRolitasMainContent(viewModel: MusicViewModel) {
    val navController = rememberNavController()

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            AppLogger.i(
                "NOTIFICATION_PERMISSION",
                "POST_NOTIFICATIONS resultado=$granted"
            )
        }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                viewModel.getApplication<Application>(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                AppLogger.i(
                    "NOTIFICATION_PERMISSION",
                    "Solicitando POST_NOTIFICATIONS"
                )

                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            } else {
                AppLogger.i(
                    "NOTIFICATION_PERMISSION",
                    "POST_NOTIFICATIONS ya concedido"
                )
            }
        }
    }

    val exportLogLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            val success = AppLogger.exportToUri(
                context = viewModel.getApplication<Application>(),
                uri = uri
            )

            if (success) {
                AppLogger.i("LOGGER", "Log exportado correctamente")
            } else {
                AppLogger.e("LOGGER", "No se pudo exportar el log")
            }
        }
    }
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
    var songForPlaylistDialog by remember { mutableStateOf<Song?>(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var playlistForDeleteDialog by remember { mutableStateOf<Playlist?>(null) }


    /*
     * Contexto real que está viendo el usuario en LibraryScreen.
     *
     * null = estamos viendo una lista de grupos (Álbumes, Artistas,
     * Géneros, Carpetas), por lo tanto el aleatorio debe estar
     * desactivado.
     *
     * lista = estamos viendo canciones concretas y el aleatorio
     * puede actuar exclusivamente sobre esa lista.
     */
    var libraryPlaybackContext by remember {
        mutableStateOf<List<Song>?>(null)
    }

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
                    shuffleEnabled = !libraryPlaybackContext.isNullOrEmpty(),
                    onShuffleAll = {
                        libraryPlaybackContext?.let { context ->
                            viewModel.shuffleContext(context)
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
                        songs = viewModel.songsForTab(activeTab),
                        playlists = playlists,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        searchQuery = searchQuery,
                        activeTab = activeTab,
                        onSongSelect = { song ->
                            val context = libraryPlaybackContext

                            if (!context.isNullOrEmpty()) {
                                viewModel.playSongInContext(context, song)
                            } else {
                                viewModel.playSong(song)
                            }
                        },
                        onFavoriteToggle = { song ->
                            viewModel.toggleFavorite(song)
                        },
                        onSongMenuClick = { song ->
                            viewModel.setSelectedSongForMenu(song)
                        },
                        onRemoveSongFromPlaylist = { playlist, song ->
                            viewModel.removeSongFromPlaylist(
                                playlist.id,
                                song
                            )
                        },
                        onDeletePlaylist = { playlist ->
                            playlistForDeleteDialog = playlist
                        },
                        onPlaybackContextChanged = { context ->
                            libraryPlaybackContext = context
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
                        onBack = { navController.popBackStack() },
                        onExportLog = {
                            exportLogLauncher.launch("unasrolitas-debug.log")
                        }
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
                    onAddToPlaylist = { song ->
                        songForPlaylistDialog = song
                    },

                    onToggleFavorite = { song -> viewModel.toggleFavorite(song) },
                    onShowInfo = { song -> songForInfoDialog = song }
                )
            }

            // Selector de playlist
            if (songForPlaylistDialog != null) {
                val song = songForPlaylistDialog!!

                AlertDialog(
                    onDismissRequest = {
                        songForPlaylistDialog = null
                    },
                    containerColor = DarkCard,
                    title = {
                        Text(
                            text = "Añadir a playlist",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val userPlaylists =
                                playlists.filter { !it.isSystemPlaylist }

                            if (userPlaylists.isEmpty()) {
                                Text(
                                    text = "No tienes playlists creadas.",
                                    color = TextSecondary
                                )
                            } else {
                                userPlaylists.forEach { playlist ->
                                    Surface(
                                        onClick = {
                                            viewModel.addSongToPlaylist(
                                                playlist.id,
                                                song
                                            )
                                            songForPlaylistDialog = null
                                        },
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
                                                imageVector = Icons.Default.QueueMusic,
                                                contentDescription = null,
                                                tint = OrangePrimary
                                            )

                                            Spacer(
                                                modifier = Modifier.width(12.dp)
                                            )

                                            Column {
                                                Text(
                                                    text = playlist.name,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Medium
                                                )

                                                Text(
                                                    text = "${playlist.songIds.size} rolitas",
                                                    color = TextSecondary,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showCreatePlaylistDialog = true
                            }
                        ) {
                            Text(
                                text = "Nueva playlist",
                                color = OrangePrimary
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                songForPlaylistDialog = null
                            }
                        ) {
                            Text(
                                text = "Cancelar",
                                color = TextSecondary
                            )
                        }
                    }
                )
            }

            // Crear playlist
            if (showCreatePlaylistDialog) {
                var playlistName by remember {
                    mutableStateOf("")
                }
                var playlistError by remember {
                    mutableStateOf<String?>(null)
                }

                AlertDialog(
                    onDismissRequest = {
                        showCreatePlaylistDialog = false
                    },
                    containerColor = DarkCard,
                    title = {
                        Text(
                            text = "Nueva playlist",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = playlistName,
                                onValueChange = {
                                    playlistName = it
                                    playlistError = null
                                },
                                singleLine = true,
                                label = {
                                    Text("Nombre")
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (playlistError != null) {
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = playlistError!!,
                                    color = Color.Red,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val created =
                                    viewModel.createPlaylist(playlistName)

                                if (created == null) {
                                    playlistError =
                                        if (playlistName.trim().isBlank()) {
                                            "Escribe un nombre para la playlist."
                                        } else {
                                            "Ya existe una playlist con ese nombre."
                                        }
                                } else {
                                    val song = songForPlaylistDialog

                                    if (song != null) {
                                        viewModel.addSongToPlaylist(
                                            created.id,
                                            song
                                        )
                                    }

                                    songForPlaylistDialog = null
                                    showCreatePlaylistDialog = false
                                }
                            }
                        ) {
                            Text(
                                text = "Crear",
                                color = OrangePrimary
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showCreatePlaylistDialog = false
                            }
                        ) {
                            Text(
                                text = "Cancelar",
                                color = TextSecondary
                            )
                        }
                    }
                )
            }

            // Confirmar eliminación de playlist
            if (playlistForDeleteDialog != null) {
                val playlist = playlistForDeleteDialog!!

                AlertDialog(
                    onDismissRequest = {
                        playlistForDeleteDialog = null
                    },
                    containerColor = DarkCard,
                    title = {
                        Text(
                            text = "Eliminar playlist",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    text = {
                        Text(
                            text = "¿Quieres eliminar la playlist \"${playlist.name}\"? Las canciones no se eliminarán de tu biblioteca.",
                            color = TextSecondary
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deletePlaylist(playlist.id)
                                playlistForDeleteDialog = null
                            }
                        ) {
                            Text(
                                text = "Eliminar",
                                color = Color.Red
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                playlistForDeleteDialog = null
                            }
                        ) {
                            Text(
                                text = "Cancelar",
                                color = TextSecondary
                            )
                        }
                    }
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

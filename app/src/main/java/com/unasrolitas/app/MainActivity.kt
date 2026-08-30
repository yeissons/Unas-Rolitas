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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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

    val importPlaylistLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                viewModel.getApplication<Application>().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
                // Algunos proveedores no permiten permisos persistentes.
            }

            val importedPlaylists = viewModel.importPlaylists(uri)

            if (importedPlaylists.isEmpty()) {
                AppLogger.e(
                    "PLAYLIST_IMPORT",
                    "No se pudo importar ninguna playlist: $uri"
                )
            } else {
                AppLogger.i(
                    "PLAYLIST_IMPORT",
                    "Playlists importadas: ${importedPlaylists.size}"
                )

                importedPlaylists.forEach { playlist ->
                    AppLogger.i(
                        "PLAYLIST_IMPORT",
                        "Importada: ${playlist.name} (${playlist.sourceFormat})"
                    )
                }
            }
        }
    }

    var playlistForExport by remember { mutableStateOf<Playlist?>(null) }

    val exportPlaylistLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("audio/x-mpegurl")
    ) { uri ->
        val playlist = playlistForExport

        if (uri != null && playlist != null) {
            val success = viewModel.exportPlaylistToUri(
                playlistId = playlist.id,
                uri = uri
            )

            if (success) {
                AppLogger.i(
                    "PLAYLIST",
                    "Playlist exportada: ${playlist.name}"
                )
            } else {
                AppLogger.e(
                    "PLAYLIST",
                    "No se pudo exportar la playlist: ${playlist.name}"
                )
            }
        }

        playlistForExport = null
    }

    var playlistsForExport by remember {
        mutableStateOf<List<Playlist>>(emptyList())
    }

    val exportPlaylistsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        val selectedPlaylists = playlistsForExport

        if (uri != null && selectedPlaylists.isNotEmpty()) {
            val success = viewModel.exportPlaylistsToUri(
                playlistIds = selectedPlaylists.map { it.id },
                uri = uri
            )

            if (success) {
                AppLogger.i(
                    "PLAYLIST",
                    "Playlists exportadas en ZIP: ${selectedPlaylists.size}"
                )
            } else {
                AppLogger.e(
                    "PLAYLIST",
                    "No se pudieron exportar las playlists seleccionadas"
                )
            }
        }

        playlistsForExport = emptyList()
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
    val sortMode by viewModel.sortMode.collectAsState()
    val sortDescending by viewModel.sortDescending.collectAsState()
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
    var playlistForRenameDialog by remember { mutableStateOf<Playlist?>(null) }
    var playlistForDeleteDialog by remember { mutableStateOf<Playlist?>(null) }
    var selectedPlaylistForMenu by remember { mutableStateOf<Playlist?>(null) }
    var playlistForAddSongs by remember { mutableStateOf<Playlist?>(null) }


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
                    },
                    sortEnabled = libraryPlaybackContext != null,

                    sortMode = sortMode,
                    sortDescending = sortDescending,

                    onSortSelected = { mode, descending ->
                        viewModel.setSortMode(mode)
                        viewModel.setSortDescending(descending)
                    },
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
                        songs = if (activeTab == "PLAYLISTS") {
                              songs
                          } else {
                              viewModel.songsForTab(activeTab)
                          },
                        playlists = playlists,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        searchQuery = searchQuery,
                        activeTab = activeTab,
                        sortMode = sortMode,
                        sortDescending = sortDescending,
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
                        onSongMenuClick = { song, playlist ->
                            viewModel.setSelectedSongForMenu(song)
                            selectedPlaylistForMenu = playlist
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
                        onRenamePlaylist = { playlist ->
                            playlistForRenameDialog = playlist
                        },
                        onAddSongsToPlaylist = { playlist ->
                            playlistForAddSongs = playlist
                        },
                        onExportPlaylist = { playlist ->
                            playlistForExport = playlist
                            exportPlaylistLauncher.launch(
                                "${playlist.name}.m3u8"
                            )
                        },
                        onExportPlaylists = { selectedPlaylists ->
                            playlistsForExport = selectedPlaylists

                            exportPlaylistsLauncher.launch(
                                "unas_rolitas_playlists.zip"
                            )
                        },
                        onCreatePlaylist = {
                            showCreatePlaylistDialog = true
                        },
                        onImportPlaylist = {
                            importPlaylistLauncher.launch(
                                arrayOf(
                                    "audio/x-mpegurl",
                                    "audio/mpegurl",
                                    "application/vnd.apple.mpegurl",
                                    "audio/x-scpls",
                                    "application/pls+xml",
                                    "application/vnd.ms-wpl",
                                    "application/zip",
                                    "*/*"
                                )
                            )
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
                    onDismiss = {
                        viewModel.setSelectedSongForMenu(null)
                        selectedPlaylistForMenu = null
                    },
                    onPlayNow = { song -> viewModel.playSong(song) },
                    onPlayNext = { song -> viewModel.playNextSong(song) },
                    onAddToQueue = { song -> viewModel.addSongToQueue(song) },
                    onRemoveFromPlaylist = selectedPlaylistForMenu?.let { playlist ->
                        { song ->
                            viewModel.removeSongFromPlaylist(
                                playlist.id,
                                song
                            )
                        }
                    },
                    onAddToPlaylist = { song ->
                        songForPlaylistDialog = song
                    },

                    onToggleFavorite = { song -> viewModel.toggleFavorite(song) },
                    onShowInfo = { song -> songForInfoDialog = song }
                )
            }

            // Añadir varias canciones a una playlist
            if (playlistForAddSongs != null) {
                val playlist = playlistForAddSongs!!

                var addSongsQuery by remember(playlist) {
                    mutableStateOf("")
                }

                var selectedSongIds by remember(playlist) {
                    mutableStateOf(emptySet<Long>())
                }

                val playlistSongIds = playlist.songIds.toSet()

                val availableSongs = remember(
                    songs,
                    addSongsQuery,
                    playlistSongIds
                ) {
                    songs.filter { song ->
                        song.id !in playlistSongIds &&
                            (
                                addSongsQuery.isBlank() ||
                                    song.title.contains(
                                        addSongsQuery,
                                        ignoreCase = true
                                    ) ||
                                    song.artist.contains(
                                        addSongsQuery,
                                        ignoreCase = true
                                    ) ||
                                    song.album.contains(
                                        addSongsQuery,
                                        ignoreCase = true
                                    )
                            )
                    }
                }

                AlertDialog(
                    onDismissRequest = {
                        playlistForAddSongs = null
                    },
                    containerColor = DarkCard,
                    title = {
                        Text(
                            text = "Añadir canciones",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = playlist.name,
                                color = TextSecondary,
                                fontSize = 13.sp
                            )

                            Spacer(
                                modifier = Modifier.height(10.dp)
                            )

                            OutlinedTextField(
                                value = addSongsQuery,
                                onValueChange = {
                                    addSongsQuery = it
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                label = {
                                    Text("Buscar")
                                }
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 320.dp)
                            ) {
                                items(
                                    items = availableSongs,
                                    key = { it.id }
                                ) { song ->
                                    val selected =
                                        song.id in selectedSongIds

                                    ListItem(
                                        headlineContent = {
                                            Text(
                                                text = song.title,
                                                color = Color.White,
                                                maxLines = 1
                                            )
                                        },
                                        supportingContent = {
                                            Text(
                                                text = song.artist,
                                                color = TextSecondary,
                                                maxLines = 1
                                            )
                                        },
                                        leadingContent = {
                                            Checkbox(
                                                checked = selected,
                                                onCheckedChange = {
                                                    selectedSongIds =
                                                        if (it) {
                                                            selectedSongIds +
                                                                song.id
                                                        } else {
                                                            selectedSongIds -
                                                                song.id
                                                        }
                                                }
                                            )
                                        },
                                        modifier = Modifier.clickable {
                                            selectedSongIds =
                                                if (selected) {
                                                    selectedSongIds -
                                                        song.id
                                                } else {
                                                    selectedSongIds +
                                                        song.id
                                                }
                                        },
                                        colors = ListItemDefaults.colors(
                                            containerColor =
                                                Color.Transparent
                                        )
                                    )
                                }
                            }

                            if (availableSongs.isEmpty()) {
                                Spacer(
                                    modifier = Modifier.height(12.dp)
                                )

                                Text(
                                    text = if (songs.isEmpty()) {
                                        "No hay canciones en la biblioteca."
                                    } else {
                                        "No hay canciones disponibles para añadir."
                                    },
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            enabled = selectedSongIds.isNotEmpty(),
                            onClick = {
                                selectedSongIds.forEach { songId ->
                                    songs.firstOrNull {
                                        it.id == songId
                                    }?.let { song ->
                                        viewModel.addSongToPlaylist(
                                            playlist.id,
                                            song
                                        )
                                    }
                                }

                                playlistForAddSongs = null
                            }
                        ) {
                            Text(
                                text =
                                    "Añadir (${selectedSongIds.size})",
                                color = OrangePrimary
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                playlistForAddSongs = null
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
            // Renombrar playlist
            if (playlistForRenameDialog != null) {
                val playlist = playlistForRenameDialog
                var playlistName by remember(playlist) {
                    mutableStateOf(playlist?.name ?: "")
                }
                var playlistError by remember(playlist) {
                    mutableStateOf<String?>(null)
                }

                AlertDialog(
                    onDismissRequest = {
                        playlistForRenameDialog = null
                    },
                    containerColor = DarkCard,
                    title = {
                        Text(
                            text = "Renombrar playlist",
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
                                val target = playlist

                                if (target == null) {
                                    playlistForRenameDialog = null
                                    return@TextButton
                                }

                                val renamed = viewModel.renamePlaylist(
                                    target.id,
                                    playlistName
                                )

                                if (renamed) {
                                    playlistForRenameDialog = null
                                } else {
                                    playlistError =
                                        if (playlistName.trim().isBlank()) {
                                            "Escribe un nombre para la playlist."
                                        } else {
                                            "Ya existe una playlist con ese nombre."
                                        }
                                }
                            }
                        ) {
                            Text(
                                text = "Guardar",
                                color = OrangePrimary
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                playlistForRenameDialog = null
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

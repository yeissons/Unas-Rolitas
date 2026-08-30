package com.unasrolitas.app.data.repository

import android.content.Context
import android.net.Uri
import com.unasrolitas.app.data.model.Playlist
import com.unasrolitas.app.data.model.Song
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.zip.ZipInputStream

class PlaylistFileRepository(private val context: Context) {

    private val resolver = context.contentResolver

    fun readPlaylist(
        uri: Uri,
        songs: List<Song>
    ): Playlist? {
        val format = detectFormat(uri) ?: return null

        if (format == "zip") {
            return readZipFirstPlaylist(uri, songs)
        }

        val content = try {
            resolver.openInputStream(uri)?.use { input ->
                BufferedReader(
                    InputStreamReader(
                        input,
                        StandardCharsets.UTF_8
                    )
                ).readText()
            }
        } catch (_: Exception) {
            null
        } ?: return null

        val entries = when (format) {
            "m3u", "m3u8" -> parseM3u(content)
            "pls" -> parsePls(content)
            "wpl" -> parseWpl(content)
            else -> emptyList()
        }

        if (entries.isEmpty()) return null

        val matchedSongIds = entries
            .mapNotNull { entry ->
                resolveSong(entry, songs)?.id
            }
            .distinct()

        if (matchedSongIds.isEmpty()) return null

        return Playlist(
            id = "file_${uri}",
            name = playlistName(uri),
            description = "Playlist importada",
            songIds = matchedSongIds,
            isSystemPlaylist = false,
            isExternalFile = true,
            sourceUri = uri,
            sourceFormat = format
        )
    }

    fun writeM3u8(
        uri: Uri,
        playlist: Playlist,
        songs: List<Song>
    ): Boolean {
        return try {
            val songsById = songs.associateBy { it.id }

            val content = buildString {
                append("#EXTM3U\n")

                playlist.songIds.forEach { songId ->
                    val song = songsById[songId] ?: return@forEach

                    append("#EXTINF:-1,")

                    if (song.artist.isNotBlank()) {
                        append(song.artist.trim())
                        append(" - ")
                    }

                    append(song.title.trim())
                    append("\n")

                    append(song.filePath)
                    append("\n")
                }
            }

            resolver.openOutputStream(uri)?.use { output ->
                output.write(
                    content.toByteArray(StandardCharsets.UTF_8)
                )
            } ?: return false

            true
        } catch (_: Exception) {
            false
        }
    }

    fun writePlaylistsZip(
        uri: Uri,
        playlists: List<Playlist>,
        songs: List<Song>
    ): Boolean {
        if (playlists.isEmpty()) return false

        return try {
            val usedNames = mutableSetOf<String>()

            resolver.openOutputStream(uri)?.use { output ->
                ZipOutputStream(output).use { zip ->
                    playlists.forEach { playlist ->
                        val baseName = sanitizeZipEntryName(playlist.name)

                        var entryName = "$baseName.m3u8"
                        var suffix = 2

                        while (!usedNames.add(entryName.lowercase())) {
                            entryName = "$baseName ($suffix).m3u8"
                            suffix++
                        }

                        zip.putNextEntry(ZipEntry(entryName))

                        zip.write(
                            buildM3u8Content(
                                playlist = playlist,
                                songs = songs
                            ).toByteArray(StandardCharsets.UTF_8)
                        )

                        zip.closeEntry()
                    }
                }
            } ?: return false

            true
        } catch (_: Exception) {
            false
        }
    }

    private fun buildM3u8Content(
        playlist: Playlist,
        songs: List<Song>
    ): String {
        val songsById = songs.associateBy { it.id }

        return buildString {
            append("#EXTM3U\n")

            playlist.songIds.forEach { songId ->
                val song = songsById[songId] ?: return@forEach

                append("#EXTINF:-1,")

                if (song.artist.isNotBlank()) {
                    append(song.artist.trim())
                    append(" - ")
                }

                append(song.title.trim())
                append("\n")

                append(song.filePath)
                append("\n")
            }
        }
    }

    private fun sanitizeZipEntryName(name: String): String {
        return name
            .trim()
            .replace(Regex("""[\\/:*?"<>|]"""), "_")
            .replace(Regex("""\s+"""), " ")
            .trim()
            .take(100)
            .ifBlank { "Playlist" }
    }

    fun readPlaylists(
        uri: Uri,
        songs: List<Song>
    ): List<Playlist> {
        val format = detectFormat(uri) ?: return emptyList()

        if (format != "zip") {
            return listOfNotNull(readPlaylist(uri, songs))
        }

        return readZipPlaylists(uri, songs)
    }

    private fun readZipFirstPlaylist(
        uri: Uri,
        songs: List<Song>
    ): Playlist? {
        return readZipPlaylists(uri, songs).firstOrNull()
    }

    private fun readZipPlaylists(
        uri: Uri,
        songs: List<Song>
    ): List<Playlist> {
        val result = mutableListOf<Playlist>()

        try {
            resolver.openInputStream(uri)?.use { input ->
                ZipInputStream(input).use { zip ->
                    var entry = zip.nextEntry

                    while (entry != null) {
                        if (!entry.isDirectory) {
                            val entryName = entry.name
                            val lowerName = entryName.lowercase()

                            if (
                                lowerName.endsWith(".m3u") ||
                                lowerName.endsWith(".m3u8") ||
                                lowerName.endsWith(".pls") ||
                                lowerName.endsWith(".wpl")
                            ) {
                                val contentBytes = zip.readBytes()
                                val content = String(
                                    contentBytes,
                                    StandardCharsets.UTF_8
                                )

                                val entries = when {
                                    lowerName.endsWith(".m3u") ||
                                    lowerName.endsWith(".m3u8") ->
                                        parseM3u(content)

                                    lowerName.endsWith(".pls") ->
                                        parsePls(content)

                                    lowerName.endsWith(".wpl") ->
                                        parseWpl(content)

                                    else ->
                                        emptyList()
                                }

                                val matchedSongIds = entries
                                    .mapNotNull { playlistEntry ->
                                        resolveSong(
                                            playlistEntry,
                                            songs
                                        )?.id
                                    }
                                    .distinct()

                                if (matchedSongIds.isNotEmpty()) {
                                    val playlistName =
                                        entryName
                                            .substringAfterLast('/')
                                            .substringBeforeLast('.')
                                            .trim()
                                            .ifBlank {
                                                "Playlist"
                                            }

                                    result += Playlist(
                                        id = "zip_${uri}_${result.size}_$playlistName",
                                        name = playlistName,
                                        description = "Playlist importada desde ZIP",
                                        songIds = matchedSongIds,
                                        isSystemPlaylist = false,
                                        isExternalFile = true,
                                        sourceUri = uri,
                                        sourceFormat = lowerName
                                            .substringAfterLast('.')
                                    )
                                }
                            }
                        }

                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }
        } catch (_: Exception) {
            return emptyList()
        }

        return result
    }

    private fun detectFormat(uri: Uri): String? {
        val uriValue = uri.toString()
            .substringBefore('?')
            .substringBefore('#')
            .lowercase()

        val displayName = try {
            resolver.query(
                uri,
                arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(0)
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            null
        }

        val nameValue = displayName
            ?.substringBefore('?')
            ?.substringBefore('#')
            ?.lowercase()
            .orEmpty()

        val mimeType = try {
            resolver.getType(uri)
                ?.lowercase()
                .orEmpty()
        } catch (_: Exception) {
            ""
        }

        return when {
            uriValue.endsWith(".m3u8") ||
                nameValue.endsWith(".m3u8") ||
                mimeType == "application/vnd.apple.mpegurl" ->
                "m3u8"

            uriValue.endsWith(".m3u") ||
                nameValue.endsWith(".m3u") ||
                mimeType == "audio/x-mpegurl" ||
                mimeType == "audio/mpegurl" ->
                "m3u"

            uriValue.endsWith(".pls") ||
                nameValue.endsWith(".pls") ||
                mimeType == "audio/x-scpls" ||
                mimeType == "application/pls+xml" ->
                "pls"

            uriValue.endsWith(".wpl") ||
                nameValue.endsWith(".wpl") ||
                mimeType == "application/vnd.ms-wpl" ->
                "wpl"

            uriValue.endsWith(".zip") ||
                nameValue.endsWith(".zip") ||
                mimeType == "application/zip" ||
                mimeType == "application/x-zip-compressed" ->
                "zip"

            else -> null
        }
    }

    private fun playlistName(uri: Uri): String {
        val raw = uri.lastPathSegment
            ?.substringAfterLast('/')
            ?.substringBeforeLast('.')
            ?.trim()

        return raw?.takeIf { it.isNotBlank() } ?: "Playlist"
    }

    private fun parseM3u(content: String): List<String> {
        return content
            .removePrefix("\uFEFF")
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filterNot { it.startsWith("#") }
            .toList()
    }

    private fun parsePls(content: String): List<String> {
        return content
            .removePrefix("\uFEFF")
            .lineSequence()
            .map { it.trim() }
            .filter {
                it.startsWith("File", ignoreCase = true) &&
                    it.contains('=')
            }
            .mapNotNull { line ->
                line.substringAfter('=')
                    .trim()
                    .removeSurrounding("\"")
                    .takeIf { it.isNotBlank() }
            }
            .toList()
    }

    private fun parseWpl(content: String): List<String> {
        val entries = mutableListOf<String>()

        val regex = Regex(
            """(?i)<media\b[^>]*\bsrc\s*=\s*["']([^"']+)["']"""
        )

        regex.findAll(content).forEach { match ->
            match.groupValues
                .getOrNull(1)
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?.let(entries::add)
        }

        return entries
    }

    private fun resolveSong(
        entry: String,
        songs: List<Song>
    ): Song? {
        val normalizedEntry = normalizePath(entry)
        val entryName = normalizedEntry.substringAfterLast('/')

        return songs.firstOrNull { song ->
            val songPath = normalizePath(song.filePath)
            val songName = normalizePath(song.filePath)
                .substringAfterLast('/')

            when {
                songPath.isNotBlank() &&
                    songPath == normalizedEntry -> true

                songPath.isNotBlank() &&
                    songPath.endsWith("/$normalizedEntry") -> true

                songPath.isNotBlank() &&
                    normalizedEntry.endsWith("/$songPath") -> true

                songName.isNotBlank() &&
                    songName == entryName -> true

                normalizeText(song.title) ==
                    normalizeText(entryName.substringBeforeLast('.')) -> true

                else -> false
            }
        }
    }

    private fun normalizePath(value: String): String {
        return value
            .trim()
            .removePrefix("\"")
            .removeSuffix("\"")
            .removePrefix("'")
            .removeSuffix("'")
            .replace('\\', '/')
            .replace("%20", " ")
            .replace("file://", "")
            .trim()
            .lowercase()
    }

    private fun normalizeText(value: String): String {
        return value
            .trim()
            .lowercase()
            .replace("\\", "/")
            .substringAfterLast('/')
            .substringBeforeLast('.')
            .trim()
    }
}

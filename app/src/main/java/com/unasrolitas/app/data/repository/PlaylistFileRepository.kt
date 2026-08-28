package com.unasrolitas.app.data.repository

import android.content.Context
import android.net.Uri
import com.unasrolitas.app.data.model.Playlist
import com.unasrolitas.app.data.model.Song
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class PlaylistFileRepository(private val context: Context) {

    private val resolver = context.contentResolver

    fun readPlaylist(
        uri: Uri,
        songs: List<Song>
    ): Playlist? {
        val format = detectFormat(uri) ?: return null

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

    private fun detectFormat(uri: Uri): String? {
        val value = uri.toString()
            .substringBefore('?')
            .substringBefore('#')
            .lowercase()

        return when {
            value.endsWith(".m3u8") -> "m3u8"
            value.endsWith(".m3u") -> "m3u"
            value.endsWith(".pls") -> "pls"
            value.endsWith(".wpl") -> "wpl"
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

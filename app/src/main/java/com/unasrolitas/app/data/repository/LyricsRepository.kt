package com.unasrolitas.app.data.repository

import android.content.Context
import com.unasrolitas.app.data.model.LyricLine
import com.unasrolitas.app.data.model.Lyrics
import com.unasrolitas.app.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.regex.Pattern

class LyricsRepository(private val context: Context) {

    private val cacheDir = File(context.cacheDir, "lyrics_cache").apply { if (!exists()) mkdirs() }

    suspend fun getLyricsForSong(song: Song): Lyrics = withContext(Dispatchers.IO) {
        val cacheFile = File(cacheDir, "${song.id}.json")

        // 1. Check local cache
        if (cacheFile.exists()) {
            try {
                val jsonStr = cacheFile.readText()
                val parsed = parseLyricsJson(song.id, jsonStr)
                if (parsed != null) return@withContext parsed
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. Query LRCLIB API (public, free open-source LRC synced lyrics service)
        try {
            val trackName = URLEncoder.encode(song.title, "UTF-8")
            val artistName = URLEncoder.encode(song.artist, "UTF-8")
            val albumName = URLEncoder.encode(song.album, "UTF-8")
            val durationSec = song.durationMs / 1000

            val getUrl = "https://lrclib.net/api/get?track_name=$trackName&artist_name=$artistName&album_name=$albumName&duration=$durationSec"
            val jsonObject = fetchJsonFromUrl(getUrl)

            var syncedLrc: String? = jsonObject?.optString("syncedLyrics")
            var plainLrc: String? = jsonObject?.optString("plainLyrics")

            // If direct match failed, try search endpoint
            if (syncedLrc.isNullOrBlank() && plainLrc.isNullOrBlank()) {
                val searchQuery = URLEncoder.encode("${song.artist} ${song.title}", "UTF-8")
                val searchUrl = "https://lrclib.net/api/search?q=$searchQuery"
                val searchConn = URL(searchUrl).openConnection() as HttpURLConnection
                try {
                    searchConn.connectTimeout = 4000
                    searchConn.readTimeout = 4000
                    if (searchConn.responseCode == HttpURLConnection.HTTP_OK) {
                        val resText = searchConn.inputStream.bufferedReader().use { it.readText() }
                        val array = JSONArray(resText)
                        if (array.length() > 0) {
                            val firstMatch = array.getJSONObject(0)
                            syncedLrc = firstMatch.optString("syncedLyrics")
                            plainLrc = firstMatch.optString("plainLyrics")
                        }
                    }
                } finally {
                    searchConn.disconnect()
                }
            }

            if (!syncedLrc.isNullOrBlank() || !plainLrc.isNullOrBlank()) {
                val cacheJson = JSONObject().apply {
                    put("syncedLyrics", syncedLrc ?: "")
                    put("plainLyrics", plainLrc ?: "")
                }
                cacheFile.writeText(cacheJson.toString())
                val parsed = parseLyricsJson(song.id, cacheJson.toString())
                if (parsed != null) return@withContext parsed
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Return empty lyrics object when no LRC available
        Lyrics(
            songId = song.id,
            isSynced = false,
            lines = emptyList(),
            plainText = "",
            source = "None"
        )
    }

    private fun fetchJsonFromUrl(urlStr: String): JSONObject? {
        return try {
            val conn = URL(urlStr).openConnection() as HttpURLConnection
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            conn.requestMethod = "GET"
            if (conn.responseCode == 200) {
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                JSONObject(text)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun parseLyricsJson(songId: Long, jsonStr: String): Lyrics? {
        return try {
            val json = JSONObject(jsonStr)
            val syncedLrc = json.optString("syncedLyrics", "")
            val plainLrc = json.optString("plainLyrics", "")

            if (syncedLrc.isNotBlank()) {
                val lines = parseLrcString(syncedLrc)
                Lyrics(
                    songId = songId,
                    isSynced = true,
                    lines = lines,
                    plainText = plainLrc.ifBlank { lines.joinToString("\n") { it.text } },
                    source = "LRCLIB"
                )
            } else if (plainLrc.isNotBlank()) {
                Lyrics(
                    songId = songId,
                    isSynced = false,
                    lines = emptyList(),
                    plainText = plainLrc,
                    source = "LRCLIB"
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun parseLrcString(lrcText: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        val timestampPattern = Pattern.compile("\\[(\\d{1,3}):(\\d{2})(?:[\\.:](\\d{1,3}))?\\]")

        lrcText.lines().forEach { rawLine ->
            val matcher = timestampPattern.matcher(rawLine)
            val timestamps = mutableListOf<Long>()
            var textStart = 0
            while (matcher.find()) {
                val minutes = matcher.group(1)?.toLongOrNull() ?: 0L
                val seconds = matcher.group(2)?.toLongOrNull() ?: 0L
                val fraction = matcher.group(3).orEmpty()
                val fractionMs = when (fraction.length) {
                    1 -> fraction.toLong() * 100L
                    2 -> fraction.toLong() * 10L
                    else -> fraction.take(3).toLongOrNull() ?: 0L
                }
                timestamps += (minutes * 60 + seconds) * 1000 + fractionMs
                textStart = matcher.end()
            }

            val text = rawLine.substring(textStart).trim()
            if (text.isNotBlank()) {
                timestamps.forEach { timestampMs ->
                    lines.add(LyricLine(timestampMs = timestampMs, text = text))
                }
            }
        }
        return lines.sortedBy { it.timestampMs }
    }
}

package com.unasrolitas.app.data.repository

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.unasrolitas.app.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class CoverArtRepository(private val context: Context) {

    private val cacheDir = File(context.cacheDir, "artwork_cache").apply { mkdirs() }

    suspend fun getArtworkForSong(song: Song): Uri? = withContext(Dispatchers.IO) {
        val cachedFile = File(cacheDir, "${song.id}.jpg")
        if (cachedFile.isFile && cachedFile.length() > 0) {
            return@withContext Uri.fromFile(cachedFile)
        }

        // Prefer embedded artwork from the actual MediaStore content URI.
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, song.uri)
            val embeddedBytes = retriever.embeddedPicture
            retriever.release()
            if (!embeddedBytes.isNullOrEmpty()) {
                FileOutputStream(cachedFile).use { it.write(embeddedBytes) }
                return@withContext Uri.fromFile(cachedFile)
            }
        } catch (_: Exception) {
            // Continue with MediaStore/remote lookup.
        }

        song.artworkUri?.let { uri ->
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    if (stream.read() >= 0) return@withContext uri
                }
            } catch (_: Exception) {
                // Continue with remote lookup.
            }
        }

        val query = "${song.artist} ${song.title}".trim()
        if (query.isBlank()) return@withContext null

        var searchConn: HttpURLConnection? = null
        var imageConn: HttpURLConnection? = null
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val apiUrl = "https://itunes.apple.com/search?term=$encodedQuery&entity=song&limit=1"
            searchConn = URL(apiUrl).openConnection() as HttpURLConnection
            searchConn.connectTimeout = 4000
            searchConn.readTimeout = 4000
            searchConn.requestMethod = "GET"

            if (searchConn.responseCode != HttpURLConnection.HTTP_OK) return@withContext null

            val responseText = searchConn.inputStream.bufferedReader().use { it.readText() }
            val results = JSONObject(responseText).optJSONArray("results")
            val artworkUrl100 = results?.optJSONObject(0)?.optString("artworkUrl100").orEmpty()
            if (artworkUrl100.isBlank()) return@withContext null

            val highResUrl = artworkUrl100.replace("100x100bb", "600x600bb")
            imageConn = URL(highResUrl).openConnection() as HttpURLConnection
            imageConn.connectTimeout = 4000
            imageConn.readTimeout = 4000
            if (imageConn.responseCode != HttpURLConnection.HTTP_OK) return@withContext null

            imageConn.inputStream.use { input ->
                FileOutputStream(cachedFile).use { output -> input.copyTo(output) }
            }
            if (cachedFile.length() > 0) Uri.fromFile(cachedFile) else null
        } catch (_: Exception) {
            null
        } finally {
            searchConn?.disconnect()
            imageConn?.disconnect()
        }
    }
}

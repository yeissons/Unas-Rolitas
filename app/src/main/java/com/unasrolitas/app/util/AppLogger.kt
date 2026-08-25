package com.unasrolitas.app.util

import android.content.Context
import android.net.Uri
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppLogger {

    private const val TAG = "UnasRolitas"
    private const val FILE_NAME = "unasrolitas-debug.log"

    private val lock = Any()

    private val formatter =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    @Volatile
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        i("LOGGER", "Logger inicializado")
    }

    fun exportToUri(context: Context, uri: Uri): Boolean {
        return runCatching {
            val file = appContext
                ?.filesDir
                ?.resolve(FILE_NAME)

            if (file == null || !file.exists()) {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write("Unas Rolitas - no hay registro disponible.\\n".toByteArray())
                } ?: error("No se pudo abrir el destino")
            } else {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    file.inputStream().use { input ->
                        input.copyTo(output)
                    }
                } ?: error("No se pudo abrir el destino")
            }

            true
        }.getOrElse {
            e("LOGGER", "Error exportando log", it)
            false
        }
    }

    fun i(area: String, message: String) {
        write("I", area, message, null)
    }

    fun w(area: String, message: String, throwable: Throwable? = null) {
        write("W", area, message, throwable)
    }

    fun e(area: String, message: String, throwable: Throwable? = null) {
        write("E", area, message, throwable)
    }

    private fun write(
        level: String,
        area: String,
        message: String,
        throwable: Throwable?
    ) {
        val line = buildString {
            append(formatter.format(Date()))
            append(" ")
            append(level)
            append("/")
            append(area)
            append(": ")
            append(message)

            if (throwable != null) {
                append("\n")
                append(Log.getStackTraceString(throwable))
            }
        }

        when (level) {
            "E" -> Log.e(TAG, line)
            "W" -> Log.w(TAG, line)
            else -> Log.i(TAG, line)
        }

        synchronized(lock) {
            runCatching {
                val file = appContext
                    ?.filesDir
                    ?.resolve(FILE_NAME)
                    ?: return@runCatching

                file.appendText(line + "\n")
            }
        }
    }
}

package com.unasrolitas.app

import android.app.Application
import com.unasrolitas.app.util.AppLogger

class UnasRolitasApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        AppLogger.init(this)

        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            AppLogger.e(
                "CRASH",
                "Excepcion no controlada en thread=${thread.name}",
                throwable
            )

            previousHandler?.uncaughtException(thread, throwable)
        }
    }
}

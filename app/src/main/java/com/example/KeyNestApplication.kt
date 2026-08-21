package com.example

import android.app.Application
import timber.log.Timber

class KeyNestApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.tag("KeyNestApp").i("Application onCreate: Starting KeyNest startup sequence")

        setupCrashLogging()

        Timber.tag("KeyNestApp").i("Application onCreate: Startup sequence completed successfully")
    }

    private fun setupCrashLogging() {
        val defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Timber.tag("KeyNestApp").e(
                throwable,
                "FATAL CRASH DETECTED on thread [%s]: %s",
                thread.name,
                throwable.message
            )
            defaultUncaughtExceptionHandler?.uncaughtException(thread, throwable)
        }
        Timber.tag("KeyNestApp").d("Uncaught exception crash tracking handler attached")
    }
}

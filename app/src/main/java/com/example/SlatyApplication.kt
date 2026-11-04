package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class SlatyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("FATAL_CRASH", "Uncaught exception in thread ${thread.name}: ${throwable.message}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val app = FirebaseApp.initializeApp(this)
                Log.i("SlatyApplication", "FirebaseApp initialized: ${app?.name ?: "default"}")
            } else {
                Log.i("SlatyApplication", "FirebaseApp already initialized with ${FirebaseApp.getApps(this).size} app(s)")
            }
        } catch (e: Exception) {
            Log.e("SlatyApplication", "Failed to initialize FirebaseApp", e)
        }
    }
}

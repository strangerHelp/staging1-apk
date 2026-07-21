package com.strangerhelp.app

import android.app.Application
import com.strangerhelp.app.utils.AppLogger
import kotlinx.coroutines.CoroutineExceptionHandler

class StrangerHelpApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Setup global uncaught exception handler to capture unhandled crashes
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            AppLogger.e("CrashHandler", "Uncaught exception in thread ${thread.name}", exception)
            defaultHandler?.uncaughtException(thread, exception)
        }
        
        AppLogger.i("StrangerHelpApp", "Application started successfully.")
    }
    
    companion object {
        // Global CoroutineExceptionHandler for "unhandled promise rejections" equivalent
        val globalExceptionHandler = CoroutineExceptionHandler { _, exception ->
            AppLogger.e("CoroutineException", "Unhandled coroutine exception", exception)
        }
    }
}

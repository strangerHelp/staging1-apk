package com.strangerhelp.app

import android.app.Application
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import androidx.work.ExistingPeriodicWorkPolicy
import com.strangerhelp.app.worker.SyncWorker
import androidx.room.Room
import com.strangerhelp.app.data.local.AppDatabase
import com.strangerhelp.app.utils.AppLogger
import kotlinx.coroutines.CoroutineExceptionHandler

class StrangerHelpApp : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        
        instance = this
        
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "strangerhelp-db"
        ).fallbackToDestructiveMigration().build()
        
        // Setup global uncaught exception handler to capture unhandled crashes
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            AppLogger.e("CrashHandler", "Uncaught exception in thread ${thread.name}", exception)
            defaultHandler?.uncaughtException(thread, exception)
        }
        
        setupBackgroundSync()
        com.strangerhelp.app.utils.BatteryMonitor.init(this)
        com.strangerhelp.app.util.MapHelper.initMap(this)
        AppLogger.i("StrangerHelpApp", "Application started successfully.")
    }
    
    private fun setupBackgroundSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true) // Battery-saving mode: reduces background task intensity
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SyncTasks",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    companion object {
        lateinit var instance: StrangerHelpApp
            private set
            
        // Global CoroutineExceptionHandler for "unhandled promise rejections" equivalent
        val globalExceptionHandler = CoroutineExceptionHandler { _, exception ->
            AppLogger.e("CoroutineException", "Unhandled coroutine exception", exception)
        }
    }
}

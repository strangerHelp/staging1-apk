package com.strangerhelp.app

import android.app.Application
import coil.ImageLoaderFactory
import coil.ImageLoader
import com.strangerhelp.app.utils.DataUriFetcher
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
import kotlinx.coroutines.launch

class StrangerHelpApp : Application(), ImageLoaderFactory {

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
        
        com.strangerhelp.app.data.api.ApiClient.init(this)
        com.strangerhelp.app.utils.NetworkMonitor.init(this)
        setupBackgroundSync()
        com.strangerhelp.app.utils.BatteryMonitor.init(this)
        com.strangerhelp.app.util.MapHelper.initMap(this)

        // Seed initial tasks in Room database if empty so user always sees tasks even before network loads
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                if (database.taskDao().getAllTasksList().isEmpty()) {
                    database.taskDao().insertTasks(com.strangerhelp.app.data.repository.DEFAULT_SEED_TASKS)
                }
            } catch (e: Exception) {
                AppLogger.e("StrangerHelpApp", "Failed to seed default tasks", e)
            }
        }

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

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(DataUriFetcher.Factory())
            }
            .build()
    }

}
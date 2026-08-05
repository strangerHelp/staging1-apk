package com.strangerhelp.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.strangerhelp.app.StrangerHelpApp
import com.strangerhelp.app.data.api.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Fetch latest tasks (requests)
            val response = ApiClient.api.getTasks()
            if (response.isSuccessful) {
                val tasks = response.body()
                if (tasks != null) {
                    val db = StrangerHelpApp.instance.database
                    // Clear and insert to ensure local cache is updated
                    // We can just use insertTasks which is a replace strategy
                    db.taskDao().insertTasks(tasks)
                }
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

package com.strangerhelp.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.strangerhelp.app.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>): List<Long>

    @Query("DELETE FROM tasks")
    suspend fun clearTasks(): Int
    
    @Query("DELETE FROM tasks WHERE _id = :id")
    suspend fun deleteTaskById(id: String): Int
}

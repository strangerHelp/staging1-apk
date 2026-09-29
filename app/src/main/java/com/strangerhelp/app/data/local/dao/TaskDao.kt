package com.strangerhelp.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.strangerhelp.app.data.model.Task
import com.strangerhelp.app.data.model.sanitized
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    abstract fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    abstract suspend fun getAllTasksList(): List<Task>

    @Query("""
        SELECT * FROM tasks 
        WHERE (:category IS NULL OR :category = '' OR :category = 'All' OR category = :category) 
        AND (:query IS NULL OR :query = '' OR title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
    """)
    abstract fun getFilteredTasks(category: String?, query: String?): Flow<List<Task>>

    @Query("""
        SELECT * FROM tasks 
        WHERE (:category IS NULL OR :category = '' OR :category = 'All' OR category = :category) 
        AND (:query IS NULL OR :query = '' OR title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
    """)
    abstract suspend fun getFilteredTasksList(category: String?, query: String?): List<Task>

    @Query("SELECT * FROM tasks WHERE _id = :id")
    abstract suspend fun getTaskById(id: String): Task?

    @Query("SELECT * FROM tasks WHERE _id = :id")
    abstract fun observeTaskById(id: String): Flow<Task?>

    @Query("SELECT * FROM tasks WHERE posterId = :posterId ORDER BY createdAt DESC")
    abstract fun getTasksByPoster(posterId: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE claimedBy = :userId ORDER BY createdAt DESC")
    abstract fun getTasksClaimedBy(userId: String): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertRawTasks(tasks: List<Task>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertRawTask(task: Task): Long

    @Transaction
    open suspend fun insertTasks(tasks: List<Task>): List<Long> {
        val safeTasks = tasks.map { it.sanitized() }
        return insertRawTasks(safeTasks)
    }

    open suspend fun insertTask(task: Task): Long {
        return insertRawTask(task.sanitized())
    }

    @Query("DELETE FROM tasks")
    abstract suspend fun clearTasks(): Int
    
    @Query("DELETE FROM tasks WHERE _id = :id")
    abstract suspend fun deleteTaskById(id: String): Int
}

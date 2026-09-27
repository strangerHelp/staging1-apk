package com.strangerhelp.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.strangerhelp.app.data.model.TaskDraft
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDraftDao {
    @Query("SELECT * FROM task_drafts WHERE id = 'active_draft' LIMIT 1")
    suspend fun getDraft(): TaskDraft?

    @Query("SELECT * FROM task_drafts WHERE id = 'active_draft' LIMIT 1")
    fun getDraftFlow(): Flow<TaskDraft?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: TaskDraft): Long

    @Query("DELETE FROM task_drafts WHERE id = 'active_draft'")
    suspend fun deleteDraft(): Int
}

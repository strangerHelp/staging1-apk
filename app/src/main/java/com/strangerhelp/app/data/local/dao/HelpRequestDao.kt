package com.strangerhelp.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.strangerhelp.app.data.model.HelpRequest
import kotlinx.coroutines.flow.Flow

@Dao
interface HelpRequestDao {
    @Query("SELECT * FROM help_requests")
    fun getAllHelpRequests(): Flow<List<HelpRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHelpRequests(requests: List<HelpRequest>): List<Long>

    @Query("DELETE FROM help_requests")
    suspend fun clearHelpRequests(): Int
}

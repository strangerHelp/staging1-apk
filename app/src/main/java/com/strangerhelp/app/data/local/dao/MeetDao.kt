package com.strangerhelp.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.strangerhelp.app.data.model.Meet
import kotlinx.coroutines.flow.Flow

@Dao
interface MeetDao {
    @Query("SELECT * FROM meets ORDER BY date ASC, time ASC")
    fun getAllMeets(): Flow<List<Meet>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeets(meets: List<Meet>) : List<Long>

    @Query("DELETE FROM meets")
    suspend fun clearMeets() : Int
}

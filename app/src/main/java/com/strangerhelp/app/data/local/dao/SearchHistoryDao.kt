package com.strangerhelp.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.strangerhelp.app.data.model.SearchHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 5")
    fun getRecentSearches(): Flow<List<SearchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(searchHistory: SearchHistory): Long

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteSearch(query: String): Int
    
    @Query("DELETE FROM search_history")
    suspend fun clearHistory(): Int
}

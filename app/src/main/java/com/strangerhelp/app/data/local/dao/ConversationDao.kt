package com.strangerhelp.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.strangerhelp.app.data.model.Conversation
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY lastMessageAt DESC")
    fun getAllConversations(): Flow<List<Conversation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<Conversation>) : List<Long>

    @Query("SELECT * FROM conversations WHERE taskId = :taskId LIMIT 1")
    suspend fun getConversationByTaskId(taskId: String): Conversation?

    @Query("SELECT * FROM conversations WHERE _id = :id LIMIT 1")
    suspend fun getConversationById(id: String): Conversation?

    @Query("SELECT * FROM conversations ORDER BY lastMessageAt DESC")
    suspend fun getAllConversationsList(): List<Conversation>

    @Query("DELETE FROM conversations")
    suspend fun clearConversations() : Int
}

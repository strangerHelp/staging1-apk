package com.strangerhelp.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.strangerhelp.app.data.model.Task
import com.strangerhelp.app.data.model.Conversation
import com.strangerhelp.app.data.model.Notification
import com.strangerhelp.app.data.model.Meet
import com.strangerhelp.app.data.model.HelpRequest
import com.strangerhelp.app.data.local.dao.TaskDao
import com.strangerhelp.app.data.local.dao.ConversationDao
import com.strangerhelp.app.data.local.dao.NotificationDao
import com.strangerhelp.app.data.local.dao.MeetDao
import com.strangerhelp.app.data.local.dao.HelpRequestDao
import com.strangerhelp.app.data.local.dao.SearchHistoryDao
import com.strangerhelp.app.data.model.SearchHistory

@Database(entities = [Task::class, Conversation::class, Notification::class, Meet::class, HelpRequest::class, SearchHistory::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun conversationDao(): ConversationDao
    abstract fun notificationDao(): NotificationDao
    abstract fun meetDao(): MeetDao
    abstract fun helpRequestDao(): HelpRequestDao
    abstract fun searchHistoryDao(): SearchHistoryDao
}

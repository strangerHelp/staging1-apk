package com.strangerhelp.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val avatar: String = "",
    val city: String = "",
    val area: String = "",
    val phone: String = "",
    val bio: String = "",
    val skills: String = "[]",
    val verified: Int = 0,
    val emailVerified: Boolean = false,
    val handle: String = "",
    val banned: Int = 0,
    val is_admin: Int = 0,
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val _id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val budget: Int = 0,
    val deadline: String = "Today",
    val location: String = "",
    val city: String = "",
    val lat: Double? = null,
    val lng: Double? = null,
    val anonymous: Int = 0,
    val urgent: Int = 0,
    val status: String = "open",
    val posterId: String = "",
    val posterName: String = "",
    val posterVerified: Boolean = false,
    val claimedBy: String? = null,
    val claimedByName: String? = null,
    val claimerVerified: Boolean = false,
    val maxClaimers: Int = 1,
    val distance: Double? = null,
    val attachments: List<String> = emptyList(),
    val completionProof: List<String> = emptyList(),
    val createdAt: String = "",
    val trackingActive: Boolean = false,
    val helperLat: Double? = null,
    val helperLng: Double? = null,
    val visibility: String = "public",
    val inviteCode: String? = null,
)

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey val _id: String = "",
    val taskId: String? = null,
    val participants: List<String> = emptyList(),
    val participantNames: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageAt: String = "",
)

data class Message(
    val _id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val attachments: List<String> = emptyList(),
    val type: String = "text",
    val createdAt: String = "",
)

data class Question(
    val _id: String = "",
    val text: String = "",
    val category: String = "",
    val location: String = "",
    val votes: Int = 0,
    val anonymous: Int = 0,
    val posterId: String = "",
    val createdAt: String = "",
)

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey val id: String = "",
    val user_id: String = "",
    val type: String = "",
    val title: String = "",
    val message: String = "",
    val link: String = "",
    val read: Int = 0,
    val created_at: String = ""
)

data class NotificationResponse(
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0
)

@Entity(tableName = "meets")
data class Meet(
    @PrimaryKey val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val location: String = "",
    val date: String = "",
    val time: String = "",
    val visibility: String = "public",
    val invite_code: String = "",
    val max_attendees: Int = 0,
    val host_id: String = "",
    val host_name: String = "",
    val voice_note: String = "",
    val anonymous: Int = 0,
    val attendeeCount: Int = 0
)

data class AuthResponse(val id: String = "", val name: String = "")

data class UserResponse(val user: User?)

data class GenericResponse(val ok: Boolean = false, val message: String = "")

data class ErrorResponse(val error: String = "")

@Entity(tableName = "help_requests")
data class HelpRequest(
    @PrimaryKey val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val status: String = "open"
)

@Entity(tableName = "search_history")
data class SearchHistory(
    @PrimaryKey val query: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class UserStats(val rating: Double = 0.0, val totalReviews: Int = 0, val tasksCompleted: Int = 0, val completionRate: Int = 0, val trustScore: Int = 0)
data class StatsResponse(val stats: UserStats)

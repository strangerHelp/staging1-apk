package com.strangerhelp.app.data.model
import com.google.gson.annotations.SerializedName

import androidx.room.Entity
import androidx.room.PrimaryKey


data class ClaimRequest(
    val id: String = "",
    @SerializedName("requester_id") val requesterId: String = "",
    @SerializedName("requester_name") val requesterName: String = "",
    val status: String = "pending",
    @SerializedName("offered_budget") val offeredBudget: Int? = null,
    val message: String? = null,
    @SerializedName("created_at") val createdAt: String = ""
)


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
    @SerializedName("email_verified") val emailVerified: Int = 0,
    val handle: String = "",
    val banned: Int = 0,
    val is_admin: Int = 0,
    val rating: Double = 0.0,
    val totalReviews: Int = 0,
    val trustScore: Int = 0
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey @SerializedName(value = "_id", alternate = ["id"]) val _id: String = "",
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
    val distance: Double? = null,
    val attachments: List<String> = emptyList(),
    val attachmentCount: Int = 0,
    val completionProof: List<String> = emptyList(),
    @com.google.gson.annotations.SerializedName("completion_status") val completionStatus: String = "",
    @com.google.gson.annotations.SerializedName("rejection_reason") val rejectionReason: String? = null,
    val createdAt: String = "",
    val trackingActive: Boolean = false,
    val helperLat: Double? = null,
    val helperLng: Double? = null,
    
    val visibility: String = "public",
    val inviteCode: String? = null,
    val claimRequests: List<ClaimRequest>? = emptyList(),
    val claimedUsers: List<ClaimedUser>? = emptyList(),
    val maxClaimers: Int = 1
)

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey @SerializedName(value = "_id", alternate = ["id"]) val _id: String = "",
    val taskId: String? = null,
    val participants: List<String> = emptyList(),
    val participantNames: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageAt: String = "",
)

data class Message(
    @SerializedName(value = "_id", alternate = ["id"]) val _id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val attachments: List<String> = emptyList(),
    val attachmentCount: Int = 0,
    val type: String = "text",
    val createdAt: String = "",
)

data class Question(
    @SerializedName(value = "_id", alternate = ["id"]) val _id: String = "",
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
    @PrimaryKey @SerializedName("_id") val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val location: String = "",
    val date: String = "",
    val time: String = "",
    val visibility: String = "public",
    @SerializedName("invite_code") val inviteCode: String = "",
    @SerializedName("max_attendees") val maxAttendees: Int = 0,
    @SerializedName("host_id") val hostId: String = "",
    @SerializedName("host_name") val hostName: String = "",
    @SerializedName("voice_note") val voiceNote: String? = null,
    val anonymous: Int = 0,
    val attendeeCount: Int = 0,
    val attendees: List<Attendee> = emptyList()
)

data class Attendee(
    @SerializedName("user_id") val userId: String = "",
    @SerializedName("user_name") val userName: String = "",
    @SerializedName("joined_at") val joinedAt: String = ""
)

data class MeetActionRequest(
    val action: String // "join" or "leave"
)

data class MeetActionResponse(
    val ok: Boolean
)


data class AuthResponse(val id: String = "", val name: String = "")

data class UserResponse(val user: User?)


@Entity(tableName = "help_requests")
data class HelpRequest(
    @PrimaryKey val id: String = "",
    val title: String = ""
)



@Entity(tableName = "search_history")
data class SearchHistory(
    @PrimaryKey val query: String = "",
    val timestamp: Long = 0
)

data class ClaimedUser(
    @SerializedName("user_id") val userId: String = "",
    val userName: String = "",
    val avatar: String = ""
)

data class GenericResponse(
    val success: Boolean = true,
    val message: String = ""
)

data class VerificationStatus(
    val status: String = "",
    val verified: Boolean = false
)

data class SupportMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val createdAt: String = "",
    val isSupport: Boolean = false
)

data class SupportResponse(
    val messages: List<SupportMessage> = emptyList()
)

data class UserStats(
    val status: String = "",
    val tasksCompleted: Int = 0,
    val rating: Double = 0.0,
    val totalReviews: Int = 0,
    val completionRate: Int = 0,
    val trustScore: Int = 0
)

data class Review(
    val id: String,
    @com.google.gson.annotations.SerializedName("task_id") val taskId: String,
    @com.google.gson.annotations.SerializedName("reviewer_id") val reviewerId: String,
    @com.google.gson.annotations.SerializedName("reviewer_name") val reviewerName: String,
    @com.google.gson.annotations.SerializedName("reviewee_id") val revieweeId: String,
    val rating: Int,                 // 1..5
    val comment: String,
    @com.google.gson.annotations.SerializedName("created_at") val createdAt: String
)

data class ReviewsResponse(
    val reviews: List<Review>,
    @com.google.gson.annotations.SerializedName("avgRating") val avgRating: Double,
    @com.google.gson.annotations.SerializedName("totalReviews") val totalReviews: Int
)

data class SubmitReviewRequest(
    @com.google.gson.annotations.SerializedName("taskId") val taskId: String,
    @com.google.gson.annotations.SerializedName("revieweeId") val revieweeId: String,
    val rating: Int,
    val comment: String
)

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
    @SerializedName("title") val title: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("category") val category: String = "",
    @SerializedName("budget") val budget: Int = 0,
    @SerializedName("deadline") val deadline: String = "Today",
    @SerializedName("location") val location: String = "",
    @SerializedName("city") val city: String = "",
    @SerializedName("lat") val lat: Double? = null,
    @SerializedName("lng") val lng: Double? = null,
    @SerializedName("anonymous") val anonymous: Int = 0,
    @SerializedName("urgent") val urgent: Int = 0,
    @SerializedName("status") val status: String = "open",
    @SerializedName(value = "posterId", alternate = ["poster_id"]) val posterId: String = "",
    @SerializedName(value = "posterName", alternate = ["poster_name", "posted_by", "postedBy"]) val posterName: String = "",
    @SerializedName(value = "posterVerified", alternate = ["poster_verified"]) val posterVerified: Boolean = false,
    @SerializedName(value = "claimedBy", alternate = ["claimed_by"]) val claimedBy: String? = null,
    @SerializedName(value = "claimedByName", alternate = ["claimed_by_name"]) val claimedByName: String? = null,
    @SerializedName(value = "claimerVerified", alternate = ["claimer_verified"]) val claimerVerified: Boolean = false,
    @SerializedName("distance") val distance: Double? = null,
    @SerializedName("attachments") val attachments: List<String> = emptyList(),
    @SerializedName(value = "attachmentCount", alternate = ["attachment_count"]) val attachmentCount: Int = 0,
    @SerializedName(value = "completionProof", alternate = ["completion_proof"]) val completionProof: List<String> = emptyList(),
    @SerializedName(value = "completionStatus", alternate = ["completion_status"]) val completionStatus: String = "",
    @SerializedName(value = "rejectionReason", alternate = ["rejection_reason"]) val rejectionReason: String? = null,
    @SerializedName(value = "createdAt", alternate = ["created_at"]) val createdAt: String = "",
    @SerializedName(value = "trackingActive", alternate = ["tracking_active"]) val trackingActive: Boolean = false,
    @SerializedName(value = "helperLat", alternate = ["helper_lat"]) val helperLat: Double? = null,
    @SerializedName(value = "helperLng", alternate = ["helper_lng"]) val helperLng: Double? = null,
    @SerializedName("visibility") val visibility: String = "public",
    @SerializedName(value = "inviteCode", alternate = ["invite_code"]) val inviteCode: String? = null,
    @SerializedName(value = "claimRequests", alternate = ["claim_requests"]) val claimRequests: List<ClaimRequest>? = emptyList(),
    @SerializedName(value = "claimedUsers", alternate = ["claimed_users"]) val claimedUsers: List<ClaimedUser>? = emptyList(),
    @SerializedName(value = "maxClaimers", alternate = ["max_claimers"]) val maxClaimers: Int = 1
)

fun Task.sanitized(): Task {
    val idVal = (this._id as? String?).orEmpty()
    val titleVal = (this.title as? String?).orEmpty()
    val descVal = (this.description as? String?).orEmpty()
    val catVal = (this.category as? String?).orEmpty()
    val deadlineVal = (this.deadline as? String?).takeIf { !it.isNullOrBlank() } ?: "Today"
    val locVal = (this.location as? String?).orEmpty()
    val cityVal = (this.city as? String?).orEmpty()
    val statusVal = (this.status as? String?).takeIf { !it.isNullOrBlank() } ?: "open"
    val pIdVal = (this.posterId as? String?).orEmpty()
    val pNameVal = (this.posterName as? String?).orEmpty()
    val compStatusVal = (this.completionStatus as? String?).orEmpty()
    val createdVal = (this.createdAt as? String?).orEmpty()
    val visVal = (this.visibility as? String?).takeIf { !it.isNullOrBlank() } ?: "public"
    val attachVal = (this.attachments as? List<String>?) ?: emptyList()
    val compProofVal = (this.completionProof as? List<String>?) ?: emptyList()
    val claimReqVal = (this.claimRequests as? List<ClaimRequest>?) ?: emptyList()
    val claimedUsrVal = (this.claimedUsers as? List<ClaimedUser>?) ?: emptyList()

    return this.copy(
        _id = if (idVal.isNotBlank()) idVal else java.util.UUID.randomUUID().toString(),
        title = titleVal,
        description = descVal,
        category = catVal,
        budget = if (this.budget < 0) 0 else this.budget,
        deadline = deadlineVal,
        location = locVal,
        city = cityVal,
        anonymous = this.anonymous,
        urgent = this.urgent,
        status = statusVal,
        posterId = pIdVal,
        posterName = pNameVal,
        posterVerified = this.posterVerified,
        claimedBy = this.claimedBy as? String?,
        claimedByName = this.claimedByName as? String?,
        claimerVerified = this.claimerVerified,
        distance = this.distance,
        attachments = attachVal,
        attachmentCount = if (this.attachmentCount > 0) this.attachmentCount else attachVal.size,
        completionProof = compProofVal,
        completionStatus = compStatusVal,
        rejectionReason = this.rejectionReason as? String?,
        createdAt = createdVal,
        trackingActive = this.trackingActive,
        helperLat = this.helperLat,
        helperLng = this.helperLng,
        visibility = visVal,
        inviteCode = this.inviteCode as? String?,
        claimRequests = claimReqVal,
        claimedUsers = claimedUsrVal,
        maxClaimers = if (this.maxClaimers <= 0) 1 else this.maxClaimers
    )
}

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
    @SerializedName(value = "_id", alternate = ["id"]) val id: String = "",
    val text: String = "",
    val category: String = "",
    val location: String? = null,
    val anonymous: Int = 0,
    @SerializedName("author_id") val authorId: String = "",
    @SerializedName("author_name") val authorName: String = "",
    val votes: Int = 0,
    @SerializedName("answerCount") val answerCount: Int = 0,
    @SerializedName("created_at") val createdAt: String = "",
    val answers: List<Answer> = emptyList()
)

data class Answer(
    val id: String = "",
    @SerializedName("question_id") val questionId: String = "",
    @SerializedName("author_id") val authorId: String = "",
    @SerializedName("author_name") val authorName: String = "",
    val text: String = "",
    val votes: Int = 0,
    @SerializedName("created_at") val createdAt: String = ""
)

data class QuestionRequest(
    val text: String,
    val category: String,
    val location: String?,
    val anonymous: Boolean
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

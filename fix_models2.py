import re

with open("app/src/main/java/com/strangerhelp/app/data/model/Models.kt", "r") as f:
    text = f.read()

# Replace the old Meet class with the new one, and add the other classes
old_meet = """@Entity(tableName = "meets")
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
)"""

new_meet = """@Entity(tableName = "meets")
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
"""

text = text.replace(old_meet, new_meet)

with open("app/src/main/java/com/strangerhelp/app/data/model/Models.kt", "w") as f:
    f.write(text)


with open("app/src/main/java/com/strangerhelp/app/data/model/Models.kt", "r") as f:
    text = f.read()

text = text.replace("""data class SupportMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val createdAt: String = ""
)""", """data class SupportMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val createdAt: String = "",
    val isSupport: Boolean = false
)""")

text = text.replace("""data class UserStats(
    val status: String = "",
    val tasksCompleted: Int = 0,
    val rating: Double = 0.0,
    val totalReviews: Int = 0,
    val completionRate: Double = 0.0,
    val trustScore: Double = 0.0
)""", """data class UserStats(
    val status: String = "",
    val tasksCompleted: Int = 0,
    val rating: Double = 0.0,
    val totalReviews: Int = 0,
    val completionRate: Int = 0,
    val trustScore: Int = 0
)""")

with open("app/src/main/java/com/strangerhelp/app/data/model/Models.kt", "w") as f:
    f.write(text)


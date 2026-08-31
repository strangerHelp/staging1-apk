with open("app/src/main/java/com/strangerhelp/app/data/model/Models.kt", "r") as f:
    text = f.read()

missing_classes = """
@Entity(tableName = "help_requests")
data class HelpRequest(
    @PrimaryKey val id: String = "",
    val title: String = ""
)

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey val id: String = "",
    val title: String = ""
)

data class NotificationResponse(
    val unread: Int = 0,
    val notifications: List<Notification> = emptyList()
)

@Entity(tableName = "search_history")
data class SearchHistory(
    @PrimaryKey val query: String = "",
    val timestamp: Long = 0
)
"""

with open("app/src/main/java/com/strangerhelp/app/data/model/Models.kt", "a") as f:
    f.write("\n" + missing_classes)

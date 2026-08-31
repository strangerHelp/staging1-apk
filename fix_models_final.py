import re

with open("app/src/main/java/com/strangerhelp/app/data/model/Models.kt", "r") as f:
    text = f.read()

# Remove the duplicated Notifications from the bottom:
text = text.replace("""@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey val id: String = "",
    val title: String = ""
)

data class NotificationResponse(
    val unread: Int = 0,
    val notifications: List<Notification> = emptyList()
)""", "")

text = text.replace("data class Notification(", "data class Notification_old(")
# Wait, actually I just need to add ClaimedUser!
# Did I overwrite the original Notification? No, `data class Notification` was earlier in the file!

with open("app/src/main/java/com/strangerhelp/app/data/model/Models.kt", "w") as f:
    f.write(text)


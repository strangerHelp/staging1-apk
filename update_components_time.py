import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatComponents.kt", "r") as f:
    content = f.read()

content = content.replace(
    'text = com.strangerhelp.app.utils.TimeUtils.formatTime(message.createdAt),',
    'text = com.strangerhelp.app.utils.TimeUtils.formatMessageTime(message.createdAt),'
)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatComponents.kt", "w") as f:
    f.write(content)

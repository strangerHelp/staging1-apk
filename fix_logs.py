import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatListScreen.kt", "r") as f:
    content = f.read()

pattern = r'onClick = \{\s*navController\.navigate\("chat/\$\{conversation\._id\}"\)\s*\}'
replacement = """onClick = {
                            android.util.Log.d("ChatList", "Clicked conversation ID: ${conversation._id}")
                            if (conversation._id.isNotEmpty()) {
                                navController.navigate("chat/${conversation._id}")
                            }
                        }"""

content = re.sub(pattern, replacement, content)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatListScreen.kt", "w") as f:
    f.write(content)

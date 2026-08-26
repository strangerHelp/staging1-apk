with open("app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatDetailScreen.kt", "r") as f:
    content = f.read()

content = content.replace('LaunchedEffect(conversationId) {\\n        android.util.Log.d("ChatDetail", "Received conversationId: $conversationId")', 'LaunchedEffect(conversationId) {\n        android.util.Log.d("ChatDetail", "Received conversationId: $conversationId")')

with open("app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatDetailScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatListScreen.kt", "r") as f:
    content2 = f.read()

content2 = content2.replace('onClick = {\\n                            android.util.Log.d("ChatList", "Clicked conversation ID: ${conversation._id}")\\n                            if (conversation._id.isNotEmpty()) {\\n                                navController.navigate("chat/${conversation._id}")\\n                            }\\n                        }', 'onClick = {\n                            android.util.Log.d("ChatList", "Clicked conversation ID: ${conversation._id}")\n                            if (conversation._id.isNotEmpty()) {\n                                navController.navigate("chat/${conversation._id}")\n                            }\n                        }')

with open("app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatListScreen.kt", "w") as f:
    f.write(content2)

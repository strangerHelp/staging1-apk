with open("app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatDetailScreen.kt", "r") as f:
    content = f.read()

target = """    val otherParticipantName = remember {
        val conv = conversations.find { it._id == conversationId }
        val userId = currentUser?.id ?: ""
        conv?.participantNames
            ?.filterIndexed { index, _ ->
                conv.participants.getOrNull(index) != userId
            }
            ?.firstOrNull() ?: "User"
    }"""

replacement = """    val conversations by viewModel.conversations.collectAsState()
    val otherParticipantName = remember(conversations, conversationId, currentUser) {
        val conv = conversations.find { it._id == conversationId }
        val userId = currentUser?.id ?: ""
        conv?.participantNames
            ?.filterIndexed { index, _ ->
                conv.participants.getOrNull(index) != userId
            }
            ?.firstOrNull() ?: "User"
    }"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatDetailScreen.kt", "w") as f:
    f.write(content)

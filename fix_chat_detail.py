import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatDetailScreen.kt", "r") as f:
    content = f.read()

# Make otherParticipantName reactive and depend on conversations
content = re.sub(
    r'val otherParticipantName = remember \{.*?\}\s*\?\: "User"\s*\}',
    '''val conversations by viewModel.conversations.collectAsState()
    val otherParticipantName = remember(conversations, conversationId, currentUser) {
        val conv = conversations.find { it._id == conversationId }
        val userId = currentUser?.id ?: ""
        conv?.participantNames
            ?.filterIndexed { index, _ ->
                conv.participants.getOrNull(index) != userId
            }
            ?.firstOrNull() ?: "User"
    }''',
    content,
    flags=re.DOTALL
)

# Update LaunchedEffect to also load conversations
content = content.replace(
    'viewModel.startPolling(conversationId)',
    'viewModel.startPolling(conversationId)\n            viewModel.loadConversations()'
)

# Replace the actions conv with the state variable
content = content.replace(
    'val conv = viewModel.conversations.value.find { it._id == conversationId }',
    'val conv = conversations.find { it._id == conversationId }'
)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatDetailScreen.kt", "w") as f:
    f.write(content)

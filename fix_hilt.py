import re
import os

def fix_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # ChatRepository
    if "ChatRepository.kt" in filepath:
        content = content.replace("import javax.inject.Inject\n", "")
        content = content.replace("class ChatRepository @Inject constructor", "class ChatRepository")
    
    # ChatViewModel
    if "ChatViewModel.kt" in filepath:
        content = content.replace("import dagger.hilt.android.lifecycle.HiltViewModel\n", "")
        content = content.replace("import javax.inject.Inject\n", "")
        content = content.replace("@HiltViewModel\n", "")
        content = content.replace("class ChatViewModel @Inject constructor(\n    private val chatRepository: ChatRepository,\n    private val authRepository: AuthRepository\n)", "class ChatViewModel(\n    private val chatRepository: ChatRepository = ChatRepository(com.strangerhelp.app.data.api.ApiClient.api),\n    private val authRepository: AuthRepository = com.strangerhelp.app.data.repository.AuthRepository(com.strangerhelp.app.data.api.ApiClient.api)\n)")
        content = content.replace("_currentUser.value = response.body()", "_currentUser.value = response.body()?.user")
        
    # ChatListScreen
    if "ChatListScreen.kt" in filepath:
        content = content.replace("import androidx.hilt.navigation.compose.hiltViewModel\n", "import androidx.lifecycle.viewmodel.compose.viewModel\n")
        content = content.replace("viewModel: ChatViewModel = hiltViewModel(),", "viewModel: ChatViewModel = viewModel(),")
        content = content.replace("getTimeAgo(conversation.lastMessageAt)", "com.strangerhelp.app.utils.TimeUtils.getTimeAgo(conversation.lastMessageAt)")
        
    # ChatDetailScreen
    if "ChatDetailScreen.kt" in filepath:
        content = content.replace("import androidx.hilt.navigation.compose.hiltViewModel\n", "import androidx.lifecycle.viewmodel.compose.viewModel\n")
        content = content.replace("viewModel: ChatViewModel = hiltViewModel(),", "viewModel: ChatViewModel = viewModel(),")

    # ChatComponents
    if "ChatComponents.kt" in filepath:
        content = content.replace("OutlinedButtonDefaults.outlinedButtonColors", "ButtonDefaults.outlinedButtonColors")
        content = content.replace("formatTime(message.createdAt)", "com.strangerhelp.app.utils.TimeUtils.formatTime(message.createdAt)")

    with open(filepath, 'w') as f:
        f.write(content)

fix_file("app/src/main/java/com/strangerhelp/app/data/repository/ChatRepository.kt")
fix_file("app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatViewModel.kt")
fix_file("app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatListScreen.kt")
fix_file("app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatDetailScreen.kt")
fix_file("app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatComponents.kt")


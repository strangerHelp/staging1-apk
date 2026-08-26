import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatDetailScreen.kt", "r") as f:
    content = f.read()

# Add isSending
content = content.replace('val error by viewModel.error.collectAsState()', 'val error by viewModel.error.collectAsState()\n    val isSending by viewModel.isSending.collectAsState()')

# Add SnackbarHostState
content = content.replace('val listState = rememberLazyListState()', 'val listState = rememberLazyListState()\n    val snackbarHostState = remember { SnackbarHostState() }')

# Scaffold updates
content = content.replace('Scaffold(\n        topBar', 'Scaffold(\n        snackbarHost = { SnackbarHost(snackbarHostState) },\n        topBar')

# Update ChatInputBar
input_bar = """            ChatInputBar(
                text = inputText,
                onTextChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(conversationId, inputText)
                        inputText = ""
                    }
                },
                onAttach = { imageFile ->
                    viewModel.sendMessageWithImage(conversationId, inputText, imageFile)
                    inputText = ""
                },
                isSending = isSending,
                maxLength = 5000
            )"""
content = re.sub(r'            ChatInputBar\(\s*text = inputText,.*?inputText = ""\s*\}\s*\)', input_bar, content, flags=re.DOTALL)

# Update error handling
error_block = """            if (error != null) {
                LaunchedEffect(error) {
                    error?.let { snackbarHostState.showSnackbar(it) }
                    viewModel.clearError()
                }
            }"""
content = re.sub(r'            if \(error != null\) \{\s*LaunchedEffect\(error\) \{\s*viewModel\.clearError\(\)\s*\}\s*\}', error_block, content, flags=re.DOTALL)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatDetailScreen.kt", "w") as f:
    f.write(content)

import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatViewModel.kt", "r") as f:
    content = f.read()

# Add isSending
is_sending_decl = """    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()"""
content = re.sub(r'    private val _isLoading = MutableStateFlow\(false\)\n    val isLoading: StateFlow<Boolean> = _isLoading\.asStateFlow\(\)', is_sending_decl, content)

# Update sendMessage
send_msg = """    fun sendMessage(conversationId: String, text: String) {
        if (text.isBlank() || text.length > 5000) return
        val currentUser = _currentUser.value
        if (currentUser == null) {
            _error.value = "Please login to send messages"
            return
        }
        viewModelScope.launch {
            _isSending.value = true
            val optimisticMessage = Message(
                _id = "temp_${System.currentTimeMillis()}",
                conversationId = conversationId,
                senderId = currentUser.id,
                senderName = currentUser.name ?: "You",
                text = text.trim(),
                attachments = emptyList(),
                type = "text",
                createdAt = formatCurrentTimestamp()
            )
            _messages.value = _messages.value + optimisticMessage
            try {
                val response = chatRepository.sendMessage(conversationId, text.trim())
                if (response.isSuccessful) {
                    loadMessages(conversationId)
                } else {
                    _messages.value = _messages.value.filter { it._id != optimisticMessage._id }
                    val code = response.code()
                    if (code == 429) _error.value = "Sending too fast. Wait a moment."
                    else if (code == 400) _error.value = "Message too long (max 5000 chars)"
                    else if (code == 404) _error.value = "Conversation not found"
                    else _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _messages.value = _messages.value.filter { it._id != optimisticMessage._id }
                _error.value = "Network error. Please check your connection."
            } finally {
                _isSending.value = false
            }
        }
    }"""
content = re.sub(r'    fun sendMessage\(conversationId: String, text: String\) \{.*?(?=    fun sendMessageWithImage)', send_msg, content, flags=re.DOTALL)

# Update sendMessageWithImage
send_img = """    fun sendMessageWithImage(conversationId: String, text: String, imageFile: File) {
        viewModelScope.launch {
            _isSending.value = true
            _error.value = null
            try {
                val response = chatRepository.sendMessageWithImage(
                    conversationId = conversationId,
                    text = text,
                    imageFile = imageFile
                )
                if (response.isSuccessful) {
                    loadMessages(conversationId)
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to send image"
            } finally {
                _isSending.value = false
            }
        }
    }"""
content = re.sub(r'    fun sendMessageWithImage\(conversationId: String, text: String, imageFile: File\) \{.*?(?=    fun createConversation)', send_img, content, flags=re.DOTALL)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatViewModel.kt", "w") as f:
    f.write(content)

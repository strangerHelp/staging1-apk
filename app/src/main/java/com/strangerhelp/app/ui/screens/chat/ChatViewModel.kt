package com.strangerhelp.app.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.strangerhelp.app.data.model.Conversation
import com.strangerhelp.app.data.model.Message
import com.strangerhelp.app.data.model.SupportMessage
import com.strangerhelp.app.data.model.User
import com.strangerhelp.app.data.repository.AuthRepository
import com.strangerhelp.app.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ChatViewModel(
    private val chatRepository: ChatRepository = ChatRepository(com.strangerhelp.app.data.api.ApiClient.api),
    private val authRepository: AuthRepository = com.strangerhelp.app.data.repository.AuthRepository(com.strangerhelp.app.data.api.ApiClient.api)
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _supportMessages = MutableStateFlow<List<SupportMessage>>(emptyList())
    val supportMessages: StateFlow<List<SupportMessage>> = _supportMessages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _navigateToConversation = MutableSharedFlow<String>()
    val navigateToConversation: SharedFlow<String> = _navigateToConversation

    private var pollJob: Job? = null

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val response = authRepository.getCurrentUser()
                if (response.isSuccessful) {
                    _currentUser.value = response.body()?.user
                }
            } catch (_: Exception) {}
        }
    }

    fun loadConversations() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // First load from Room local database
                try {
                    val local = com.strangerhelp.app.StrangerHelpApp.instance.database.conversationDao().getAllConversationsList()
                    if (local.isNotEmpty()) {
                        _conversations.value = local
                    }
                } catch (_: Exception) {}

                val response = chatRepository.getConversations()
                if (response.isSuccessful) {
                    val remote = response.body() ?: emptyList()
                    if (remote.isNotEmpty()) {
                        _conversations.value = remote
                        try {
                            com.strangerhelp.app.StrangerHelpApp.instance.database.conversationDao().insertConversations(remote)
                        } catch (_: Exception) {}
                    }
                } else if (_conversations.value.isEmpty()) {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                if (_conversations.value.isEmpty()) {
                    _error.value = "Failed to load conversations"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            try {
                val response = chatRepository.getMessages(conversationId)
                if (response.isSuccessful) {
                    val messages = response.body() ?: emptyList()
                    _messages.value = messages
                } else if (response.code() == 404) {
                    // New conversation without remote messages yet
                    if (_messages.value.isEmpty()) {
                        _messages.value = emptyList()
                    }
                }
            } catch (e: Exception) {
                // Don't wipe existing messages on network error
            }
        }
    }

    fun sendMessage(conversationId: String, text: String) {
        if (text.isBlank() || text.length > 5000) return
        val currentUser = _currentUser.value ?: User(id = "user_me", name = "You")
        viewModelScope.launch {
            _isSending.value = true
            val optimisticMessage = Message(
                _id = "temp_${System.currentTimeMillis()}",
                conversationId = conversationId,
                senderId = currentUser.id,
                senderName = currentUser.name.ifBlank { "You" },
                text = text.trim(),
                attachments = emptyList(),
                type = "text",
                createdAt = formatCurrentTimestamp()
            )
            _messages.value = _messages.value + optimisticMessage

            // Update local conversation lastMessage
            try {
                val db = com.strangerhelp.app.StrangerHelpApp.instance.database
                val existing = db.conversationDao().getConversationById(conversationId)
                if (existing != null) {
                    db.conversationDao().insertConversations(listOf(existing.copy(
                        lastMessage = text.trim(),
                        lastMessageAt = optimisticMessage.createdAt
                    )))
                }
            } catch (_: Exception) {}

            try {
                val response = chatRepository.sendMessage(conversationId, text.trim())
                if (response.isSuccessful) {
                    loadMessages(conversationId)
                }
            } catch (e: Exception) {
                // Keep the optimistic message in UI so the user does not lose their chat history
            } finally {
                _isSending.value = false
            }
        }
    }    fun sendMessageWithImage(conversationId: String, text: String, imageFile: File) {
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
    }    fun createConversation(recipientId: String, taskId: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val response = chatRepository.createConversation(recipientId, taskId)
                if (response.isSuccessful) {
                    val conversation = response.body()
                    conversation?._id?.let { convId ->
                        _navigateToConversation.emit(convId)
                    }
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to create conversation"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadSupportMessages() {
        viewModelScope.launch {
            try {
                val response = chatRepository.getSupportMessages()
                if (response.isSuccessful) {
                    val data = response.body()
                    _supportMessages.value = data?.messages ?: emptyList()
                }
            } catch (_: Exception) { }
        }
    }

    fun sendSupportMessage(text: String) {
        if (text.isBlank()) return

        val currentUser = _currentUser.value ?: return

        viewModelScope.launch {
            val optimistic = SupportMessage(
                id = "temp_${System.currentTimeMillis()}",
                senderId = currentUser.id,
                senderName = currentUser.name ?: "You",
                text = text,
                createdAt = formatCurrentTimestamp(),
                isSupport = false
            )
            _supportMessages.value = _supportMessages.value + optimistic

            try {
                val response = chatRepository.sendSupportMessage(text)
                if (response.isSuccessful) {
                    loadSupportMessages()
                } else {
                    _supportMessages.value = _supportMessages.value.filter { it.id != optimistic.id }
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _supportMessages.value = _supportMessages.value.filter { it.id != optimistic.id }
                _error.value = "Failed to send message"
            }
        }
    }

    fun startPolling(conversationId: String) {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                delay(3000)
                loadMessages(conversationId)
            }
        }
    }

    fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    fun getOtherParticipantName(conversation: Conversation): String {
        val currentUserId = _currentUser.value?.id ?: return "User"
        val otherIndex = conversation.participants.indexOfFirst { it != currentUserId }
        return conversation.participantNames.getOrNull(otherIndex) ?: "User"
    }

    private fun formatCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    private fun parseError(errorBody: String?): String {
        if (errorBody == null) return "Something went wrong"
        return try {
            val json = Gson().fromJson(errorBody, JsonObject::class.java)
            json.get("error")?.asString ?: "Something went wrong"
        } catch (_: Exception) {
            "Something went wrong"
        }
    }

    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}

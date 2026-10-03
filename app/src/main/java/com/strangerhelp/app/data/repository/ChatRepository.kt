package com.strangerhelp.app.data.repository

import com.strangerhelp.app.data.api.StrangerHelpApi
import com.strangerhelp.app.data.model.Conversation
import com.strangerhelp.app.data.model.Message
import com.strangerhelp.app.data.model.SupportResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

class ChatRepository(
    private val api: StrangerHelpApi
) {
    suspend fun getConversations(): Response<List<Conversation>> {
        return api.getConversations()
    }

    suspend fun getConversation(conversationId: String): Response<Conversation> {
        return api.getConversation(conversationId)
    }

    suspend fun getMessages(conversationId: String): Response<List<Message>> {
        return api.getMessages(conversationId)
    }

    suspend fun sendMessage(conversationId: String, text: String): Response<Message> {
        return api.sendMessage(conversationId, mapOf("text" to text))
    }

    suspend fun sendMessageWithImage(
        conversationId: String,
        text: String,
        imageFile: File
    ): Response<Message> {
        val textBody = text.toRequestBody("text/plain".toMediaTypeOrNull())
        val reqFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("files", imageFile.name, reqFile)
        
        return api.sendMessageMultipart(conversationId, textBody, body)
    }

    suspend fun createConversation(
        recipientId: String,
        taskId: String?
    ): Response<Conversation> {
        val body = mutableMapOf<String, String>("recipientId" to recipientId)
        taskId?.let { body["taskId"] = it }
        return api.createConversation(body)
    }

    suspend fun getSupportMessages(): Response<SupportResponse> {
        return api.getSupportMessages()
    }

    suspend fun sendSupportMessage(text: String): Response<SupportResponse> {
        return api.sendSupportMessage(mapOf("text" to text))
    }
}

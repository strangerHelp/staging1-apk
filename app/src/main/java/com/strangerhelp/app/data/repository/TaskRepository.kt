package com.strangerhelp.app.data.repository

import com.strangerhelp.app.data.api.StrangerHelpApi
import com.strangerhelp.app.data.model.Task
import com.strangerhelp.app.data.model.ClaimTaskRequest
import com.strangerhelp.app.data.model.ClaimResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

class TaskRepository(private val api: StrangerHelpApi) {
    suspend fun claimTask(taskId: String, offeredBudget: Int?, message: String?): Response<ClaimResponse> {
        val request = ClaimTaskRequest(action = "claim", offeredBudget = offeredBudget, message = message)
        return api.claimTask(taskId, request)
    }
    
    suspend fun getTask(taskId: String): Response<Task> {
        return api.getTask(taskId)
    }
    
    suspend fun createConversation(body: Map<String, String>) = api.createConversation(body)
    
    suspend fun patchTask(taskId: String, body: Map<String, Any?>): Response<Map<String, Any>> {
        val safeBody = body.filterValues { it != null }.mapValues { it.value as Any }
        return api.updateTracking(taskId, safeBody)
    }
    
    suspend fun submitProof(taskId: String, proofFiles: List<File>): Response<Map<String, Any>> {
        val action = "complete".toRequestBody("text/plain".toMediaTypeOrNull())
        val firstFile = proofFiles.firstOrNull()
        val part = firstFile?.let {
            val reqBody = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("proof", it.name, reqBody)
        }
        return api.completeTask(taskId, action, part)
    }
    
    suspend fun deleteTask(taskId: String): Response<Map<String, Any>> {
        return api.deleteTask(taskId)
    }
}

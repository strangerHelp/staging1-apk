package com.strangerhelp.app.data.repository

import com.strangerhelp.app.data.api.StrangerHelpApi
import com.strangerhelp.app.data.model.GenericResponse
import com.strangerhelp.app.data.model.NotificationResponse
import retrofit2.Response

class NotificationRepository(private val api: StrangerHelpApi) {
    suspend fun getNotifications(): Response<NotificationResponse> {
        return api.getNotifications()
    }
    
    suspend fun markAsRead(id: String): Response<GenericResponse> {
        return api.markAsRead(id)
    }
    
    suspend fun markAllAsRead(): Response<GenericResponse> {
        return api.markAllAsRead()
    }
}

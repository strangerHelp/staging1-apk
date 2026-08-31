package com.strangerhelp.app.data.repository

import com.strangerhelp.app.data.api.StrangerHelpApi
import com.strangerhelp.app.data.model.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class MeetRepository(private val api: StrangerHelpApi) {
    suspend fun getMeets(): Response<List<Meet>> = api.getMeets()
    suspend fun getMeet(id: String): Response<Meet> = api.getMeet(id)
    suspend fun getMeetByCode(code: String): Response<Meet> = api.getMeetByCode(code)

    suspend fun createMeet(
        title: String, description: String?, category: String, location: String?,
        date: String, time: String, visibility: String, maxAttendees: Int,
        anonymous: Boolean, voiceNoteBytes: ByteArray?
    ): Response<Meet> {
        val titleRb = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val descRb = description?.toRequestBody("text/plain".toMediaTypeOrNull())
        val categoryRb = category.toRequestBody("text/plain".toMediaTypeOrNull())
        val locationRb = location?.toRequestBody("text/plain".toMediaTypeOrNull())
        val dateRb = date.toRequestBody("text/plain".toMediaTypeOrNull())
        val timeRb = time.toRequestBody("text/plain".toMediaTypeOrNull())
        val visRb = visibility.toRequestBody("text/plain".toMediaTypeOrNull())
        val maxRb = maxAttendees.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val anonRb = anonymous.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val voicePart = voiceNoteBytes?.let {
            val reqFile = it.toRequestBody("audio/mp4".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("voice_note", "meet_audio.mp4", reqFile)
        }

        return api.createMeet(titleRb, descRb, categoryRb, locationRb, dateRb, timeRb, visRb, maxRb, anonRb, voicePart)
    }

    suspend fun joinMeet(id: String): Response<MeetActionResponse> = api.performMeetAction(id, MeetActionRequest("join"))
    suspend fun leaveMeet(id: String): Response<MeetActionResponse> = api.performMeetAction(id, MeetActionRequest("leave"))
    suspend fun deleteMeet(id: String): Response<GenericResponse> = api.deleteMeet(id)
}

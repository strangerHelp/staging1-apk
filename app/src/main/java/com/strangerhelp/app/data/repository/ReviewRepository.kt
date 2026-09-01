package com.strangerhelp.app.data.repository

import com.google.gson.JsonObject
import com.strangerhelp.app.data.api.StrangerHelpApi
import com.strangerhelp.app.data.model.ReviewsResponse
import com.strangerhelp.app.data.model.SubmitReviewRequest
import retrofit2.Response

class ReviewRepository(
    private val api: StrangerHelpApi
) {
    suspend fun submitReview(request: SubmitReviewRequest): Response<JsonObject> {
        return api.submitReview(request)
    }

    suspend fun getReviewsByUser(userId: String): Response<ReviewsResponse> {
        return api.getReviewsByUser(userId)
    }

    suspend fun getReviewsByTask(taskId: String): Response<ReviewsResponse> {
        return api.getReviewsByTask(taskId)
    }
}

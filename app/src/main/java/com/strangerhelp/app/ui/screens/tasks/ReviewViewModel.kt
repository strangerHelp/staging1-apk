package com.strangerhelp.app.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.strangerhelp.app.data.model.Review
import com.strangerhelp.app.data.model.ReviewsResponse
import com.strangerhelp.app.data.model.SubmitReviewRequest
import com.strangerhelp.app.data.model.Task
import com.strangerhelp.app.data.model.User
import com.strangerhelp.app.data.repository.AuthRepository
import com.strangerhelp.app.data.repository.ReviewRepository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class ReviewViewModel(
    private val reviewRepository: ReviewRepository = ReviewRepository(com.strangerhelp.app.data.api.ApiClient.api),
    private val authRepository: AuthRepository = AuthRepository(com.strangerhelp.app.data.api.ApiClient.api)
) : ViewModel() {

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _submittedReview = MutableStateFlow<Review?>(null)
    val submittedReview: StateFlow<Review?> = _submittedReview.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _taskReviews = MutableStateFlow<ReviewsResponse?>(null)
    val taskReviews: StateFlow<ReviewsResponse?> = _taskReviews.asStateFlow()

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

    fun submitReview(
        taskId: String,
        revieweeId: String,
        rating: Int,
        comment: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _error.value = null

            try {
                val request = SubmitReviewRequest(
                    taskId = taskId,
                    revieweeId = revieweeId,
                    rating = rating,
                    comment = comment
                )

                val response = reviewRepository.submitReview(request)

                if (response.isSuccessful) {
                    val currentUser = _currentUser.value
                    val optimisticReview = Review(
                        id = "temp_${System.currentTimeMillis()}",
                        taskId = taskId,
                        reviewerId = currentUser?.id ?: "",
                        reviewerName = currentUser?.name ?: "You",
                        revieweeId = revieweeId,
                        rating = rating,
                        comment = comment,
                        createdAt = formatCurrentTimestamp()
                    )
                    _submittedReview.value = optimisticReview
                    onSuccess()
                    loadTaskReviews(taskId)
                } else {
                    val errorBody = response.errorBody()?.string()
                    _error.value = parseError(errorBody, response.code())
                }
            } catch (e: Exception) {
                _error.value = "Network error. Please try again."
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun loadTaskReviews(taskId: String) {
        viewModelScope.launch {
            try {
                val response = reviewRepository.getReviewsByTask(taskId)
                if (response.isSuccessful) {
                    _taskReviews.value = response.body()
                }
            } catch (_: Exception) { }
        }
    }

    suspend fun hasUserReviewed(taskId: String): Boolean {
        return try {
            val response = reviewRepository.getReviewsByTask(taskId)
            if (response.isSuccessful) {
                val currentUserId = _currentUser.value?.id ?: return false
                response.body()?.reviews?.any { it.reviewerId == currentUserId } == true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun getRevieweeForTask(task: Task, me: User): Pair<String, String> {
        return if (me.id == task.posterId) {
            (task.claimedBy ?: "") to (task.claimedByName ?: "Helper")
        } else {
            task.posterId to task.posterName
        }
    }

    private fun parseError(errorBody: String?, code: Int): String {
        if (errorBody == null) return "Something went wrong"
        return try {
            val json = Gson().fromJson(errorBody, JsonObject::class.java)
            json.get("error")?.asString ?: "Something went wrong"
        } catch (_: Exception) {
            when (code) {
                409 -> "You already reviewed this task"
                403 -> "You can't review this task"
                400 -> "Task must be completed first"
                else -> "Failed to submit review"
            }
        }
    }

    private fun formatCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSubmittedReview() {
        _submittedReview.value = null
    }
}

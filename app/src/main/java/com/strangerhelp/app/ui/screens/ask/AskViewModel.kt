package com.strangerhelp.app.ui.screens.ask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.strangerhelp.app.data.model.Question
import com.strangerhelp.app.data.model.QuestionRequest
import com.strangerhelp.app.data.model.User
import com.strangerhelp.app.data.repository.AskRepository
import com.strangerhelp.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AskViewModel (
    private val askRepository: AskRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val _selectedQuestion = MutableStateFlow<Question?>(null)
    val selectedQuestion: StateFlow<Question?> = _selectedQuestion.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

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
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun loadQuestions(category: String = "All") {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val response = askRepository.getQuestions(category)
                if (response.isSuccessful) {
                    _questions.value = response.body() ?: emptyList()
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to load questions"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadQuestion(questionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val response = askRepository.getQuestion(questionId)
                if (response.isSuccessful) {
                    _selectedQuestion.value = response.body()
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to load question"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun postQuestion(
        text: String,
        category: String,
        location: String?,
        anonymous: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _error.value = null
            _successMessage.value = null

            try {
                val request = QuestionRequest(
                    text = text,
                    category = category,
                    location = location.takeIf { !it.isNullOrBlank() },
                    anonymous = anonymous
                )
                val response = askRepository.postQuestion(request)

                if (response.isSuccessful) {
                    _successMessage.value = "Question posted successfully!"
                    onSuccess()
                    loadQuestions(_selectedCategory.value)
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to post question"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun postAnswer(questionId: String, text: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _error.value = null

            try {
                val response = askRepository.postAnswer(questionId, text)

                if (response.isSuccessful) {
                    _successMessage.value = "Answer posted!"
                    loadQuestion(questionId)
                    onSuccess()
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to post answer"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun voteAnswer(questionId: String, vote: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = askRepository.voteAnswer(questionId, vote)

                if (response.isSuccessful) {
                    loadQuestion(questionId)
                    onSuccess()
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to vote"
            }
        }
    }

    fun deleteQuestion(questionId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _error.value = null

            try {
                val response = askRepository.deleteQuestion(questionId)

                if (response.isSuccessful) {
                    _successMessage.value = "Question deleted"
                    onSuccess()
                    loadQuestions(_selectedCategory.value)
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete question"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        loadQuestions(category)
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

    fun clearSuccess() {
        _successMessage.value = null
    }
}


class AskViewModelFactory(
    private val askRepository: AskRepository,
    private val authRepository: AuthRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AskViewModel(askRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package com.strangerhelp.app.ui.screens.meets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.strangerhelp.app.data.model.Meet
import com.strangerhelp.app.data.model.User
import com.strangerhelp.app.data.repository.AuthRepository
import com.strangerhelp.app.data.repository.MeetRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MeetViewModel(
    private val meetRepository: MeetRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _meets = MutableStateFlow<List<Meet>>(emptyList())
    val meets: StateFlow<List<Meet>> = _meets.asStateFlow()

    private val _selectedMeet = MutableStateFlow<Meet?>(null)
    val selectedMeet: StateFlow<Meet?> = _selectedMeet.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _navigateToMeet = MutableSharedFlow<String>()
    val navigateToMeet: SharedFlow<String> = _navigateToMeet

    init {
        viewModelScope.launch {
            
            val response = authRepository.getCurrentUser()
            if (response.isSuccessful) {
                _currentUser.value = response.body()?.user
            }

        }
    }

    fun loadMeets() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = meetRepository.getMeets()
                if (response.isSuccessful) {
                    _meets.value = response.body() ?: emptyList()
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to load meets"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMeet(meetId: String, inviteCode: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = if (inviteCode != null) {
                    meetRepository.getMeetByCode(inviteCode)
                } else {
                    meetRepository.getMeet(meetId)
                }
                if (response.isSuccessful) {
                    _selectedMeet.value = response.body()
                } else if (response.code() == 403) {
                    _error.value = "Private meet. Use invite link to access."
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to load meet"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createMeet(
        title: String,
        description: String?,
        category: String,
        location: String?,
        date: String,
        time: String,
        visibility: String,
        maxAttendees: Int,
        anonymous: Boolean,
        voiceNote: ByteArray?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _successMessage.value = null
            try {
                val response = meetRepository.createMeet(
                    title, description, category, location, date, time,
                    visibility, maxAttendees, anonymous, voiceNote
                )
                if (response.isSuccessful) {
                    val meet = response.body()
                    _successMessage.value = "Meet created successfully!"
                    if (meet?.visibility == "private") {
                        _navigateToMeet.emit(meet.id)
                    } else {
                        loadMeets()
                    }
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to create meet"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun joinMeet(meetId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = meetRepository.joinMeet(meetId)
                if (response.isSuccessful) {
                    _successMessage.value = "You joined the meet!"
                    loadMeet(meetId)
                    loadMeets()
                } else {
                    when (response.code()) {
                        409 -> _error.value = "Already joined"
                        400 -> _error.value = "Meet is full"
                        401 -> _error.value = "Please login to join"
                        else -> _error.value = parseError(response.errorBody()?.string())
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to join meet"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun leaveMeet(meetId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = meetRepository.leaveMeet(meetId)
                if (response.isSuccessful) {
                    _successMessage.value = "You left the meet."
                    loadMeet(meetId)
                    loadMeets()
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to leave meet"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteMeet(meetId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = meetRepository.deleteMeet(meetId)
                if (response.isSuccessful) {
                    _successMessage.value = "Meet deleted."
                    loadMeets()
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete meet"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun joinByCode(code: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = meetRepository.getMeetByCode(code)
                if (response.isSuccessful) {
                    onResult(response.body()?.id)
                } else {
                    _error.value = "Invalid invite code"
                    onResult(null)
                }
            } catch (e: Exception) {
                _error.value = "Failed to check invite code"
                onResult(null)
            }
        }
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

    fun clearError() { _error.value = null }
    fun clearSuccess() { _successMessage.value = null }
}

class MeetViewModelFactory(
    private val meetRepository: MeetRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MeetViewModel(meetRepository, authRepository) as T
    }
}

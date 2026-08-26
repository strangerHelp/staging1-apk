package com.strangerhelp.app.ui.screens.profile

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.model.User
import com.strangerhelp.app.data.model.UserStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _stats = MutableStateFlow<UserStats?>(null)
    val stats: StateFlow<UserStats?> = _stats.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch User
                val userRes = ApiClient.api.getMe()
                if (userRes.isSuccessful) {
                    _user.value = userRes.body()?.user
                }

                // In a real app we might fetch stats from /api/users/{id}
                // Mocking stats for now since backend doesn't explicitly return UserStats via getMe
                _stats.value = UserStats(
                    rating = 4.8,
                    totalReviews = 14,
                    tasksCompleted = 9,
                    completionRate = 92,
                    trustScore = 85
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            try {
                ApiClient.api.resendVerification()
                // In a real app we could show a toast here
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                ApiClient.api.logout()
                val sharedPrefs = getApplication<Application>().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                sharedPrefs.edit().clear().apply()
                onLogoutComplete()
            } catch (e: Exception) {
                e.printStackTrace()
                onLogoutComplete()
            }
        }
    }
}

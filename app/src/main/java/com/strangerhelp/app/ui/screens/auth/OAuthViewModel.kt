package com.strangerhelp.app.ui.screens.auth

import android.webkit.CookieManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class OAuthState {
    object Idle : OAuthState()
    object Loading : OAuthState()
    data class Success(val user: User) : OAuthState()
    data class Error(val message: String) : OAuthState()
}

class OAuthViewModel : ViewModel() {
    private val _oauthState = MutableStateFlow<OAuthState>(OAuthState.Idle)
    val oauthState: StateFlow<OAuthState> = _oauthState
    
    private var loginAttempted = false

    fun processCookiesAndVerify(url: String?) {
        if (loginAttempted) return
        
        if (url?.startsWith("https://strangerhelp.com/login") == true || 
            url?.contains("success=1") == true || 
            url?.startsWith("https://strangerhelp.com") == true) {
            
            val cookieManager = CookieManager.getInstance()
            val cookieString = cookieManager.getCookie("https://strangerhelp.com")
            
            if (!cookieString.isNullOrEmpty() && cookieString.contains("session")) {
                loginAttempted = true
                _oauthState.value = OAuthState.Loading
                
                ApiClient.injectCookies(cookieString)
                
                viewModelScope.launch {
                    try {
                        val meRes = ApiClient.api.getMe()
                        val user = meRes.body()?.user
                        if (meRes.isSuccessful && user != null) {
                            _oauthState.value = OAuthState.Success(user)
                        } else {
                            loginAttempted = false
                            _oauthState.value = OAuthState.Error("Failed to fetch user profile.")
                        }
                    } catch (e: Exception) {
                        loginAttempted = false
                        _oauthState.value = OAuthState.Error(e.message ?: "Network error occurred.")
                    }
                }
            }
        }
    }
}

package com.strangerhelp.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.model.User
import com.strangerhelp.app.navigation.AppNavigation
import com.strangerhelp.app.ui.screens.auth.LoginScreen
import com.strangerhelp.app.ui.screens.LandingScreen
import com.strangerhelp.app.ui.theme.StrangerHelpTheme
import com.strangerhelp.app.utils.AppLogger
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            ApiClient.init(this)
        } catch (e: Exception) {
            AppLogger.e("MainActivity", "Failed to initialize ApiClient during app launch", e)
        }
        
        enableEdgeToEdge()

        setContent {
            StrangerHelpTheme {
                var currentUser by remember { mutableStateOf<User?>(null) }
                var isLoading by remember { mutableStateOf(true) }
                val scope = rememberCoroutineScope() // Note: globalExceptionHandler could be added if passing context was easier in compose without breaking structure

                // Check if already logged in
                LaunchedEffect(Unit) {
                    try {
                        val res = ApiClient.api.getMe()
                        if (res.isSuccessful) currentUser = res.body()?.user
                    } catch (_: Exception) {}
                    isLoading = false
                }

                when {
                    isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    currentUser == null -> {
                        var showLogin by remember { mutableStateOf(false) }
                        if (showLogin) {
                            LoginScreen(onLoginSuccess = { user ->
                                currentUser = user
                                showLogin = false
                            })
                        } else {
                            LandingScreen(onLoginClick = { showLogin = true })
                        }
                    }
                    else -> {
                        AppNavigation(
                            user = currentUser!!,
                            onLogout = {
                                scope.launch {
                                    try { ApiClient.api.logout() } catch (_: Exception) {}
                                    ApiClient.clearSession()
                                    currentUser = null
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

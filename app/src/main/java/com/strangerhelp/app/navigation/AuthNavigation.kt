package com.strangerhelp.app.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.strangerhelp.app.data.model.User
import com.strangerhelp.app.ui.screens.LandingScreen
import com.strangerhelp.app.ui.screens.auth.EmailSentScreen
import com.strangerhelp.app.ui.screens.auth.ForgotPasswordScreen
import com.strangerhelp.app.ui.screens.auth.LoginScreen
import com.strangerhelp.app.ui.screens.auth.ResetPasswordScreen

@Composable
fun AuthNavigation(onLoginSuccess: (User) -> Unit) {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "landing") {
        composable("landing") {
            LandingScreen(onLoginClick = { navController.navigate("login") })
        }
        
        composable("login") {
            LoginScreen(
                onLoginSuccess = onLoginSuccess,
                onForgotPasswordClick = { navController.navigate("forgot_password") },
                onGoogleLoginClick = { navController.navigate("oauth_webview") }
            )
        }
        composable("forgot_password") {
            ForgotPasswordScreen(navController = navController)
        }
        composable(
            "email_sent/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            EmailSentScreen(navController = navController, email = email)
        }
        composable(
            "reset_password/{token}",
            arguments = listOf(navArgument("token") { type = NavType.StringType }),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://strangerhelp.com/reset-password?token={token}"
                    action = Intent.ACTION_VIEW
                }
            )
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token") ?: ""
            ResetPasswordScreen(navController = navController, token = token)
        }
    }
}

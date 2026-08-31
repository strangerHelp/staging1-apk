package com.strangerhelp.app.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Forgot Password", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundLight
                )
            )
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            
            Surface(
                shape = CircleShape,
                color = Color(0xFFF2F2F2),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = Primary, modifier = Modifier.size(28.dp))
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Text(
                text = "Trouble logging in?",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium,
                color = Primary
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = "Enter your email and we'll send you a\nlink to reset your password.",
                color = Body,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(32.dp))
            
            if (error != null) {
                Surface(
                    color = Color(0xFFFFEBEB),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFCDCD)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = Color(0xFFB3261E))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = error!!,
                            color = Color(0xFFB3261E),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Email Address",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                    color = Primary
                )
                
                Spacer(Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        error = null
                    },
                    placeholder = { Text("user@example.com", color = Muted) },
                    leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = Muted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, autoCorrect = false),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline)
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = {
                    if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        error = "Please enter a valid email address."
                        return@Button
                    }
                    
                    isLoading = true
                    error = null
                    scope.launch {
                        try {
                            val res = ApiClient.api.forgotPassword(mapOf("email" to email.trim()))
                            if (res.isSuccessful) {
                                navController.navigate("email_sent/${email.trim()}")
                            } else {
                                val errorStr = res.errorBody()?.string() ?: ""
                                error = if (res.code() == 429) {
                                    "Too many requests. Please wait before trying again."
                                } else {
                                    "Failed to send reset link. Please try again."
                                }
                            }
                        } catch (e: Exception) {
                            error = "Network error. Please check your connection."
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = email.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Send Reset Link",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }
            
            Spacer(Modifier.height(48.dp))
            
            Text(
                text = "Back to Login",
                color = Primary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                style = MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.Underline),
                modifier = Modifier.clickable { navController.popBackStack() }.padding(8.dp)
            )
        }
    }
}

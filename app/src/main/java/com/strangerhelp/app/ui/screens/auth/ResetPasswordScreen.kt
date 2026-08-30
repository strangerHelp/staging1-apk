package com.strangerhelp.app.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(navController: NavController, token: String) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    val minLength = password.length >= 8
    val hasNumber = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    val isMatch = password.isNotEmpty() && password == confirmPassword
    val isValid = minLength && hasNumber && hasSpecial && isMatch
    
    val passwordsDoNotMatch = confirmPassword.isNotEmpty() && !isMatch
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Reset Password", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack("login", inclusive = false) }) {
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
            if (success) {
                Spacer(Modifier.height(48.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CyanDeep.copy(alpha = 0.1f),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Success", tint = CyanDeep, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Password Reset Successfully!", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        Text("You can now log in with your new password.", textAlign = TextAlign.Center)
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = { navController.popBackStack("login", inclusive = false) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("Back to Login", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                return@Scaffold
            }
        
            Spacer(Modifier.height(32.dp))
            
            Surface(
                shape = CircleShape,
                color = Color(0xFFF2F2F2),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Key, contentDescription = null, tint = Primary, modifier = Modifier.size(28.dp))
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Text(
                text = "Create New Password",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium,
                color = Primary
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = "Enter your new password below.",
                color = Body,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(32.dp))
            
            Surface(
                color = Color.Transparent,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    
                    if (error != null || passwordsDoNotMatch) {
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
                                    text = error ?: "Passwords do not match.",
                                    color = Color(0xFFB3261E),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                    
                    Text(
                        text = "New Password",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        color = Primary
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; error = null },
                        placeholder = { Text("••••••••", color = Muted) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = "Toggle password visibility", tint = Muted)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrect = false),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF7F7F7),
                            unfocusedContainerColor = Color(0xFFF7F7F7),
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Text(
                        text = "Confirm Password",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        color = Primary
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; error = null },
                        placeholder = { Text("••••••••", color = Muted) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (confirmPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(imageVector = image, contentDescription = "Toggle password visibility", tint = Muted)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrect = false),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF7F7F7),
                            unfocusedContainerColor = Color(0xFFF7F7F7),
                            focusedBorderColor = if (passwordsDoNotMatch) Color(0xFFB3261E) else Primary,
                            unfocusedBorderColor = if (passwordsDoNotMatch) Color(0xFFB3261E) else MaterialTheme.colorScheme.outline
                        )
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Password Requirements", fontWeight = FontWeight.Bold, color = Primary, fontSize = 16.sp)
                            Spacer(Modifier.height(12.dp))
                            RequirementItem("Minimum 8 characters", minLength)
                            RequirementItem("At least one number", hasNumber)
                            RequirementItem("At least one special character", hasSpecial)
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            Button(
                onClick = {
                    isLoading = true
                    error = null
                    scope.launch {
                        try {
                            val res = ApiClient.api.resetPassword(mapOf("token" to token, "password" to password))
                            if (res.isSuccessful) {
                                success = true
                            } else {
                                val errorStr = res.errorBody()?.string() ?: ""
                                error = if (res.code() == 429) {
                                    "Too many attempts. Please try again later."
                                } else if (res.code() == 400 || errorStr.contains("invalid", ignoreCase = true) || errorStr.contains("expire", ignoreCase = true)) {
                                    "This reset link is invalid or has expired."
                                } else {
                                    "Failed to reset password. Please try again."
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
                enabled = isValid && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "RESET PASSWORD",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RequirementItem(text: String, isMet: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        if (isMet) {
            Icon(Icons.Outlined.CheckCircle, contentDescription = "Met", tint = Saffron, modifier = Modifier.size(18.dp))
        } else {
            Icon(Icons.Outlined.RadioButtonUnchecked, contentDescription = "Not Met", tint = Muted, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            color = Body,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

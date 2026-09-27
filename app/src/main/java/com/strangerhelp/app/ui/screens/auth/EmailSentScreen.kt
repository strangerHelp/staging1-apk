package com.strangerhelp.app.ui.screens.auth

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailSentScreen(navController: NavController, email: String) {
    val context = LocalContext.current
    var isResending by remember { mutableStateOf(false) }
    var resendMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Check Your Email", fontWeight = FontWeight.Bold) },
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
            Spacer(Modifier.height(48.dp))
            
            Surface(
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(1.dp, Cyan.copy(alpha = 0.2f)),
                modifier = Modifier.size(120.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Surface(
                        shape = CircleShape,
                        color = Cyan.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, Cyan.copy(alpha = 0.3f)),
                        modifier = Modifier.size(96.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Email, contentDescription = null, tint = CyanDeep, modifier = Modifier.size(40.dp))
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                text = "We've sent you a reset link",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium,
                color = Primary,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(16.dp))
            
            val annotatedText = buildAnnotatedString {
                append("Check your email at ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Primary)) {
                    append(email)
                }
                append(".\nThe link will expire in 1 hour.")
            }
            
            Text(
                text = annotatedText,
                style = MaterialTheme.typography.bodyLarge,
                color = Body,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(48.dp))
            
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_APP_EMAIL)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Email app not found
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Saffron)
            ) {
                Text(
                    text = "Open Email App",
                    color = OnSaffron,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = {
                    if (isResending) return@OutlinedButton
                    isResending = true
                    resendMessage = null
                    scope.launch {
                        try {
                            val res = ApiClient.api.forgotPassword(mapOf("email" to email))
                            if (res.isSuccessful) {
                                resendMessage = "Link resent successfully."
                            } else {
                                resendMessage = if (res.code() == 429) "Too many attempts. Please wait." else "Failed to resend."
                            }
                        } catch(e: Exception) {
                            resendMessage = "Network error."
                        }
                        isResending = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, Primary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
            ) {
                if (isResending) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Primary)
                } else {
                    Text(
                        text = "Resend Email",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            
            val msg = resendMessage
            if (msg != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = msg,
                    color = Primary,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(Modifier.height(48.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { navController.popBackStack("login", inclusive = false) }.padding(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Outlined.Login, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Back to Login",
                    color = Primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.Underline)
                )
            }
        }
    }
}

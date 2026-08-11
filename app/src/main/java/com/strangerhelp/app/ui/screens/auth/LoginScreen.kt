package com.strangerhelp.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.model.User
import com.strangerhelp.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: (User) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isRegister by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(32.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 16.dp)) {
                    com.strangerhelp.app.ui.components.StrangerHelpLogo(size = 96.dp)
                    Spacer(Modifier.height(8.dp))
                    androidx.compose.ui.text.buildAnnotatedString {
                        withStyle(androidx.compose.ui.text.SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 32.sp)) {
                            append("stranger")
                        }
                        withStyle(androidx.compose.ui.text.SpanStyle(color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 32.sp)) {
                            append("help")
                        }
                    }.let { text ->
                        Text(text = text)
                    }
                }
                
                Text(
                    text = if (isRegister) "Create Account" else "Welcome Back",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = if (isRegister) "Sign up to join your community." else "Log in to start getting help or lend a hand in your community today.",
                    color = Muted,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(24.dp))
                
                // Tabs
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isRegister = false; error = null }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Login", color = if (!isRegister) MaterialTheme.colorScheme.primary else Muted, fontWeight = if (!isRegister) FontWeight.Bold else FontWeight.Normal)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isRegister = true; error = null }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Register", color = if (isRegister) MaterialTheme.colorScheme.primary else Muted, fontWeight = if (isRegister) FontWeight.Bold else FontWeight.Normal)
                    }
                }
                
                // Tab Indicator Line
                Row(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = 2.dp,
                        color = if (!isRegister) MaterialTheme.colorScheme.primary else Hairline
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = 2.dp,
                        color = if (isRegister) MaterialTheme.colorScheme.primary else Hairline
                    )
                }
                
                Spacer(Modifier.height(24.dp))
                
                error?.let {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(it, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(16.dp))
                }
                
                if (isRegister) {
                    OutlinedTextField(
                        value = name, 
                        onValueChange = { name = it }, 
                        placeholder = { Text("Full Name", color = Muted) }, 
                        modifier = Modifier.fillMaxWidth(), 
                        shape = RoundedCornerShape(8.dp), 
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Hairline
                        )
                    )
                    Spacer(Modifier.height(16.dp))
                }
                
                OutlinedTextField(
                    value = email, 
                    onValueChange = { email = it }, 
                    placeholder = { Text("name@example.com", color = Muted) }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(8.dp), 
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Hairline
                    )
                )
                
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = password, 
                    onValueChange = { password = it }, 
                    placeholder = { Text("Password", color = Muted) }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(8.dp), 
                    singleLine = true, 
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = "Toggle password visibility", tint = Muted)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Hairline
                    )
                )
                
                if (isRegister) {
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = city, 
                        onValueChange = { city = it }, 
                        placeholder = { Text("City", color = Muted) }, 
                        modifier = Modifier.fillMaxWidth(), 
                        shape = RoundedCornerShape(8.dp), 
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Hairline
                        )
                    )
                }
                
                if (!isRegister) {
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { /* Handle password reset */ }) {
                            Text("Forgot password?", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                } else {
                    Spacer(Modifier.height(24.dp))
                }
                
                Button(
                    onClick = {
                        isLoading = true; error = null
                        scope.launch {
                            try {
                                val res = if (isRegister) {
                                    ApiClient.api.register(mapOf("name" to name, "email" to email, "password" to password, "city" to city))
                                } else {
                                    ApiClient.api.login(mapOf("email" to email, "password" to password))
                                }
                                if (res.isSuccessful) {
                                    // Fetch full user
                                    val meRes = ApiClient.api.getMe()
                                    if (meRes.isSuccessful && meRes.body()?.user != null) {
                                        onLoginSuccess(meRes.body()!!.user!!)
                                    } else error = "Login succeeded but failed to load profile"
                                } else {
                                    error = if (isRegister) "Registration failed. Email may already exist." else "Invalid email or password"
                                }
                            } catch (e: Exception) {
                                error = "Network error: ${e.message?.take(50)}"
                            }
                            isLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    enabled = email.isNotBlank() && password.isNotBlank() && !isLoading && (!isRegister || (name.isNotBlank() && city.isNotBlank())),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onSecondary, strokeWidth = 2.dp)
                    } else {
                        Text(if (isRegister) "Sign Up" else "Sign In", color = MaterialTheme.colorScheme.onSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary)
                    }
                }
                
                Spacer(Modifier.height(32.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(Modifier.weight(1f), color = Hairline)
                    Text("OR CONTINUE WITH", Modifier.padding(horizontal = 16.dp), color = Muted, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, letterSpacing = 1.sp)
                    HorizontalDivider(Modifier.weight(1f), color = Hairline)
                }
                
                Spacer(Modifier.height(24.dp))
                
                OutlinedButton(
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://strangerhelp.com/api/auth/google"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.strangerhelp.app.R.drawable.ic_google),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Google", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

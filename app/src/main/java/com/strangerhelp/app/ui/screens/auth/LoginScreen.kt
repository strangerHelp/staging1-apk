package com.strangerhelp.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.model.User
import com.strangerhelp.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: (User) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isRegister by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(32.dp))
        
        AsyncImage(
            model = "https://images.unsplash.com/photo-1529156069898-49953eb1b5ae?q=80&w=800&auto=format&fit=crop",
            contentDescription = "Community helping each other",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(24.dp))

        Text(
            if (isRegister) "Create an account." else "Welcome back.",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        if (!isRegister) {
            Text(
                "StrangerHelp connects you with trusted locals to get tasks done or offer your skills.",
                color = Muted,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
            )
        } else {
            Text(
                "Sign up for a StrangerHelp account.",
                color = Muted,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        Spacer(Modifier.height(32.dp))

        error?.let {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Text(it, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(16.dp))
        }

        OutlinedButton(
            onClick = {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://strangerhelp.com/api/auth/google"))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Text("Continue with Google", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(Modifier.weight(1f), color = Hairline)
            Text("or", Modifier.padding(horizontal = 16.dp), color = Muted, style = MaterialTheme.typography.labelSmall)
            HorizontalDivider(Modifier.weight(1f), color = Hairline)
        }
        Spacer(Modifier.height(24.dp))

        if (isRegister) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current.copy(alpha = 1f)))
            Spacer(Modifier.height(12.dp))
        }

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current.copy(alpha = 1f)))
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, visualTransformation = PasswordVisualTransformation(), textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current.copy(alpha = 1f)))
        Spacer(Modifier.height(16.dp))

        if (isRegister) {
            OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current.copy(alpha = 1f)))
            Spacer(Modifier.height(16.dp))
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
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = email.isNotBlank() && password.isNotBlank() && !isLoading && (!isRegister || (name.isNotBlank() && city.isNotBlank())),
        ) {
            if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            else Text(if (isRegister) "Sign Up" else "Log In", fontWeight = FontWeight.SemiBold)
        }

        if (!isRegister) {
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { /* Handle password reset */ }) {
                    Text("Forgot password?", color = Link, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = { isRegister = !isRegister; error = null }) {
            Text(
                if (isRegister) "Already have an account? Log In" else "Don't have an account? Sign up",
                color = Link
            )
        }
    }
}

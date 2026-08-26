package com.strangerhelp.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.strangerhelp.app.ui.theme.Muted
import com.strangerhelp.app.ui.theme.OnPrimary
import com.strangerhelp.app.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationScreen(
    token: String,
    viewModel: AuthViewModel = viewModel(),
    navController: NavController
) {
    val isVerifying by viewModel.isVerifying.collectAsState()
    val verificationSuccess by viewModel.verificationSuccess.collectAsState()
    val verificationError by viewModel.verificationError.collectAsState()

    // Trigger verification on first launch
    LaunchedEffect(Unit) {
        viewModel.verifyEmail(token)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Email Verification") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                // ⭐ Loading State
                isVerifying -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = Primary
                        )
                        Text(
                            text = "Verifying your email...",
                            fontSize = 14.sp,
                            color = Muted
                        )
                    }
                }

                // ⭐ Success State
                verificationSuccess -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Text(
                            text = "✅",
                            fontSize = 48.sp
                        )
                        Text(
                            text = "Email Verified!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Your email has been successfully verified.",
                            fontSize = 14.sp,
                            color = Muted,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewModel.clearVerificationState()
                                navController.navigate("profile") {
                                    popUpTo("verification") { inclusive = true }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary
                            )
                        ) {
                            Text("Continue to Profile", color = OnPrimary)
                        }
                    }
                }

                // ⭐ Error State
                verificationError != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Text(
                            text = "❌",
                            fontSize = 48.sp
                        )
                        Text(
                            text = "Verification Failed",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = verificationError ?: "Invalid or expired link",
                            fontSize = 14.sp,
                            color = Muted,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "The verification link may have expired. You can request a new one from your profile.",
                            fontSize = 12.sp,
                            color = Muted,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                viewModel.clearVerificationState()
                                navController.navigate("profile") {
                                    popUpTo("verification") { inclusive = true }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(26.dp)
                        ) {
                            Text("Back to Profile")
                        }
                        TextButton(
                            onClick = {
                                navController.navigate("profile")
                                // User can click "Resend" from profile
                            }
                        ) {
                            Text("Resend Verification Email")
                        }
                    }
                }
            }
        }
    }
}

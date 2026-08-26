package com.strangerhelp.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.strangerhelp.app.data.model.User
import com.strangerhelp.app.ui.theme.*
import com.strangerhelp.app.ui.components.EmailVerificationBanner
import com.strangerhelp.app.ui.screens.profile.AuthViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    verificationViewModel: VerificationViewModel = viewModel()
) {
    val user by viewModel.user.collectAsState()
    val stats by viewModel.stats.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
    
    val isSendingVerification by authViewModel.isSendingVerification.collectAsState()
    val verificationMessage by authViewModel.verificationMessage.collectAsState()
    val verificationError by authViewModel.verificationError.collectAsState()
    
    val verificationStatus by verificationViewModel.status.collectAsState()
    
    LaunchedEffect(Unit) {
        verificationViewModel.loadStatus()
    }

    LaunchedEffect(Unit) {
        // viewModel.loadData()
    }

    if (user?.banned == 1) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF9F9F9)), contentAlignment = Alignment.Center) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Error)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Gavel, contentDescription = null, tint = Error, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Account Suspended", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Error)
                    Spacer(Modifier.height(8.dp))
                    Text("Your account has been restricted.", color = Body, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = onLogout, colors = ButtonDefaults.buttonColors(containerColor = Error)) {
                        Text("Logout")
                    }
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF9F9F9))
            )
        },
        containerColor = Color(0xFFF9F9F9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (user?.emailVerified == 0) {
                EmailVerificationBanner(
                    onResendClick = { authViewModel.resendVerificationEmail() },
                    isSending = isSendingVerification,
                    message = verificationMessage,
                    error = verificationError
                )
            }

            // Hero Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        AsyncImage(
                            model = "https://ui-avatars.com/api/?name=${user?.name ?: "U"}&background=E0E0E0&color=333&size=200",
                            contentDescription = "Avatar",
                            modifier = Modifier.size(80.dp).clip(CircleShape).background(Color.LightGray)
                        )
                        if (user?.verified == 1) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .offset(x = 4.dp, y = 4.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = "Verified", tint = Color(0xFF004D40), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(text = user?.name ?: "Unknown User", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    if (!user?.handle.isNullOrBlank()) {
                        Text(text = "@${user?.handle}", fontSize = 14.sp, color = Color.Gray)
                    }
                    Spacer(Modifier.height(8.dp))
                    if (!user?.city.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                            Spacer(Modifier.width(4.dp))
                            Text(text = user?.city ?: "", fontSize = 14.sp, color = Color.Black)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    if (user?.emailVerified == 1) {
                        Text(
                            text = "EMAIL VERIFIED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00BFA5),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF004D40))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    OutlinedButton(
                        onClick = { navController.navigate("edit_profile") },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black)
                    ) {
                        Text("Edit Profile", fontWeight = FontWeight.Medium)
                    }
                }
            }



            // Quick Actions
            QuickActionsRow(
                onVerifyIdClick = {
                    when (verificationStatus?.status) {
                        "approved" -> navController.navigate("verify_id")
                        "pending" -> navController.navigate("verify_id")
                        else -> navController.navigate("verify_id")
                    }
                },
                onReferClick = { navController.navigate("refer_earn") },
                onKarmaClick = { navController.navigate("karma_wallet") }
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Trust Stats
            TrustStatsCard(
                rating = stats?.rating ?: 0.0,
                totalReviews = stats?.totalReviews ?: 0,
                tasksCompleted = stats?.tasksCompleted ?: 0,
                completionRate = stats?.completionRate ?: 0,
                trustScore = stats?.trustScore ?: 0,
                verified = user?.verified == 1
            )

            // Menu List
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Column {
                    MenuItemRow(icon = Icons.Outlined.Person, label = "Edit Profile", onClick = { navController.navigate("edit_profile") }, showChevron = true)
                    Divider(color = Color(0xFFF0F0F0))

                    MenuItemRow(
                        icon = Icons.Outlined.Logout, 
                        label = "Logout", 
                        onClick = onLogout, 
                        showChevron = false,
                        textColor = Color(0xFFD32F2F),
                        iconColor = Color(0xFFD32F2F)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun QuickActionCard(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(84.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, fontSize = 12.sp, color = Color.Black)
        }
    }
}

@Composable
fun MenuItemRow(
    icon: ImageVector, 
    label: String, 
    onClick: () -> Unit, 
    showChevron: Boolean = true,
    textColor: Color = Color.Black,
    iconColor: Color = Color.Black,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(Color(0xFFF5F5F5), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Text(label, fontSize = 16.sp, color = textColor, modifier = Modifier.weight(1f))
        if (trailingContent != null) {
            trailingContent()
            Spacer(Modifier.width(8.dp))
        }
        if (showChevron) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

fun calculateTrustPercent(completionRate: Int, verified: Boolean, totalReviews: Int): Int {
    val fromCompletion = (completionRate * 0.4).toInt()
    val fromVerification = if (verified) 20 else 0
    val fromReviews = minOf(totalReviews * 4, 40)
    return minOf(100, fromCompletion + fromVerification + fromReviews)
}

@Composable
fun TrustStatsCard(
    rating: Double,
    totalReviews: Int,
    tasksCompleted: Int,
    completionRate: Int,
    trustScore: Int,
    verified: Boolean
) {
    val trustPercent = if (trustScore > 0) trustScore else calculateTrustPercent(completionRate, verified, totalReviews)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${"%.1f".format(rating)} ", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBC02D), modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("$totalReviews reviews", fontSize = 12.sp, color = Color.Gray)
                }
                Divider(modifier = Modifier.width(1.dp).height(40.dp), color = Color(0xFFE0E0E0))
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("$tasksCompleted", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(Modifier.height(4.dp))
                    Text("Tasks\nCompleted", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                }
                Divider(modifier = Modifier.width(1.dp).height(40.dp), color = Color(0xFFE0E0E0))
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("$completionRate%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(Modifier.height(4.dp))
                    Text("Completion\nRate", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
            
            Spacer(Modifier.height(24.dp))
            Divider(color = Color(0xFFE0E0E0))
            Spacer(Modifier.height(24.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Trust Score", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.background(Color(0xFF004D40), RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).background(Color(0xFF00BFA5), CircleShape))
                    Spacer(Modifier.width(6.dp))
                    Text("Excellent ($trustPercent)", color = Color(0xFF00BFA5), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { trustPercent / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Color.Black,
                trackColor = Color(0xFFEEEEEE)
            )
            Spacer(Modifier.height(12.dp))
            Text("Score is based on successful tasks and verified information.", fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp)
        }
    }
}

@Composable
fun QuickActionsRow(
    onVerifyIdClick: () -> Unit,
    onReferClick: () -> Unit,
    onKarmaClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionCardStr(
            icon = "🪪",
            label = "Verify ID",
            onClick = onVerifyIdClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionCardStr(
            icon = "🎁",
            label = "Refer & Earn",
            onClick = onReferClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionCardStr(
            icon = "⭐",
            label = "Karma Wallet",
            onClick = onKarmaClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickActionCardStr(
    icon: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(72.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceVariant
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = icon, fontSize = 24.sp)
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Body,
                textAlign = TextAlign.Center
            )
        }
    }
}

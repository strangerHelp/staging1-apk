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

import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.scale

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

import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.entryModelOf



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
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
            
            // Karma Rank Progress (Level-Up Animation)
            val computedKarmaPoints = (stats?.tasksCompleted ?: 0) * 125 + 230 // Mock formula to show some progress
            KarmaRankCard(karmaPoints = computedKarmaPoints)
            
            Spacer(Modifier.height(24.dp))
            
            // Trust Stats
            TrustStatsCard(
                rating = stats?.rating ?: 0.0,
                totalReviews = stats?.totalReviews ?: 0,
                tasksCompleted = stats?.tasksCompleted ?: 0,
                completionRate = stats?.completionRate ?: 0,
                trustScore = stats?.trustScore ?: 0,
                verified = user?.verified == 1
            )

            Spacer(Modifier.height(24.dp))
            ActivityLogCard()
            Spacer(Modifier.height(24.dp))
            // ⭐ Legal Section
            Text(
                text = "Legal",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Muted,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            LegalMenuItem(
                icon = "📜",
                title = "Terms of Service",
                subtitle = "Includes P2P payment terms",
                onClick = { navController.navigate("webview?url=${java.net.URLEncoder.encode("https://strangerhelp.com/terms", "UTF-8")}") }
            )
            LegalMenuItem(
                icon = "🔒",
                title = "Privacy Policy",
                subtitle = "We do not collect payment data",
                onClick = { navController.navigate("webview?url=${java.net.URLEncoder.encode("https://strangerhelp.com/privacy", "UTF-8")}") }
            )
            LegalMenuItem(
                icon = "⚠️",
                title = "Disclaimer",
                subtitle = "P2P payments — platform not liable",
                onClick = { navController.navigate("disclaimer") }
            )
            LegalMenuItem(
                icon = "🍪",
                title = "Cookie Policy",
                subtitle = "",
                onClick = { navController.navigate("cookie_policy") }
            )
            LegalMenuItem(
                icon = "📋",
                title = "Community Guidelines",
                subtitle = "",
                onClick = { navController.navigate("community_guidelines") }
            )
            
            Spacer(Modifier.height(24.dp))
            // Menu List
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
        QuickActionCardIcon(
            icon = Icons.Outlined.AccountBalanceWallet,
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LegalMenuItem(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}



@Composable
fun QuickActionCardIcon(
    icon: ImageVector,
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun ActivityLogCard(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(0) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Activity Log", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Tasks Completed") }, selectedContentColor = MaterialTheme.colorScheme.primary, unselectedContentColor = Color.Gray)
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Karma Earned") }, selectedContentColor = MaterialTheme.colorScheme.primary, unselectedContentColor = Color.Gray)
            }
            
            Spacer(Modifier.height(24.dp))
            
            if (selectedTab == 0) {
                val taskData = entryModelOf(2f, 5f, 4f, 8f, 6f, 10f)
                val labels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
                val bottomAxisFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                    labels.getOrNull(value.toInt()) ?: ""
                }
                
                Chart(
                    chart = columnChart(
                        columns = listOf(lineComponent(color = Color(0xFF00BFA5), thickness = 16.dp, shape = com.patrykandpatrick.vico.core.component.shape.Shapes.roundedCornerShape(topRightPercent = 50, topLeftPercent = 50)))
                    ),
                    model = taskData,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisFormatter),
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                )
            } else {
                val karmaData = entryModelOf(15f, 45f, 30f, 80f, 60f, 120f)
                val labels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
                val bottomAxisFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                    labels.getOrNull(value.toInt()) ?: ""
                }
                
                Chart(
                    chart = lineChart(
                        lines = listOf(
                            com.patrykandpatrick.vico.compose.chart.line.lineSpec(
                                lineColor = Color(0xFFFFB300),
                                lineThickness = 3.dp,
                                lineBackgroundShader = null
                            )
                        )
                    ),
                    model = karmaData,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisFormatter),
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                )
            }
        }
    }
}


@Composable
fun KarmaRankCard(karmaPoints: Int, modifier: Modifier = Modifier) {
    val ranks = listOf(
        0 to "Novice Helper",
        500 to "Bronze Helper",
        1500 to "Silver Helper",
        3000 to "Gold Helper",
        5000 to "Platinum Helper"
    )
    
    var currentRank = ranks[0].second
    var nextRank = ranks[1].second
    var minPoints = ranks[0].first
    var maxPoints = ranks[1].first
    
    for (i in 0 until ranks.size - 1) {
        if (karmaPoints >= ranks[i].first && karmaPoints < ranks[i+1].first) {
            currentRank = ranks[i].second
            nextRank = ranks[i+1].second
            minPoints = ranks[i].first
            maxPoints = ranks[i+1].first
            break
        }
    }
    
    if (karmaPoints >= ranks.last().first) {
        currentRank = ranks.last().second
        nextRank = "Max Rank"
        minPoints = ranks.last().first
        maxPoints = ranks.last().first + 1000
    }
    
    val targetProgress = ((karmaPoints - minPoints).toFloat() / (maxPoints - minPoints).toFloat()).coerceIn(0f, 1f)
    
    var animationPlayed by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (animationPlayed) targetProgress else 0f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
    )
    
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), // Dark premium background
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Karma Rank",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        currentRank,
                        color = Color(0xFFFBBF24), // Gold color
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF334155), CircleShape)
                        .scale(pulseScale)
                ) {
                    Text("🌟", fontSize = 24.sp)
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$karmaPoints KP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("$maxPoints KP", color = Color(0xFF94A3B8), fontSize = 14.sp)
            }
            
            Spacer(Modifier.height(8.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Color(0xFF334155), RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(8.dp)
                        .background(
                            color = Color(0xFFFBBF24),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                "Earn ${maxPoints - karmaPoints} more KP to reach $nextRank!",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

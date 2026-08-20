import os

# Update AppNavigation.kt
nav_file = "app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt"
with open(nav_file, "r") as f:
    content = f.read()

if "import com.strangerhelp.app.ui.screens.profile.EditProfileScreen" not in content:
    content = content.replace("import com.strangerhelp.app.ui.screens.profile.ProfileScreen", "import com.strangerhelp.app.ui.screens.profile.ProfileScreen\nimport com.strangerhelp.app.ui.screens.profile.EditProfileScreen")

if "composable(\"edit_profile\")" not in content:
    content = content.replace("composable(\"postQuestion\") { PostQuestionScreen(navController) }", "composable(\"postQuestion\") { PostQuestionScreen(navController) }\n            composable(\"edit_profile\") { EditProfileScreen(navController, user) }")

with open(nav_file, "w") as f:
    f.write(content)


# Rewrite ProfileScreen.kt
profile_code = """package com.strangerhelp.app.ui.screens.profile

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val user by viewModel.user.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
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
            if (user?.emailVerified == false) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE0B2)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WarningAmber, contentDescription = null, tint = Color(0xFF5D4037))
                            Spacer(Modifier.width(8.dp))
                            Text("Verify your email", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Verify your email to unlock all features and increase trust.",
                            fontSize = 14.sp, color = Color.DarkGray
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.resendVerificationEmail() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF795516), contentColor = Color.White),
                            modifier = Modifier.align(Alignment.End),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("Verify", fontWeight = FontWeight.Medium)
                        }
                    }
                }
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
                    if (user?.emailVerified == true) {
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickActionCard(icon = Icons.Outlined.VerifiedUser, label = "Verify ID", onClick = { }, modifier = Modifier.weight(1f))
                QuickActionCard(icon = Icons.Default.CardGiftcard, label = "Refer & Earn", onClick = { }, modifier = Modifier.weight(1f))
                QuickActionCard(icon = Icons.Outlined.StarBorder, label = "Karma", onClick = { }, modifier = Modifier.weight(1f))
            }

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
                        icon = Icons.Outlined.VerifiedUser, 
                        label = "Identity Verification", 
                        onClick = { }, 
                        showChevron = true,
                        trailingContent = {
                            if (user?.verified == 1) {
                                Text(
                                    "Verified", 
                                    color = Color(0xFF00BFA5), 
                                    fontSize = 12.sp,
                                    modifier = Modifier.background(Color(0xFF004D40), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    )
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
                        Icon(Icons.Default.StarBorder, contentDescription = null, tint = Color(0xFFFBC02D), modifier = Modifier.size(18.dp))
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
"""
with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt", "w") as f:
    f.write(profile_code)


# Write EditProfileScreen.kt
edit_profile_code = """package com.strangerhelp.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.strangerhelp.app.data.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController, user: User) {
    var name by remember { mutableStateOf(user.name) }
    var handle by remember { mutableStateOf(user.handle ?: "") }
    var bio by remember { mutableStateOf("Urban explorer and foodie.") }
    var city by remember { mutableStateOf(user.city ?: "") }
    var locality by remember { mutableStateOf("Koramangala") }
    var phone by remember { mutableStateOf("+91 98765 43210") }
    
    val handleError = handle == "ravi_kumar" // Mock error state

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF9F9F9))
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFF9F9F9)).padding(16.dp)
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("SAVE CHANGES", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Color(0xFFF9F9F9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.padding(top = 8.dp)) {
                AsyncImage(
                    model = "https://ui-avatars.com/api/?name=${user.name}&background=E0E0E0&color=333&size=200",
                    contentDescription = "Avatar",
                    modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.LightGray)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text("CHANGE AVATAR", color = Color(0xFFF57C00), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            
            Spacer(Modifier.height(32.dp))
            
            // Personal Information
            SectionTitle("Personal Information")
            CustomTextField(label = "FULL NAME", value = name, onValueChange = { name = it })
            
            Spacer(Modifier.height(16.dp))
            CustomTextField(
                label = "USERNAME", 
                value = "@$handle", 
                onValueChange = { handle = it.removePrefix("@") },
                isError = handleError,
                errorMessage = "Handle already taken",
                trailingText = "3 chars min",
                trailingIcon = if (handleError) Icons.Default.ErrorOutline else null
            )
            
            Spacer(Modifier.height(16.dp))
            CustomTextField(
                label = "BIO", 
                value = bio, 
                onValueChange = { bio = it },
                singleLine = false,
                modifier = Modifier.height(100.dp),
                trailingText = "${bio.length}/5000"
            )
            
            Spacer(Modifier.height(32.dp))
            
            // Skills
            SectionTitle("Skills")
            Text("Add skills to help others find you for specific tasks.", fontSize = 14.sp, color = Color.DarkGray, modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth())
            
            Box(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)).padding(12.dp)
            ) {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        SkillChip("Cooking")
                        SkillChip("Driving")
                        SkillChip("Photography")
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Add skill...", color = Color.Gray, fontSize = 14.sp)
                }
            }
            
            Spacer(Modifier.height(16.dp))
            Text("SUGGESTED", fontSize = 10.sp, letterSpacing = 1.sp, color = Color.DarkGray, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SuggestedSkillChip("+ Plumbing")
                SuggestedSkillChip("+ Electrical")
                SuggestedSkillChip("+ Tutoring")
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Location
            SectionTitle("Location")
            CustomTextField(label = "CITY", value = city, onValueChange = { city = it })
            Spacer(Modifier.height(16.dp))
            CustomTextField(label = "AREA / LOCALITY", value = locality, onValueChange = { locality = it })
            Spacer(Modifier.height(16.dp))
            CustomTextField(label = "COUNTRY", value = "India", onValueChange = { })
            
            Spacer(Modifier.height(32.dp))
            
            // Contact
            SectionTitle("Contact")
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text("PHONE", fontSize = 10.sp, letterSpacing = 1.sp, color = Color.DarkGray, modifier = Modifier.padding(bottom = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().height(48.dp).background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(48.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(20.dp))
                    }
                    Divider(modifier = Modifier.width(1.dp).fillMaxHeight().padding(vertical = 8.dp), color = Color(0xFFE0E0E0))
                    BasicTextFieldWrapper(value = phone, onValueChange = { phone = it }, modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
                }
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )
}

@Composable
fun CustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier.height(48.dp),
    isError: Boolean = false,
    errorMessage: String? = null,
    trailingText: String? = null,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 10.sp, letterSpacing = 1.sp, color = if (isError) Color(0xFFD32F2F) else Color.DarkGray, modifier = Modifier.padding(bottom = 4.dp))
            if (trailingText != null) {
                Text(trailingText, fontSize = 10.sp, color = Color.Gray)
            }
        }
        val borderColor = if (isError) Color(0xFFD32F2F) else Color(0xFFE0E0E0)
        Row(
            modifier = Modifier.fillMaxWidth().then(modifier).background(if (isError) Color(0xFFFFEBEE) else Color(0xFFF5F5F5), RoundedCornerShape(8.dp)).border(1.dp, borderColor, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = if (singleLine) 0.dp else 12.dp),
            verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
        ) {
            BasicTextFieldWrapper(value = value, onValueChange = onValueChange, modifier = Modifier.weight(1f), singleLine = singleLine)
            if (trailingIcon != null) {
                Icon(trailingIcon, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
            }
        }
        if (isError && errorMessage != null) {
            Text(errorMessage, color = Color(0xFFD32F2F), fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun BasicTextFieldWrapper(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier, singleLine: Boolean = true) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = Color.Black)
    )
}

@Composable
fun SkillChip(label: String) {
    Row(
        modifier = Modifier.background(Color(0xFFFCE4EC), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = Color(0xFF795548))
        Spacer(Modifier.width(4.dp))
        Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color(0xFF795548), modifier = Modifier.size(12.dp))
    }
}

@Composable
fun SuggestedSkillChip(label: String) {
    Box(
        modifier = Modifier.border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(4.dp)).background(Color.White, RoundedCornerShape(4.dp)).padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(label, fontSize = 12.sp, color = Color.DarkGray)
    }
}
"""
with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/EditProfileScreen.kt", "w") as f:
    f.write(edit_profile_code)


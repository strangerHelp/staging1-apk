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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.navigation.NavController
import com.strangerhelp.app.data.model.User
import com.strangerhelp.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, user: User, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF9F6)) // Warm Off-White Paper
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                var menuExpanded by remember { mutableStateOf(false) }
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Outlined.Menu, contentDescription = "Menu", tint = Primary)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        onClick = { menuExpanded = false },
                        leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Help & Support") },
                        onClick = { menuExpanded = false },
                        leadingIcon = { Icon(Icons.Outlined.HelpOutline, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Logout") },
                        onClick = {
                            menuExpanded = false
                            onLogout()
                        },
                        leadingIcon = { Icon(Icons.Outlined.ExitToApp, contentDescription = null) }
                    )
                }
            }
            
            // Logo
            Row(verticalAlignment = Alignment.CenterVertically) {
                com.strangerhelp.app.ui.components.StrangerHelpLogo(size = 28.dp)
                Spacer(Modifier.width(4.dp))
                androidx.compose.ui.text.buildAnnotatedString {
                    withStyle(androidx.compose.ui.text.SpanStyle(color = Primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)) { append("stranger") }
                    withStyle(androidx.compose.ui.text.SpanStyle(color = Saffron, fontWeight = FontWeight.Bold, fontSize = 16.sp)) { append("help") }
                }.let { text ->
                    Text(text = text)
                }
            }
            
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = Primary)
            }
        }
        
        Spacer(Modifier.height(16.dp))
        // Avatar
        Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.size(96.dp)) {
            if (user.avatar.isNotBlank()) {
                coil.compose.AsyncImage(
                    model = user.avatar,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Surface(modifier = Modifier.fillMaxSize(), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            user.name.take(2).uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            // Cyan dot
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF50E3C2)) // Cyan
                    .border(2.dp, Color(0xFFFAF9F6), CircleShape)
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
        Text(user.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Primary)
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = Muted)
            Spacer(Modifier.width(4.dp))
            Text(user.city.ifBlank { "Downtown District" }, color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
        
        Spacer(Modifier.height(24.dp))
        
        // 2 Cards Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(
                modifier = Modifier.weight(1f).height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Outlined.TaskAlt, contentDescription = null, tint = Saffron, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("142", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Primary)
                    Text("TASKS DONE", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
            
            Card(
                modifier = Modifier.weight(1f).height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Outlined.StarBorder, contentDescription = null, tint = CyanDeep, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("4.9", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Primary)
                    Text("TRUST SCORE", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Joined Card
        Card(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = Muted, modifier = Modifier.size(24.dp))
                Spacer(Modifier.height(8.dp))
                Text("Oct '22", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Primary)
                Text("JOINED", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Badges & Skills
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.EmojiEvents, contentDescription = null, tint = Primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Badges & Skills", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Primary)
                }
                Spacer(Modifier.height(16.dp))
                
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val skills = listOf(
                        Pair(Icons.Outlined.LocalShipping, "Driver"),
                        Pair(Icons.Outlined.Pets, "Pet Care"),
                        Pair(Icons.Outlined.Handyman, "Handyman")
                    )
                    skills.forEach { (icon, name) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.height(36.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(icon, contentDescription = null, tint = CyanDeep, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(name, fontSize = 12.sp, color = Primary)
                            }
                        }
                    }
                    
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.WorkspacePremium, contentDescription = null, tint = Saffron, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Top Helper 2023", fontSize = 12.sp, color = Primary)
                        }
                    }
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Menu Items
        val menuItems = listOf(
            Triple(Icons.Outlined.PersonOutline, "Edit Profile", "edit"),
            Triple(Icons.Outlined.Settings, "Preferences", "settings"),
            Triple(Icons.Outlined.HelpOutline, "Support", "support")
        )
        
        Column(modifier = Modifier.fillMaxWidth()) {
            menuItems.forEach { (icon, label, route) ->
                Card(
                    modifier = Modifier.fillMaxWidth().height(64.dp).padding(bottom = 8.dp).clickable { },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(icon, contentDescription = null, tint = Muted, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Text(label, fontWeight = FontWeight.Medium, color = Primary, modifier = Modifier.weight(1f))
                        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Muted)
                    }
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Logout
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent),
            border = androidx.compose.foundation.BorderStroke(1.dp, Primary)
        ) {
            Icon(Icons.Outlined.Logout, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("LOGOUT", color = Primary, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
        
        Spacer(Modifier.height(32.dp))
    }
}

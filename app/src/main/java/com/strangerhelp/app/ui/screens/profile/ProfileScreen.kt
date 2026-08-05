package com.strangerhelp.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.strangerhelp.app.data.model.User
import com.strangerhelp.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, user: User, onLogout: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(24.dp))
        // Avatar
        Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    user.name.take(2).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(user.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (user.verified == 1) {
            Text("✓ Verified", color = Link, style = MaterialTheme.typography.labelMedium)
        }
        Text(user.city, color = Muted, style = MaterialTheme.typography.bodySmall)
        
        Spacer(Modifier.height(24.dp))
        
        // Trust Stats Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Trust Level: Good", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { 0.8f }, 
                    modifier = Modifier.fillMaxWidth().height(8.dp), 
                    color = CyanDeep,
                    trackColor = MaterialTheme.colorScheme.surface
                )
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("4.9", fontWeight = FontWeight.Bold)
                        Text("Rating", style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("12", fontWeight = FontWeight.Bold)
                        Text("Completed", style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("95%", fontWeight = FontWeight.Bold)
                        Text("Completion", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        
        if (user.skills.length > 2) {
            val skillsList = try { org.json.JSONArray(user.skills).let { arr -> List(arr.length()) { arr.getString(it) } } } catch (e: Exception) { emptyList() }
            if (skillsList.isNotEmpty()) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                    Text("Skills & Interests", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        skillsList.forEach { skill ->
                            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                Text(skill, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }

        // Menu items
        val menuItems = listOf(
            Triple(Icons.Outlined.AccountBalanceWallet, "Karma Wallet", "wallet"),
            Triple(Icons.Outlined.EmojiEvents, "Leaderboard", "leaderboard"),
            Triple(Icons.Outlined.Forum, "Ask & Answer", "ask"),
            Triple(Icons.Outlined.Map, "Live Pulse", "pulse"),
            Triple(Icons.Outlined.Verified, "Verify Identity", "verify"),
            Triple(Icons.Outlined.Star, "Karma & Reviews", "karma"),
            Triple(Icons.Outlined.Groups, "Community Meets", "meets"),
            Triple(Icons.Outlined.Settings, "Settings", "settings"),
            Triple(Icons.Outlined.Help, "Help & Support", "help"),
        )
        menuItems.forEach { (icon, label, route) ->
            Card(
                onClick = {
                    if (route == "meets" || route == "wallet" || route == "leaderboard" || route == "ask" || route == "pulse") {
                        navController.navigate(route)
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(12.dp))
                    Text(label, modifier = Modifier.weight(1f))
                    Icon(Icons.Outlined.ChevronRight, null, tint = Muted)
                }
            }
        }
        Spacer(Modifier.weight(1f))
        
        // Logout
        OutlinedButton(
            onClick = { onLogout() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Log Out", color = Error)
        }
    }
}

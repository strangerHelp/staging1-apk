package com.strangerhelp.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.strangerhelp.app.ui.theme.CyanDeep
import com.strangerhelp.app.ui.theme.Muted
import com.strangerhelp.app.ui.theme.OnPrimary
import com.strangerhelp.app.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComingSoonScreen(
    title: String,
    description: String,
    icon: String,
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = icon,
                    fontSize = 64.sp
                )
                Text(
                    text = "Coming Soon! \uD83D\uDE80",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Muted,
                    textAlign = TextAlign.Center
                )
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = CyanDeep.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "We're working hard to bring this feature to you. Stay tuned!",
                        fontSize = 12.sp,
                        color = Muted,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    )
                ) {
                    Text("Back to Profile", color = OnPrimary)
                }
            }
        }
    }
}

@Composable
fun ReferEarnScreen(
    navController: NavController
) {
    ComingSoonScreen(
        title = "Refer & Earn",
        description = "Invite your friends to StrangerHelp and earn rewards when they sign up and complete their first task!",
        icon = "\uD83C\uDF81", // Gift
        navController = navController
    )
}

@Composable
fun KarmaWalletScreen(
    navController: NavController
) {
    ComingSoonScreen(
        title = "Karma Wallet",
        description = "Track your karma points, rewards, and earnings all in one place. Complete tasks and build your reputation!",
        icon = "⭐",
        navController = navController
    )
}

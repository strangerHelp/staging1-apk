import sys

new_content = """package com.strangerhelp.app.ui.screens.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

val Primary = Color(0xFF171717)
val Body = Color(0xFF666666)
val Muted = Color(0xFF999999)
val Surface = Color.White
val SurfaceVariant = Color(0xFFF5F5F5)
val Hairline = Color(0xFFE5E5E5)
val TrustColor = Color(0xFF10B981)
val Warning = Color(0xFFEE0000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KarmaWalletScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Karma Wallet") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                BalanceCard(
                    title = "KARMA BALANCE",
                    value = "—",
                    subtitle = "karma",
                    description = "Karma Wallet is coming soon! Complete tasks and help the community to earn karma points.",
                    actions = listOf("Withdraw ₹", "Donate", "Transfer"),
                    badgeText = "COMING SOON"
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "1 Karma = ₹1 · Minimum withdrawal: 100 Karma · Instant UPI transfer",
                        fontSize = 12.sp,
                        color = Muted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            item {
                EarnSpendTables()
            }

            item {
                DonationCausesSection()
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun BalanceCard(
    title: String,
    value: String,
    subtitle: String,
    description: String,
    actions: List<String>,
    badgeText: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Primary),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            if (badgeText != null) {
                Badge(
                    text = badgeText,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = value,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )

                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    actions.forEach { action ->
                        Button(
                            onClick = { /* No-op */ },
                            enabled = false,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.1f),
                                disabledContainerColor = Color.White.copy(alpha = 0.05f)
                            ),
                            shape = RoundedCornerShape(26.dp)
                        ) {
                            Text(
                                text = action,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Badge(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF5A623),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun EarnSpendTables() {
    val earnData = listOf(
        "Complete a task" to "+10-50",
        "Answer a question" to "+5",
        "Get upvoted" to "+2",
        "Verify identity" to "+100",
        "Emergency help" to "+75"
    )

    val spendData = listOf(
        "Withdraw as ₹" to "1:1",
        "Boost your task" to "-20",
        "Priority matching" to "-15",
        "Donate to cause" to "Any",
        "Gift to user" to "Any"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = Surface),
            border = BorderStroke(1.dp, Hairline),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "💰 Earn Karma",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Primary
                )
                earnData.forEach { (label, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = label, fontSize = 12.sp, color = Body)
                        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TrustColor)
                    }
                }
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = Surface),
            border = BorderStroke(1.dp, Hairline),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "💎 Spend Karma",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Primary
                )
                spendData.forEach { (label, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = label, fontSize = 12.sp, color = Body)
                        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Warning)
                    }
                }
            }
        }
    }
}

@Composable
fun DonationCausesSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Hairline),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "🚀 Donation feature coming soon",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Primary
            )
            
            Text(
                text = "Support great causes using your Karma (Example):",
                fontSize = 12.sp,
                color = Body
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("🌿 Plant Trees", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text("₹500 / tree", fontSize = 12.sp, color = Muted)
                    }
                    Button(
                        onClick = { /* No-op */ },
                        enabled = false,
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Text("Donate")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferEarnScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val user by profileViewModel.user.collectAsState()
    val context = LocalContext.current

    val refCode = user?.handle ?: user?.name?.take(8) ?: "stranger"
    val refLink = "https://strangerhelp.com/register?ref=$refCode"
    val shareText = \"\"\"
        🎉 Join StrangerHelp!
        
        Post tasks or help strangers near you and earn money.
        Use my referral code: $refCode
        
        Sign up: $refLink
    \"\"\".trimIndent()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Refer & Earn") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ReferralCard(
                    refCode = refCode,
                    refLink = refLink,
                    shareText = shareText
                )
            }

            item {
                HowItWorksSection()
            }

            item {
                StatsRow(
                    invited = "—",
                    joined = "—",
                    earned = "—"
                )
            }

            item {
                YourReferralsSection()
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Rewards are subject to terms. Referral program will be live soon.",
                        fontSize = 11.sp,
                        color = Muted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ReferralCard(
    refCode: String,
    refLink: String,
    shareText: String
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Primary),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Badge(
                text = "COMING SOON",
                modifier = Modifier.align(Alignment.TopEnd)
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "REFERRAL EARNINGS",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = "₹0 earned",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Text(
                    text = "Invite friends and earn rewards when they complete their first task!",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Text(
                    text = "Your Referral Code",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = refCode,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Referral Code", refCode)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied!", Toast.LENGTH.SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy", color = Color.White, fontSize = 12.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { shareToWhatsApp(context, shareText) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366)
                        ),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Text("📱 WhatsApp", color = Color.White, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { shareGeneric(context, shareText) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Text("🔗 Share Link", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

fun shareToWhatsApp(context: Context, text: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage("com.whatsapp")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        shareGeneric(context, text)
    }
}

fun shareGeneric(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share"))
}

@Composable
fun HowItWorksSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Hairline),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📋 How Refer & Earn Works",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("1️⃣", fontSize = 24.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Share your referral code", fontSize = 11.sp, color = Body, textAlign = TextAlign.Center)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("2️⃣", fontSize = 24.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Friend signs up with your code", fontSize = 11.sp, color = Body, textAlign = TextAlign.Center)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("3️⃣", fontSize = 24.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("They complete their first task", fontSize = 11.sp, color = Body, textAlign = TextAlign.Center)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎁", fontSize = 24.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("You both earn rewards!", fontSize = 11.sp, color = TrustColor, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5A623).copy(alpha = 0.08f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "🚀 Rewards will be credited to your Karma Wallet — amounts announced at launch!",
                    fontSize = 11.sp,
                    color = Muted,
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun StatsRow(
    invited: String,
    joined: String,
    earned: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatItem("👥 Invited", invited, Modifier.weight(1f))
        StatItem("✅ Joined", joined, Modifier.weight(1f))
        StatItem("💰 Earned", earned, Modifier.weight(1f))
    }
}

@Composable
fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Muted)
            Text(text = label, fontSize = 11.sp, color = Muted)
        }
    }
}

@Composable
fun YourReferralsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Hairline),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "👤 Your Referrals",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Primary
            )

            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📭", fontSize = 32.sp)
                    Text("No referrals yet", fontSize = 14.sp, color = Body)
                    Text("Share your referral code to start earning!", fontSize = 12.sp, color = Muted)
                }
            }
        }
    }
}
"""

with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/ComingSoonScreens.kt", "w") as f:
    f.write(new_content)

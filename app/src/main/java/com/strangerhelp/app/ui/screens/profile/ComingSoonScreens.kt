package com.strangerhelp.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.strangerhelp.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComingSoonBanner() {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyanDeep.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🚀", fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Coming Soon!", fontWeight = FontWeight.Bold, color = CyanDeep, fontSize = 16.sp)
                Text("We are working on bringing these features to life.", fontSize = 12.sp, color = Muted)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferEarnScreen(navController: NavController) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ComingSoonBanner()
            
            Text("🎁", fontSize = 80.sp)
            Spacer(Modifier.height(16.dp))
            Text("Invite Friends,\nEarn Karma & Cash!", fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text("For every friend that signs up and completes their first task, you both get ₹50 and 100 Karma points.", 
                color = Muted, textAlign = TextAlign.Center, fontSize = 14.sp)
            
            Spacer(Modifier.height(32.dp))
            
            Text("Your Referral Code", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(8.dp))
            
            OutlinedTextField(
                value = "STRANGER50",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                trailingIcon = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.ContentCopy, "Copy", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                textStyle = LocalTextStyle.current.copy(
                    fontWeight = FontWeight.Bold, 
                    fontSize = 18.sp, 
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )
            )
            
            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = { /* TODO */ },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(26.dp)
            ) {
                Icon(Icons.Default.Share, "Share", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Share Invite Link", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(Modifier.height(40.dp))
            
            Text("Your Referrals", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(16.dp))
            
            Box(
                modifier = Modifier.fillMaxWidth().height(120.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("👥", fontSize = 32.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("No referrals yet.", color = Muted, fontSize = 14.sp)
                }
            }
        }
    }
}

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(24.dp)
        ) {
            ComingSoonBanner()
            
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CyanDeep),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Total Karma Balance", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⭐", fontSize = 32.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("1,250", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { /* TODO */ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = CyanDeep),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("Redeem Rewards", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            Text("Recent Transactions", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(16.dp))
            
            // Dummy transaction list
            val transactions = listOf(
                TransactionMock("Helped with luggage carrying", "+50", "Today, 10:30 AM", true),
                TransactionMock("Account Verification bonus", "+100", "Yesterday", true),
                TransactionMock("Redeemed Coffee Voucher", "-300", "Aug 20, 2026", false),
                TransactionMock("Completed survey task", "+20", "Aug 18, 2026", true)
            )
            
            transactions.forEach { tx ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(if (tx.isPositive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (tx.isPositive) "✨" else "🎁", fontSize = 20.sp)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(tx.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(tx.date, color = Muted, fontSize = 12.sp)
                    }
                    Text(
                        text = tx.amount, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 16.sp,
                        color = if (tx.isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

data class TransactionMock(val title: String, val amount: String, val date: String, val isPositive: Boolean)

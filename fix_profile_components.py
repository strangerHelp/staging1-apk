with open('app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt', 'r') as f:
    content = f.read()

# Add missing components at the end
missing_code = """
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
                    Text("Tasks\\nCompleted", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                }
                Divider(modifier = Modifier.width(1.dp).height(40.dp), color = Color(0xFFE0E0E0))
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("$completionRate%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(Modifier.height(4.dp))
                    Text("Completion\\nRate", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
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
"""

content = content.replace("    verified: Boolean", missing_code)

with open('app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt', 'w') as f:
    f.write(content)

import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt", "r") as f:
    content = f.read()

# Make sure imports exist for SurfaceVariant
if "SurfaceVariant" not in content:
    content = content.replace("import com.strangerhelp.app.ui.theme.*", "import com.strangerhelp.app.ui.theme.*\nimport androidx.compose.ui.text.style.TextAlign")

# Add VerificationViewModel to parameters
target_params = """fun ProfileScreen(
    navController: NavController,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {"""
replacement_params = """fun ProfileScreen(
    navController: NavController,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    verificationViewModel: VerificationViewModel = viewModel()
) {"""
content = content.replace(target_params, replacement_params)

# Add loadStatus call and state
target_states = """    val verificationMessage by authViewModel.verificationMessage.collectAsState()
    val verificationError by authViewModel.verificationError.collectAsState()"""
replacement_states = """    val verificationMessage by authViewModel.verificationMessage.collectAsState()
    val verificationError by authViewModel.verificationError.collectAsState()
    
    val verificationStatus by verificationViewModel.status.collectAsState()
    
    LaunchedEffect(Unit) {
        verificationViewModel.loadStatus()
    }"""
content = content.replace(target_states, replacement_states)

# Add the new QuickActionsRow
quick_actions = """
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
"""

if "QuickActionsRow(" not in content:
    content += quick_actions

# Inject the QuickActionsRow before the TrustStatsCard
target_quick_actions = """            // Trust Stats"""
replacement_quick_actions = """            // Quick Actions
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
            
            // Trust Stats"""
content = content.replace(target_quick_actions, replacement_quick_actions)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt", "w") as f:
    f.write(content)

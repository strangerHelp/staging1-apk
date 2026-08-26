import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt", "r") as f:
    content = f.read()

# Add AuthViewModel usage
import_auth = "import com.strangerhelp.app.ui.components.EmailVerificationBanner\nimport com.strangerhelp.app.ui.screens.profile.AuthViewModel\n"
content = content.replace("import com.strangerhelp.app.ui.theme.*", "import com.strangerhelp.app.ui.theme.*\n" + import_auth)

# Add AuthViewModel to parameters or instantiate
signature_replace = """fun ProfileScreen(
    navController: NavController,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {"""
content = re.sub(r'fun ProfileScreen\(\s*navController: NavController,\s*onLogout: \(\) -> Unit,\s*viewModel: ProfileViewModel = viewModel\(\)\s*\) \{', signature_replace, content)

# Add the states
states_replace = """    val isLoading by viewModel.isLoading.collectAsState()
    
    val isSendingVerification by authViewModel.isSendingVerification.collectAsState()
    val verificationMessage by authViewModel.verificationMessage.collectAsState()
    val verificationError by authViewModel.verificationError.collectAsState()"""
content = content.replace("val isLoading by viewModel.isLoading.collectAsState()", states_replace)

# Replace the existing email verification UI (which was false/true based)
existing_banner = """            if (user?.emailVerified == false) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Warning)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Please verify your email address to unlock all features.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.resendVerificationEmail() }) {
                            Text("Resend", color = Primary)
                        }
                    }
                }
            }"""

new_banner = """            if (user?.emailVerified == 0) {
                EmailVerificationBanner(
                    onResendClick = { authViewModel.resendVerificationEmail() },
                    isSending = isSendingVerification,
                    message = verificationMessage,
                    error = verificationError
                )
            }"""

if existing_banner in content:
    content = content.replace(existing_banner, new_banner)
else:
    # If it's different, let's use regex
    content = re.sub(r'            if \(user\?\.emailVerified == false\) \{.*?\n            \}', new_banner, content, flags=re.DOTALL)

# Replace the verified icon logic
content = content.replace('user?.emailVerified == true', 'user?.emailVerified == 1')

with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt", "w") as f:
    f.write(content)

import os

theme_path = 'app/src/main/java/com/strangerhelp/app/ui/theme/Theme.kt'
with open(theme_path, 'r') as f:
    theme_content = f.read()

theme_content = theme_content.replace(
    'darkTheme: Boolean = isSystemInDarkTheme(),',
    'darkTheme: Boolean = false, // Forced white/light theme'
)
with open(theme_path, 'w') as f:
    f.write(theme_content)


login_path = 'app/src/main/java/com/strangerhelp/app/ui/screens/auth/LoginScreen.kt'
with open(login_path, 'r') as f:
    login_content = f.read()

# Replace background
login_content = login_content.replace(
    'modifier = Modifier\n            .fillMaxSize()\n            .background(MaterialTheme.colorScheme.background)',
    'modifier = Modifier\n            .fillMaxSize()\n            .background(Color.White)'
)

# Update Logo to include the text and match the design
old_logo = '''                // Logo
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 16.dp)) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(72.dp)
                    )
                    Icon(
                        imageVector = Icons.Outlined.Handshake,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary, // Saffron
                        modifier = Modifier.size(36.dp).padding(bottom = 8.dp)
                    )
                }'''

new_logo = '''                // Logo
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 16.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(96.dp)
                        )
                        Icon(
                            imageVector = Icons.Outlined.Handshake,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(48.dp).padding(bottom = 12.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    androidx.compose.ui.text.buildAnnotatedString {
                        androidx.compose.ui.text.withStyle(androidx.compose.ui.text.SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 32.sp)) {
                            append("stranger")
                        }
                        androidx.compose.ui.text.withStyle(androidx.compose.ui.text.SpanStyle(color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 32.sp)) {
                            append("help")
                        }
                    }.let { text ->
                        Text(text = text)
                    }
                }'''

login_content = login_content.replace(old_logo, new_logo)

# Add missing imports if needed
if 'import androidx.compose.ui.text.withStyle' not in login_content:
    login_content = login_content.replace('import androidx.compose.ui.text.style.TextAlign', 'import androidx.compose.ui.text.style.TextAlign\nimport androidx.compose.ui.text.withStyle\nimport androidx.compose.ui.text.buildAnnotatedString')

# Adjust surface color
login_content = login_content.replace(
    'color = MaterialTheme.colorScheme.surface,',
    'color = Color.White,'
)

with open(login_path, 'w') as f:
    f.write(login_content)

print("Patched Theme and LoginScreen")

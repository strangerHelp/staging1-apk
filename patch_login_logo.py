import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/auth/LoginScreen.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

target = '''                    Box(contentAlignment = Alignment.Center) {
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
                    }'''
                    
replacement = '''                    com.strangerhelp.app.ui.components.StrangerHelpLogo(size = 96.dp)'''

content = content.replace(target, replacement)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Updated LoginScreen logo")

import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatDetailScreen.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

target = '''                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                            Icon(Icons.Outlined.Handshake, contentDescription = null, tint = Saffron, modifier = Modifier.size(12.dp).padding(bottom = 2.dp))
                        }'''

replacement = '''                        com.strangerhelp.app.ui.components.StrangerHelpLogo(size = 28.dp)'''

content = content.replace(target, replacement)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Updated ChatDetailScreen logo")

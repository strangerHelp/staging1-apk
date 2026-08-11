import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt'
with open(path, 'r') as f:
    content = f.read()

top_bar = '''        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Outlined.Menu, contentDescription = "Menu", tint = Primary)
            }
            
            // Logo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.LocationOn, contentDescription = "Logo", tint = Primary, modifier = Modifier.size(28.dp))
                    Icon(Icons.Outlined.Handshake, contentDescription = null, tint = Saffron, modifier = Modifier.size(14.dp).padding(bottom = 2.dp))
                }
                Spacer(Modifier.width(4.dp))
                androidx.compose.ui.text.buildAnnotatedString {
                    androidx.compose.ui.text.withStyle(androidx.compose.ui.text.SpanStyle(color = Primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)) { append("stranger") }
                    androidx.compose.ui.text.withStyle(androidx.compose.ui.text.SpanStyle(color = Saffron, fontWeight = FontWeight.Bold, fontSize = 16.sp)) { append("help") }
                }.let { text ->
                    Text(text = text)
                }
            }
            
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = Primary)
            }
        }
        
        Spacer(Modifier.height(16.dp))'''

content = content.replace('        Spacer(Modifier.height(24.dp))\n        \n        // Avatar', top_bar + '\n        // Avatar')

with open(path, 'w') as f:
    f.write(content)
print("Added TopBar to ProfileScreen")

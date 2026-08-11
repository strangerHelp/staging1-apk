import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt'
with open(path, 'r') as f:
    content = f.read()

avatar_target = '''        // Avatar
        Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.size(96.dp)) {
            Surface(modifier = Modifier.fillMaxSize(), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        user.name.take(2).uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            // Cyan dot'''

avatar_replacement = '''        // Avatar
        Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.size(96.dp)) {
            if (user.avatar.isNotBlank()) {
                coil.compose.AsyncImage(
                    model = user.avatar,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Surface(modifier = Modifier.fillMaxSize(), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            user.name.take(2).uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            // Cyan dot'''

content = content.replace(avatar_target, avatar_replacement)

with open(path, 'w') as f:
    f.write(content)
print("Updated ProfileScreen avatar")

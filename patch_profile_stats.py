import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt'
with open(path, 'r') as f:
    content = f.read()

target = '''        // Trust Stats Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Trust Level: Good", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { 0.8f }, 
                    modifier = Modifier.fillMaxWidth().height(8.dp), 
                    color = CyanDeep,
                    trackColor = MaterialTheme.colorScheme.surface
                )
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("4.9", fontWeight = FontWeight.Bold)
                        Text("Rating", style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("12", fontWeight = FontWeight.Bold)
                        Text("Completed", style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("95%", fontWeight = FontWeight.Bold)
                        Text("Completion", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }'''

replacement = '''        // Trust Stats Card
        com.strangerhelp.app.ui.components.UserProfileStatsCard()'''

if target in content:
    content = content.replace(target, replacement)
    with open(path, 'w') as f:
        f.write(content)
    print("Patched.")
else:
    print("Not found.")

import re

with open('app/src/main/java/com/strangerhelp/app/ui/screens/pulse/PulseScreen.kt', 'r') as f:
    content = f.read()

target = """            if (downloadProgress != null) {
                LinearProgressIndicator(
                    progress = { downloadProgress!! / 100f },
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(top = 16.dp, start = 16.dp, end = 16.dp),
                    color = Saffron
                )
            }
            AndroidView("""
            
replacement = """            AndroidView("""

content = content.replace(target, replacement, 1)

target2 = """            // Map Controls"""
replacement2 = """            if (downloadProgress != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Downloading Offline Map...", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { downloadProgress!! / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = Saffron
                        )
                    }
                }
            }

            // Map Controls"""
content = content.replace(target2, replacement2, 1)

with open('app/src/main/java/com/strangerhelp/app/ui/screens/pulse/PulseScreen.kt', 'w') as f:
    f.write(content)

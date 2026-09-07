import os

file_path = "app/src/main/java/com/strangerhelp/app/ui/screens/LandingScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

target = """                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.LocationOn, contentDescription = "Logo", tint = DarkNavy, modifier = Modifier.size(24.dp))
                        Icon(Icons.Outlined.Handshake, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(12.dp).padding(bottom = 2.dp))
                    }"""

replacement = """                    com.strangerhelp.app.ui.components.StrangerHelpLogo(size = 24.dp)"""

if target in content:
    content = content.replace(target, replacement)
    with open(file_path, "w") as f:
        f.write(content)
    print("Success LandingScreen bottom")
else:
    print("Target not found in LandingScreen bottom")

import os

file_path = "app/src/main/java/com/strangerhelp/app/ui/screens/LandingScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

target = """                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Logo",
                        tint = DarkNavy,
                        modifier = Modifier.size(32.dp)
                    )
                    Icon(
                        imageVector = Icons.Outlined.Handshake,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(16.dp).padding(bottom = 4.dp)
                    )
                }"""

replacement = """                com.strangerhelp.app.ui.components.StrangerHelpLogo(size = 32.dp)"""

if target in content:
    content = content.replace(target, replacement)
    with open(file_path, "w") as f:
        f.write(content)
    print("Success LandingScreen")
else:
    print("Target not found in LandingScreen")

import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/LandingScreen.kt'
with open(path, 'r') as f:
    content = f.read()

content = content.replace('val OrangePrimary = Color(0xFFF9A826)', 'val OrangePrimary = Color(0xFFF5A623)')
content = content.replace('val DarkNavy = Color(0xFF1B202D)', 'val DarkNavy = Color(0xFF101828)')
content = content.replace('val LightBeige = Color(0xFFF8F7F2)', 'val LightBeige = Color(0xFFFAF9F6)')
content = content.replace('val TextDark = Color(0xFF111111)', 'val TextDark = Color(0xFF101828)')

with open(path, 'w') as f:
    f.write(content)
print("Patched colors in LandingScreen")

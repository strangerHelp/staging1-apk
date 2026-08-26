import re

code = """
val PrimaryColor = Color(0xFFF59E0B) // Amber
val BackgroundColor = Color(0xFFF9F9F9)
val CardOutlineColor = Color(0xFFE5E5E5)
val TextColor = Color(0xFF111111)
val MutedText = Color(0xFF666666)
val BlueBg = Color(0xFFEBF8FF)
val CyanColor = Color(0xFF009688)
val CyanDeep = Color(0xFF00796B)
val Primary = Color(0xFFF59E0B)
val Error = Color(0xFFD32F2F)
val Warning = Color(0xFFF57C00)
"""

with open('app/src/main/java/com/strangerhelp/app/ui/screens/path/PathSetupScreen.kt', 'r') as f:
    content = f.read()

content = re.sub(r'val PrimaryColor.*?val BlueBg = Color\(0xFFEBF8FF\)', code.strip(), content, flags=re.DOTALL)

with open('app/src/main/java/com/strangerhelp/app/ui/screens/path/PathSetupScreen.kt', 'w') as f:
    f.write(content)

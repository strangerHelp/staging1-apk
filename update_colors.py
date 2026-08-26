import re

code = """
val CyanColor = Color(0xFF009688)
val CyanDeep = Color(0xFF00796B)
val Primary = Color(0xFFF59E0B)
val PrimaryColor = Color(0xFFF59E0B)
val BackgroundColor = Color(0xFFF9F9F9)
val CardOutlineColor = Color(0xFFE5E5E5)
val TextColor = Color(0xFF111111)
val MutedText = Color(0xFF666666)
val BlueBg = Color(0xFFEBF8FF)
val Error = Color(0xFFD32F2F)
val Warning = Color(0xFFF57C00)
"""

with open('app/src/main/java/com/strangerhelp/app/ui/screens/path/PathActiveScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("val CyanColor = Color(0xFF009688)", code)

with open('app/src/main/java/com/strangerhelp/app/ui/screens/path/PathActiveScreen.kt', 'w') as f:
    f.write(content)

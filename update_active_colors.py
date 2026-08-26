import re

with open('app/src/main/java/com/strangerhelp/app/ui/screens/path/PathActiveScreen.kt', 'r') as f:
    content = f.read()

colors_to_remove = [
    "val CyanColor = Color(0xFF009688)\n",
    "val CyanDeep = Color(0xFF00796B)\n",
    "val Primary = Color(0xFFF59E0B)\n",
    "val PrimaryColor = Color(0xFFF59E0B)\n",
    "val BackgroundColor = Color(0xFFF9F9F9)\n",
    "val CardOutlineColor = Color(0xFFE5E5E5)\n",
    "val TextColor = Color(0xFF111111)\n",
    "val MutedText = Color(0xFF666666)\n",
    "val BlueBg = Color(0xFFEBF8FF)\n",
    "val Error = Color(0xFFD32F2F)\n",
    "val Warning = Color(0xFFF57C00)\n"
]

for c in colors_to_remove:
    content = content.replace(c, "")

with open('app/src/main/java/com/strangerhelp/app/ui/screens/path/PathActiveScreen.kt', 'w') as f:
    f.write(content)

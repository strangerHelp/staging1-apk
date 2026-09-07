import os
import re

filepath = "app/src/main/java/com/strangerhelp/app/ui/screens/LandingScreen.kt"
with open(filepath, "r") as f:
    content = f.read()

# Update sizes to match user's desktop request closer (40dp logo, 28sp text)
content = content.replace("StrangerHelpHeader(logoSize = 32.dp, textSize = 20.sp)", "StrangerHelpHeader(logoSize = 40.dp, textSize = 28.sp)")
content = content.replace("StrangerHelpHeader(logoSize = 24.dp, textSize = 16.sp)", "StrangerHelpHeader(logoSize = 32.dp, textSize = 22.sp)")

with open(filepath, "w") as f:
    f.write(content)

print("Updated header sizes")

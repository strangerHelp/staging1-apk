import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt", "r") as f:
    content = f.read()

old_actions_pattern = r"            // Quick Actions\s*Row\(modifier = Modifier\.fillMaxWidth\(\), horizontalArrangement = Arrangement\.spacedBy\(8\.dp\)\) \{\s*QuickActionCard[^\}]+\}\s*"

content = re.sub(old_actions_pattern, "", content, count=1)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt", "w") as f:
    f.write(content)

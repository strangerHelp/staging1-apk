import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt'
with open(path, 'r') as f:
    content = f.read()

target = '''        Spacer(Modifier.height(24.dp))
                
        if (user.skills.length > 2) {'''

replacement = '''        Spacer(Modifier.height(24.dp))
        
        com.strangerhelp.app.ui.components.DashboardChart()
        Spacer(Modifier.height(24.dp))
                
        if (user.skills.length > 2) {'''

if target in content:
    content = content.replace(target, replacement)
    with open(path, 'w') as f:
        f.write(content)
        print("ProfileScreen patched.")
else:
    print("Target not found.")

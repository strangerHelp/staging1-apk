with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt", "r") as f:
    content = f.read()

content = content.replace('Icon(Icons.Default.StarBorder, contentDescription = null, tint = Color(0xFFFBC02D)', 'Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBC02D)')

with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt", "w") as f:
    f.write(content)

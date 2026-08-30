with open('app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    "    trustScore: Int,\\n\\n) {", 
    "    trustScore: Int,\\n    verified: Boolean\\n) {"
)

with open('app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt', 'w') as f:
    f.write(content)

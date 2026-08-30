with open('app/src/main/java/com/strangerhelp/app/ui/theme/Theme.kt', 'r') as f:
    content = f.read()

content = content.replace("outline = Color(0xFF999999),", "outline = Color(0xFF737373),")
content = content.replace("outlineVariant = Color(0xFFB3B3B3),", "outlineVariant = Color(0xFF8C8C8C),")

with open('app/src/main/java/com/strangerhelp/app/ui/theme/Theme.kt', 'w') as f:
    f.write(content)

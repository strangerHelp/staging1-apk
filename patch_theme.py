with open('app/src/main/java/com/strangerhelp/app/ui/theme/Theme.kt', 'r') as f:
    content = f.read()

content = content.replace("outline = Hairline,", "outline = Color(0xFF999999),")
content = content.replace("outlineVariant = Hairline,", "outlineVariant = Color(0xFFB3B3B3),")

with open('app/src/main/java/com/strangerhelp/app/ui/theme/Theme.kt', 'w') as f:
    f.write(content)

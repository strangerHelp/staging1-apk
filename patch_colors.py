with open('app/src/main/java/com/strangerhelp/app/ui/theme/Color.kt', 'r') as f:
    content = f.read()

content = content.replace("val Hairline = Color(0xFFEBEBEB)", "val Hairline = Color(0xFFCCCCCC)")
content = content.replace("val SurfaceVariant = Color(0xFFF0F0F0)", "val SurfaceVariant = Color(0xFFF5F5F5)")

with open('app/src/main/java/com/strangerhelp/app/ui/theme/Color.kt', 'w') as f:
    f.write(content)


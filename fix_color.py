import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatDetailScreen.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('Color(0xFF9CA3AF)', 'Color(0xFF64748B)')
with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Fixed text color")

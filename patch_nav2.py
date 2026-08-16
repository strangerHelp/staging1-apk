import os

path = 'app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

target = '''unreadCount = res.body()?.notifications?.count { !it.read } ?: 0'''
replacement = '''unreadCount = res.body()?.unreadCount ?: 0'''
content = content.replace(target, replacement)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Updated AppNavigation unread logic")

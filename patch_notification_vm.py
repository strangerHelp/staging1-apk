import re

file_path = "app/src/main/java/com/strangerhelp/app/ui/screens/notifications/NotificationViewModel.kt"
with open(file_path, "r") as f:
    content = f.read()

init_block = """    init {
        startPolling()
    }

    // ⭐ Load notifications"""

if "init {" not in content:
    content = content.replace('    // ⭐ Load notifications', init_block)

with open(file_path, "w") as f:
    f.write(content)

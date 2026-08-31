with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "r") as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "posterId" in line or "currentUser" in line:
        print(f"{i+1}: {line.strip()}")

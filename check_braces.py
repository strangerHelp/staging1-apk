with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "r") as f:
    lines = f.readlines()

brace_count = 0
for i, line in enumerate(lines):
    brace_count += line.count('{')
    brace_count -= line.count('}')
    if brace_count < 0:
        print(f"Negative brace count at line {i+1}: {line}")
        break

print(f"Final brace count: {brace_count}")

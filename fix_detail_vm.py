import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "r") as f:
    content = f.read()

# Delete submitReview method
content = re.sub(r'    fun submitReview\([^)]+\)\s*\{\s*viewModelScope\.launch\s*\{\s*try\s*\{[^\}]+\}\s*catch\s*\([^\)]+\)\s*\{\s*\_error\.value\s*=\s*"[^"]+"\s*\}\s*\}\s*\}', '', content)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "w") as f:
    f.write(content)

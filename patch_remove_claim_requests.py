import sys
import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "r") as f:
    content = f.read()

# Use regex to remove fun ClaimRequestsSection(task: Task, viewModel: TaskDetailViewModel) { ... }
content = re.sub(r'@Composable\nfun ClaimRequestsSection\(task: Task, viewModel: TaskDetailViewModel\) \{.*', '', content, flags=re.DOTALL)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "w") as f:
    f.write(content)

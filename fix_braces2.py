import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
    "                    // 10. Payment Notice\n                    item { PaymentNotice(t.budget) }\n                }\n            }\n",
    "                    // 10. Payment Notice\n                    item { PaymentNotice(t.budget) }\n                }\n            }\n        }\n"
)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "w") as f:
    f.write(content)

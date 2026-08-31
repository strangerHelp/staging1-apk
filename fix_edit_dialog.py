import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/PosterComponents.kt", "r") as f:
    content = f.read()

content = content.replace(
    "onSave: (title: String, description: String, budget: String) -> Unit",
    "onSave: (Map<String, Any>) -> Unit"
)

content = content.replace(
    "onClick = { onSave(title, description, budget) }",
    "onClick = { onSave(mapOf(\"title\" to title, \"description\" to description, \"budget\" to (budget.toIntOrNull() ?: 0))) }"
)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/PosterComponents.kt", "w") as f:
    f.write(content)

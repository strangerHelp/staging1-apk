with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/PosterComponents.kt", "r") as f:
    content = f.read()

content = content.replace("onSubmit: (title: String, description: String, budget: String) -> Unit", "onSave: (title: String, description: String, budget: String) -> Unit")
content = content.replace("onClick = { onSubmit(title, description, budget) }", "onClick = { onSave(title, description, budget) }")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/PosterComponents.kt", "w") as f:
    f.write(content)

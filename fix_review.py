with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "r") as f:
    content = f.read()

content = content.replace(
    '// Needs viewModel.submitReview',
    'viewModel.submitReview(task._id, task.claimedBy ?: "", rating, comment)'
)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "w") as f:
    f.write(content)

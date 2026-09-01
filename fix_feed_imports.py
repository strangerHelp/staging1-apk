import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/feed/FeedScreen.kt", "r") as f:
    content = f.read()

content = content.replace("import com.strangerhelp.app.data.model.Task", "import com.strangerhelp.app.data.model.Task\nimport com.strangerhelp.app.ui.components.TaskCardSkeleton\nimport com.strangerhelp.app.ui.components.EmptyState")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/feed/FeedScreen.kt", "w") as f:
    f.write(content)

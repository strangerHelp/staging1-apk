import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/feed/FeedScreen.kt", "r") as f:
    content = f.read()

# Add import
if "import com.strangerhelp.app.ui.components.TaskCardSkeleton" not in content:
    content = content.replace("import com.strangerhelp.app.ui.theme.*", "import com.strangerhelp.app.ui.theme.*\nimport com.strangerhelp.app.ui.components.TaskCardSkeleton\nimport com.strangerhelp.app.ui.components.EmptyState")

old_loading = """        if (isLoading) {
            Text("Loading...", color = MutedText, fontSize = 14.sp)
        } else {"""
new_loading = """        if (isLoading) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { TaskCardSkeleton() }
            }
        } else {"""

content = content.replace(old_loading, new_loading)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/feed/FeedScreen.kt", "w") as f:
    f.write(content)

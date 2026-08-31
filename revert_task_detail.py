import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "r") as f:
    text = f.read()

bad_block = """                    
                    if (currentUser?.id == t.posterId) {
                        item {
                            PosterTrackingView(task = t)
                        }
                    }
"""

text = text.replace(bad_block, "")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "w") as f:
    f.write(text)

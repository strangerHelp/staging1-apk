import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "r") as f:
    text = f.read()

stepper_block = """                    // 3.5. Task Lifecycle Stepper
                    item { 
                        Spacer(Modifier.height(16.dp))
                        TaskLifecycleStepper(t, currentUser?.id) 
                    }"""

new_stepper_block = stepper_block + """
                    
                    if (currentUser?.id == t.posterId) {
                        item {
                            PosterTrackingView(task = t)
                        }
                    }
"""

text = text.replace(stepper_block, new_stepper_block)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "w") as f:
    f.write(text)

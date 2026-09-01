import re

# Fix ReviewComponents.kt
with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/ReviewComponents.kt", "r") as f:
    content = f.read()

content = content.replace("Modifier.size(14.sp)", "Modifier.size(14.dp)")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/ReviewComponents.kt", "w") as f:
    f.write(content)

# Fix TaskDetailScreen.kt
with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "r") as f:
    content = f.read()

content = content.replace("val reviews by reviewViewModel.taskReviews.collectAsState()", "val reviews by reviewViewModel.taskReviews.collectAsStateWithLifecycle()")
content = content.replace("Text(\n                                        \"📋 Reviews (${reviews?.totalReviews ?: 0})\",\n                                        androidx.compose.ui.text.font.FontWeight.SemiBold,\n                                        fontSize = 14.sp\n                                    )", "Text(\n                                        text = \"📋 Reviews (${reviews?.totalReviews ?: 0})\",\n                                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,\n                                        fontSize = 14.sp\n                                    )")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "w") as f:
    f.write(content)

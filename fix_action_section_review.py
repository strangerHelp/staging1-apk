import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "r") as f:
    content = f.read()

# For HELPER_COMPLETED
helper_completed_old = """                Button(onClick = onReview, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)) {
                    Text("⭐ Leave a Review")
                }
                Spacer(Modifier.height(16.dp))"""
content = content.replace(helper_completed_old, "")

# For POSTER_COMPLETED
poster_completed_old = """                PosterReviewSection(
                    task = task,
                    onReviewSubmit = { rating, comment ->
                        viewModel.submitReview(task._id, task.claimedBy ?: "", rating, comment)
                    }
                )
                Spacer(Modifier.height(16.dp))"""
content = content.replace(poster_completed_old, "")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "w") as f:
    f.write(content)

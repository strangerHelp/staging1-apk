import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "r") as f:
    content = f.read()

# Add hiltViewModel() import if not exists
if "import androidx.hilt.navigation.compose.hiltViewModel" not in content:
    content = content.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\nimport androidx.hilt.navigation.compose.hiltViewModel")

# Add reviewViewModel declaration
old_declaration = """fun TaskDetailScreen(
    taskId: String,
    viewModel: TaskDetailViewModel,
    navController: NavController
) {"""
new_declaration = """fun TaskDetailScreen(
    taskId: String,
    viewModel: TaskDetailViewModel,
    navController: NavController,
    reviewViewModel: ReviewViewModel = hiltViewModel()
) {"""
content = content.replace(old_declaration, new_declaration)

# Replace the dialogs logic
old_dialogs = """            if (showReviewDialog) {
                ReviewDialog(
                    onDismiss = { showReviewDialog = false },
                    onConfirm = { rating, comment ->
                        task?.let {
                            val revieweeId = if (currentUser?.id == it.posterId) it.claimedBy ?: "" else it.posterId
                            viewModel.submitReview(it._id, revieweeId, rating, comment)
                        }
                        showReviewDialog = false
                    }
                )
            }"""
content = content.replace(old_dialogs, "")

# Find the end of LazyColumn where we add Review items
# item { PaymentNotice(t.budget) }
# }

new_items = """                    // 10. Payment Notice
                    item { PaymentNotice(t.budget) }

                    if (t.status == "completed") {
                        val isParticipant = currentUser?.id == t.posterId || currentUser?.id == t.claimedBy

                        if (isParticipant && currentUser != null) {
                            item {
                                ReviewPrompt(
                                    task = t,
                                    viewModel = reviewViewModel
                                )
                            }
                        }

                        item {
                            val reviews by reviewViewModel.taskReviews.collectAsState()
                            LaunchedEffect(Unit) {
                                reviewViewModel.loadTaskReviews(t._id)
                            }

                            if (reviews?.reviews?.isNotEmpty() == true) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "📋 Reviews (${reviews?.totalReviews ?: 0})",
                                        androidx.compose.ui.text.font.FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    ReviewList(reviews = reviews?.reviews ?: emptyList())
                                }
                            }
                        }
                    }"""

content = content.replace("                    // 10. Payment Notice\n                    item { PaymentNotice(t.budget) }", new_items)

# Add imports for ReviewPrompt and ReviewList if needed (they are in the same package so shouldn't need import)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "w") as f:
    f.write(content)

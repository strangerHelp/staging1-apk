with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "r") as f:
    content = f.read()

content = content.replace(
"""            }                Spacer(Modifier.height(16.dp))
                HelperProofGallery(task.completionProof)
            TaskUiState.HELPER_PROOF_REJECTED -> {""",
"""
                Spacer(Modifier.height(16.dp))
                HelperProofGallery(task.completionProof)
            }
            TaskUiState.HELPER_PROOF_REJECTED -> {"""
)

content = content.replace(
"""            }                Spacer(Modifier.height(16.dp))
                HelperProofGallery(task.completionProof)
            TaskUiState.HELPER_COMPLETED -> {""",
"""
                Spacer(Modifier.height(16.dp))
                HelperProofGallery(task.completionProof)
            }
            TaskUiState.HELPER_COMPLETED -> {"""
)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "w") as f:
    f.write(content)

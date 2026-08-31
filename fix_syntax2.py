with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "r") as f:
    content = f.read()

# First, remove all improperly placed Spacer and HelperProofGallery lines
content = content.replace("            }                Spacer(Modifier.height(16.dp))\n                HelperProofGallery(task.completionProof)\n", "            }\n")
content = content.replace("            }                Spacer(Modifier.height(16.dp))\n                HelperProofGallery(task.completionProof)", "            }\n")

# Now re-add them properly at the END of each block.
import re

# HelperProofPending:
content = re.sub(
    r'(TaskUiState\.HELPER_PROOF_PENDING -> \{[\s\S]*?)(\n            \}\n            TaskUiState\.HELPER_PROOF_REJECTED -> \{)',
    r'\1\n                Spacer(Modifier.height(16.dp))\n                HelperProofGallery(task.completionProof)\2',
    content
)

# HelperProofRejected:
content = re.sub(
    r'(TaskUiState\.HELPER_PROOF_REJECTED -> \{[\s\S]*?)(\n            \}\n            TaskUiState\.HELPER_COMPLETED -> \{)',
    r'\1\n                Spacer(Modifier.height(16.dp))\n                HelperProofGallery(task.completionProof)\2',
    content
)

# HelperCompleted:
content = re.sub(
    r'(TaskUiState\.HELPER_COMPLETED -> \{[\s\S]*?)(\n            \}\n\s*TaskUiState\.POSTER_WAITING -> \{)',
    r'\1\n                Spacer(Modifier.height(16.dp))\n                HelperProofGallery(task.completionProof)\2',
    content
)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "w") as f:
    f.write(content)

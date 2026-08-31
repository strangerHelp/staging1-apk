with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "r") as f:
    content = f.read()

import re

# Insert HelperProofGallery for HELPER_PROOF_PENDING
content = re.sub(
    r'(TaskUiState\.HELPER_PROOF_PENDING -> \{[\s\S]*?)(            TaskUiState\.HELPER_PROOF_REJECTED -> \{)',
    r'\1                Spacer(Modifier.height(16.dp))\n                HelperProofGallery(task.completionProof)\n\2',
    content
)

# Insert HelperProofGallery for HELPER_PROOF_REJECTED
content = re.sub(
    r'(TaskUiState\.HELPER_PROOF_REJECTED -> \{[\s\S]*?Button\([\s\S]*?\{[\s\S]*?\}\n)(            TaskUiState\.HELPER_COMPLETED -> \{)',
    r'\1                Spacer(Modifier.height(16.dp))\n                HelperProofGallery(task.completionProof)\n\2',
    content
)

# Insert HelperProofGallery for HELPER_COMPLETED
content = re.sub(
    r'(TaskUiState\.HELPER_COMPLETED -> \{[\s\S]*?Button\([\s\S]*?\{[\s\S]*?\}\n)(            TaskUiState\.POSTER_WAITING -> \{)',
    r'\1                Spacer(Modifier.height(16.dp))\n                HelperProofGallery(task.completionProof)\n\2',
    content
)


with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "w") as f:
    f.write(content)
print("Updated TaskActionSection.kt")

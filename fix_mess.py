with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "r") as f:
    text = f.read()

text = text.replace("                Spacer(Modifier.height(16.dp))\n                HelperProofGallery(task.completionProof)\n", "")

# Now add them once
import re
text = re.sub(
    r'(TaskUiState\.HELPER_PROOF_PENDING -> \{[\s\S]*?)(            \})',
    r'\1                Spacer(Modifier.height(16.dp))\n                HelperProofGallery(task.completionProof)\n\2',
    text,
    count=1
)

text = re.sub(
    r'(TaskUiState\.HELPER_PROOF_REJECTED -> \{[\s\S]*?)(            \})',
    r'\1                Spacer(Modifier.height(16.dp))\n                HelperProofGallery(task.completionProof)\n\2',
    text,
    count=1
)

text = re.sub(
    r'(TaskUiState\.HELPER_COMPLETED -> \{[\s\S]*?)(            \})',
    r'\1                Spacer(Modifier.height(16.dp))\n                HelperProofGallery(task.completionProof)\n\2',
    text,
    count=1
)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "w") as f:
    f.write(text)

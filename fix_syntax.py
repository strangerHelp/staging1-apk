with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "r") as f:
    content = f.read()

import re

# We placed it outside the blocks. We need to move it inside.
content = content.replace("            }                Spacer(Modifier.height(16.dp))\n                HelperProofGallery(task.completionProof)\n            TaskUiState.HELPER_PROOF_REJECTED -> {", "                Spacer(Modifier.height(16.dp))\n                HelperProofGallery(task.completionProof)\n            }\n            TaskUiState.HELPER_PROOF_REJECTED -> {")

content = content.replace("                }\n            }                Spacer(Modifier.height(16.dp))\n                HelperProofGallery(task.completionProof)\n            TaskUiState.HELPER_COMPLETED -> {", "                }\n                Spacer(Modifier.height(16.dp))\n                HelperProofGallery(task.completionProof)\n            }\n            TaskUiState.HELPER_COMPLETED -> {")

content = content.replace("                }\n            }                Spacer(Modifier.height(16.dp))\n                HelperProofGallery(task.completionProof)\n                        TaskUiState.POSTER_WAITING -> {", "                }\n                Spacer(Modifier.height(16.dp))\n                HelperProofGallery(task.completionProof)\n            }\n            TaskUiState.POSTER_WAITING -> {")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "w") as f:
    f.write(content)

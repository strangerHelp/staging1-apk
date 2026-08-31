import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/ProofGallery.kt", "r") as f:
    content = f.read()

content = content.replace("OutlinedButtonDefaults.outlinedButtonColors(", "ButtonDefaults.outlinedButtonColors(")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/ProofGallery.kt", "w") as f:
    f.write(content)

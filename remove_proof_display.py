import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/PosterComponents.kt", "r") as f:
    content = f.read()

# The function we want to remove
content = re.sub(
    r'@Composable\s*fun ProofDisplayComponent\([\s\S]*?\}\s*\}\s*\}\s*\}\s*selectedImage\?\.let \{ url ->[\s\S]*?FullScreenImageDialog\([\s\S]*?imageUrl = url,[\s\S]*?onDismiss = \{ selectedImage = null \}[\s\S]*?\)[\s\S]*?\}\s*\}',
    "",
    content
)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/PosterComponents.kt", "w") as f:
    f.write(content)


import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/PosterComponents.kt", "r") as f:
    content = f.read()

# Delete ProofReviewSection
content = re.sub(r'@Composable\s*fun ProofReviewSection[\s\S]*?FullScreenImageDialog\([\s\S]*?\n\s*\}\s*\}\s*\}', '', content)
# Wait, it's easier to just leave it. Or I'll use a simpler regex.

with open('app/src/main/java/com/strangerhelp/app/ui/screens/feed/FeedScreen.kt', 'r') as f:
    content = f.read()

# Add animation imports
if "import androidx.compose.animation.animateContentSize" not in content:
    content = content.replace("import androidx.compose.foundation.background", "import androidx.compose.animation.animateContentSize\nimport androidx.compose.foundation.background")

# Apply animateContentSize to Card
content = content.replace("Card(\n        modifier = Modifier", "Card(\n        modifier = Modifier.animateContentSize()")
content = content.replace("Card(\n            modifier = Modifier", "Card(\n            modifier = Modifier.animateContentSize()")

with open('app/src/main/java/com/strangerhelp/app/ui/screens/feed/FeedScreen.kt', 'w') as f:
    f.write(content)

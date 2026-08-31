import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/meets/CreateMeetScreen.kt", "r") as f:
    text = f.read()

text = text.replace("import androidx.compose.foundation.lazy.LazyColumn", "import androidx.compose.foundation.lazy.LazyColumn\nimport androidx.compose.foundation.lazy.LazyRow")

text = text.replace("containerColor = Error.copy", "containerColor = com.strangerhelp.app.ui.screens.meets.Warning.copy")
text = text.replace("color = Error,", "color = com.strangerhelp.app.ui.screens.meets.Warning,")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/meets/CreateMeetScreen.kt", "w") as f:
    f.write(text)

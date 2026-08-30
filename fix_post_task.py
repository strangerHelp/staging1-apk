with open('app/src/main/java/com/strangerhelp/app/ui/screens/post/PostTaskScreen.kt', 'r') as f:
    content = f.read()

imports_to_add = """
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Info
"""
content = content.replace("package com.strangerhelp.app.ui.screens.post\\n", "package com.strangerhelp.app.ui.screens.post\\n" + imports_to_add)

with open('app/src/main/java/com/strangerhelp/app/ui/screens/post/PostTaskScreen.kt', 'w') as f:
    f.write(content)

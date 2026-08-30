import re

with open('app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt', 'r') as f:
    content = f.read()

content = content.replace("import com.strangerhelp.app.ui.screens.WebViewScreen\\n", "")
content = content.replace("package com.strangerhelp.app.navigation", "package com.strangerhelp.app.navigation\\nimport com.strangerhelp.app.ui.screens.WebViewScreen")

with open('app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/strangerhelp/app/ui/screens/post/PostTaskScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("import androidx.compose.ui.graphics.Color\\nimport androidx.compose.ui.unit.sp\\nimport androidx.compose.material.icons.filled.Info\\n", "")
content = content.replace("package com.strangerhelp.app.ui.screens.post", "package com.strangerhelp.app.ui.screens.post\\nimport androidx.compose.ui.graphics.Color\\nimport androidx.compose.ui.unit.sp\\nimport androidx.compose.material.icons.filled.Info")

with open('app/src/main/java/com/strangerhelp/app/ui/screens/post/PostTaskScreen.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt', 'r') as f:
    content = f.read()

content = re.sub(r'trustScore:\s*Int\s*\)', 'trustScore: Int,\\n    verified: Boolean\\n)', content)

with open('app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt', 'w') as f:
    f.write(content)


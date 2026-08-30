with open('app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt', 'r') as f:
    content = f.read()

content = "import com.strangerhelp.app.ui.screens.WebViewScreen\n" + content
with open('app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/strangerhelp/app/ui/screens/post/PostTaskScreen.kt', 'r') as f:
    content = f.read()

content = "import androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.unit.sp\nimport androidx.compose.material.icons.filled.Info\n" + content
with open('app/src/main/java/com/strangerhelp/app/ui/screens/post/PostTaskScreen.kt', 'w') as f:
    f.write(content)


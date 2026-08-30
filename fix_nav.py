with open('app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt', 'r') as f:
    content = f.read()

if "import com.strangerhelp.app.ui.screens.WebViewScreen" not in content:
    content = content.replace("import androidx.compose.material3.*\\n", "import androidx.compose.material3.*\\nimport com.strangerhelp.app.ui.screens.WebViewScreen\\n")

with open('app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt', 'w') as f:
    f.write(content)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/PosterComponents.kt", "r") as f:
    content = f.read()

if "import com.strangerhelp.app.ui.theme.TrustColor" not in content:
    content = content.replace("import com.strangerhelp.app.ui.theme.Warning", "import com.strangerhelp.app.ui.theme.Warning\nimport com.strangerhelp.app.ui.theme.TrustColor")

if "import androidx.compose.material.icons.filled.Verified" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Star", "import androidx.compose.material.icons.filled.Star\nimport androidx.compose.material.icons.filled.Verified")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/PosterComponents.kt", "w") as f:
    f.write(content)


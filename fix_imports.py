with open("app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

content = content.replace("import com.strangerhelp.app.data.model.User", "import com.strangerhelp.app.data.model.User\nimport androidx.lifecycle.viewmodel.compose.viewModel\nimport com.strangerhelp.app.ui.screens.chat.ChatViewModel")

with open("app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt", "w") as f:
    f.write(content)

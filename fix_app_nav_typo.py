with open("app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt", "r") as f:
    content = f.read()
content = content.replace("\import com.strangerhelp.app.ui.screens.profile.VerificationScreen", "\nimport com.strangerhelp.app.ui.screens.profile.VerificationScreen")
with open("app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt", "w") as f:
    f.write(content)

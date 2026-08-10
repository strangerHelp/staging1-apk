import os

path = 'app/src/main/java/com/strangerhelp/app/MainActivity.kt'
with open(path, 'r') as f:
    content = f.read()

import_target = 'import com.strangerhelp.app.ui.screens.auth.LoginScreen'
import_replacement = '''import com.strangerhelp.app.ui.screens.auth.LoginScreen
import com.strangerhelp.app.ui.screens.LandingScreen'''

target = '''                    currentUser == null -> {
                        LoginScreen(onLoginSuccess = { user ->
                            currentUser = user
                        })
                    }'''

replacement = '''                    currentUser == null -> {
                        var showLogin by remember { mutableStateOf(false) }
                        if (showLogin) {
                            LoginScreen(onLoginSuccess = { user ->
                                currentUser = user
                                showLogin = false
                            })
                        } else {
                            LandingScreen(onLoginClick = { showLogin = true })
                        }
                    }'''

if target in content:
    content = content.replace(import_target, import_replacement)
    content = content.replace(target, replacement)
    with open(path, 'w') as f:
        f.write(content)
    print("Patched MainActivity")
else:
    print("Target not found")

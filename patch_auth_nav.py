import re

with open('app/src/main/java/com/strangerhelp/app/navigation/AuthNavigation.kt', 'r') as f:
    content = f.read()

content = content.replace("import com.strangerhelp.app.ui.screens.auth.OAuthWebViewScreen\n", "")

content = re.sub(r'composable\("oauth_webview"\)\s*\{\s*OAuthWebViewScreen\(\s*navController\s*=\s*navController,\s*onLoginSuccess\s*=\s*onLoginSuccess\s*\)\s*\}', '', content)

with open('app/src/main/java/com/strangerhelp/app/navigation/AuthNavigation.kt', 'w') as f:
    f.write(content)

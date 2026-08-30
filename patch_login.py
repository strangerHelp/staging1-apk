import re

with open('app/src/main/java/com/strangerhelp/app/ui/screens/auth/LoginScreen.kt', 'r') as f:
    content = f.read()

# Add imports
imports = """
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.gson.Gson
import androidx.compose.ui.platform.LocalContext
"""
content = content.replace("import kotlinx.coroutines.launch\n", "import kotlinx.coroutines.launch\n" + imports)

# Setup launcher
launcher_code = """    var isLoading by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val oauthLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val userJson = result.data?.getStringExtra("userJson")
            if (userJson != null) {
                val user = Gson().fromJson(userJson, User::class.java)
                onLoginSuccess(user)
            }
        }
    }"""
content = content.replace("    var isLoading by remember { mutableStateOf(false) }", launcher_code)

# Replace onGoogleLoginClick button action
button_replacement = """onClick = {
                    val intent = Intent(context, OAuthWebViewActivity::class.java)
                    oauthLauncher.launch(intent)
                }"""
content = re.sub(r'onClick = onGoogleLoginClick', button_replacement, content)

with open('app/src/main/java/com/strangerhelp/app/ui/screens/auth/LoginScreen.kt', 'w') as f:
    f.write(content)

import re

file_path = "app/src/main/java/com/strangerhelp/app/MainActivity.kt"
with open(file_path, "r") as f:
    content = f.read()

import_firebase = "import com.google.firebase.messaging.FirebaseMessaging\nimport android.os.Build\nimport android.Manifest\nimport android.content.Intent\nclass MainActivity"
content = content.replace("class MainActivity", import_firebase)

permission_logic = """    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
        }
        
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    com.strangerhelp.app.utils.AppLogger.d("FCM", "Token: $token")
                }
            }
        } catch (e: Exception) {}
        
        intent?.getStringExtra("deep_link")?.let { link ->
            // In a real app we would pass this to the Compose navigator
            com.strangerhelp.app.utils.AppLogger.d("FCM", "Deep link: $link")
        }"""

content = content.replace("    override fun onCreate(savedInstanceState: Bundle?) {\n        super.onCreate(savedInstanceState)", permission_logic)

with open(file_path, "w") as f:
    f.write(content)

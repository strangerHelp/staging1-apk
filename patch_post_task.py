import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/post/PostTaskScreen.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

target = '''    var isPosting by remember { mutableStateOf(false) }'''
replacement = '''    var isPosting by remember { mutableStateOf(false) }
    var isPrivate by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(isPosting) {
        if (isPosting) {
            try {
                val t = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), title)
                val d = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), description)
                val c = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), category)
                val b = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), budget)
                val l = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), location)
                val u = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), if (isUrgent) "1" else "0")
                val v = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), if (isPrivate) "private" else "public")
                
                val res = com.strangerhelp.app.data.api.ApiClient.api.postTask(
                    title = t,
                    description = d,
                    category = c,
                    budget = b,
                    location = l,
                    urgent = u,
                    visibility = v
                )
                
                if (res.isSuccessful) {
                    val inviteCode = res.body()?.get("inviteCode")
                    if (isPrivate && inviteCode != null) {
                        val sendIntent: android.content.Intent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, "Join my private task on StrangerHelp: https://strangerhelp.com/tasks/${res.body()?.get("taskId")}?invite=$inviteCode")
                            type = "text/plain"
                        }
                        val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }
                    navController.popBackStack()
                } else {
                    isPosting = false
                }
            } catch (e: Exception) {
                isPosting = false
            }
        }
    }'''

content = content.replace(target, replacement)

target2 = '''import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController'''

replacement2 = '''import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch'''

content = content.replace(target2, replacement2)

target3 = '''        var isPrivate by remember { mutableStateOf(false) }'''
replacement3 = ''''''
content = content.replace(target3, replacement3)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Updated PostTaskScreen")

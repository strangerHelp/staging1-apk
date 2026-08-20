import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/pulse/PulseScreen.kt'
with open(path, 'r') as f:
    content = f.read()

content = content.replace("fun PulseScreen(navController: NavController, viewModel: PulseViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {", "fun PulseScreen(navController: NavController) {")
content = content.replace("val helpRequests by viewModel.helpRequests.collectAsStateWithLifecycle()", "val db = com.strangerhelp.app.StrangerHelpApp.instance.database\n    val helpRequests by db.helpRequestDao().getAllHelpRequests().collectAsStateWithLifecycle(initialValue = emptyList())")
content = content.replace("val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()", "var isLoading by remember { mutableStateOf(true) }\n    LaunchedEffect(Unit) { kotlinx.coroutines.delay(1500); isLoading = false }")

with open(path, 'w') as f:
    f.write(content)

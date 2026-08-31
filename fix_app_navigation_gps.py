import re

with open("app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt", "r") as f:
    text = f.read()

text = text.replace("""                    com.strangerhelp.app.ui.screens.tasks.GpsCameraScreen(
                        taskId = taskId,
                        viewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.strangerhelp.app.ui.screens.tasks.TaskDetailViewModelFactory()),
                        navController = navController
                    )""", """                    val context = androidx.compose.ui.platform.LocalContext.current
                    com.strangerhelp.app.ui.screens.tasks.GpsCameraScreen(
                        taskId = taskId,
                        viewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.strangerhelp.app.ui.screens.tasks.GpsCameraViewModelFactory(context)),
                        navController = navController
                    )""")

with open("app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt", "w") as f:
    f.write(text)

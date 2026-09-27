import re

with open("app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

new_nav = """
                composable(
                    "gps_camera/{taskId}",
                    arguments = listOf(navArgument("taskId") { type = NavType.StringType })
                ) { entry ->
                    val taskId = entry.arguments?.getString("taskId") ?: ""
                    val context = androidx.compose.ui.platform.LocalContext.current
                    
                    val gpsCameraViewModel: com.strangerhelp.app.ui.screens.tasks.GpsCameraViewModel = 
                        androidx.lifecycle.viewmodel.compose.viewModel(factory = com.strangerhelp.app.ui.screens.tasks.GpsCameraViewModelFactory(context))

                    com.strangerhelp.app.ui.screens.tasks.GpsCameraScreen(
                        taskId = taskId,
                        onSubmitProof = { bytes ->
                            gpsCameraViewModel.submitProof(taskId, bytes) { success ->
                                if (success) {
                                    navController.popBackStack()
                                }
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
"""

pattern = r'composable\(\s*"gps_camera/\{taskId\}"\s*,.*?\}\s*\)\s*\{.*?GpsCameraScreen\(.*?\)\s*\}'
content = re.sub(pattern, new_nav.strip(), content, flags=re.DOTALL)

with open("app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt", "w") as f:
    f.write(content)

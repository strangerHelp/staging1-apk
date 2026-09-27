import re

with open("app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

old_nav = """
                composable(
                    "gps_camera/{taskId}",
                    arguments = listOf(navArgument("taskId") { type = NavType.StringType })
                ) { entry ->
                    val taskId = entry.arguments?.getString("taskId") ?: ""
                    val context = androidx.compose.ui.platform.LocalContext.current
                    com.strangerhelp.app.ui.screens.tasks.GpsCameraScreen(
                        taskId = taskId,
                        viewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.strangerhelp.app.ui.screens.tasks.GpsCameraViewModelFactory(context)),
                        navController = navController
                    )
                }
"""

new_nav = """
                composable(
                    "gps_camera/{taskId}",
                    arguments = listOf(navArgument("taskId") { type = NavType.StringType })
                ) { entry ->
                    val taskId = entry.arguments?.getString("taskId") ?: ""
                    val context = androidx.compose.ui.platform.LocalContext.current
                    
                    // We need a TaskDetailViewModel to call submitProof
                    val factory = com.strangerhelp.app.ui.screens.tasks.TaskDetailViewModelFactory(
                        com.strangerhelp.app.data.repository.TaskRepository(com.strangerhelp.app.data.api.ApiClient.api)
                    )
                    val taskDetailViewModel: com.strangerhelp.app.ui.screens.tasks.TaskDetailViewModel = 
                        androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)

                    com.strangerhelp.app.ui.screens.tasks.GpsCameraScreen(
                        taskId = taskId,
                        onSubmitProof = { bytes ->
                            // Use reflection or direct API call if submitProof isn't matching perfectly
                            // Let's use the taskDetailViewModel
                            taskDetailViewModel.submitProof(taskId, bytes) {
                                navController.popBackStack()
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
"""

# Let's search for the composable using regex to be safe
pattern = r'composable\(\s*"gps_camera/\{taskId\}"\s*,.*?\}\s*\)\s*\{.*?GpsCameraScreen\(.*?\)\s*\}'
content = re.sub(pattern, new_nav.strip(), content, flags=re.DOTALL)

with open("app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt", "w") as f:
    f.write(content)

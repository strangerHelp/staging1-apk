import re

with open("app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

start_idx = content.find('composable(\n                    "gps_camera/{taskId}"')
if start_idx != -1:
    end_idx = content.find('}', content.find('GpsCameraScreen', start_idx)) + 1
    
    new_nav = """composable(
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
                }"""
    
    # We actually need to replace up to the closing brace of the composable block
    # Let's find the closing brace of the composable block
    idx = content.find('GpsCameraScreen', start_idx)
    brace_idx = content.find('}', idx)
    
    # The composable block closes with }
    # So replace from start_idx to brace_idx + 1
    
    content = content[:start_idx] + new_nav + content[brace_idx+1:]
    
    with open("app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt", "w") as f:
        f.write(content)
else:
    print("Could not find gps_camera composable")


import re

file_path = "app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt"
with open(file_path, "r") as f:
    content = f.read()

new_route = """                composable(
                    route = "my_tasks?filter={filter}",
                    arguments = listOf(navArgument("filter") { type = NavType.StringType; defaultValue = "all" })
                ) { backStackEntry ->
                    val filter = backStackEntry.arguments?.getString("filter") ?: "all"
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val myTasksViewModel: com.strangerhelp.app.ui.screens.tasks.MyTasksViewModel = viewModel(
                        factory = com.strangerhelp.app.ui.screens.tasks.MyTasksViewModelFactory(
                            com.strangerhelp.app.data.repository.TaskRepository(com.strangerhelp.app.data.api.ApiClient.create(context))
                        )
                    )
                    com.strangerhelp.app.ui.screens.tasks.MyTasksScreen(filter = filter, navController = navController, viewModel = myTasksViewModel)
                }"""

if "my_tasks?filter={filter}" not in content:
    content = content.replace('composable(Screen.Tasks.route) { TasksScreen(navController) }', 'composable(Screen.Tasks.route) { TasksScreen(navController) }\n' + new_route)

with open(file_path, "w") as f:
    f.write(content)

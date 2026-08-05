sed -i 's/val db = com.strangerhelp.app.StrangerHelpApp.instance.database/val db = com.strangerhelp.app.StrangerHelpApp.instance.database/g' app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TasksScreen.kt
sed -i 's/\.androidx\.lifecycle\.compose\.collectAsStateWithLifecycle/.collectAsStateWithLifecycle/g' app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TasksScreen.kt
sed -i '1iimport androidx.lifecycle.compose.collectAsStateWithLifecycle' app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TasksScreen.kt

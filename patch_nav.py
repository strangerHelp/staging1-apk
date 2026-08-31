import sys

with open("app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

imports = """
import com.strangerhelp.app.ui.screens.meets.CreateMeetScreen
import com.strangerhelp.app.ui.screens.meets.MeetDetailScreen
import com.strangerhelp.app.ui.screens.meets.MeetViewModel
import com.strangerhelp.app.ui.screens.meets.MeetViewModelFactory
import com.strangerhelp.app.ui.screens.meets.MeetsListScreen
import com.strangerhelp.app.data.repository.MeetRepository
"""

if "import com.strangerhelp.app.ui.screens.meets" not in content:
    content = content.replace("import com.strangerhelp.app.ui.screens.profile.KarmaWalletScreen", "import com.strangerhelp.app.ui.screens.profile.KarmaWalletScreen\n" + imports)

# We need an instance of MeetViewModel. 
# We'll initialize MeetRepository with ApiClient.api and AuthRepository.
viewmodel_init = """
    val meetViewModel: MeetViewModel = viewModel(
        factory = MeetViewModelFactory(
            MeetRepository(com.strangerhelp.app.data.api.ApiClient.api),
            com.strangerhelp.app.data.repository.AuthRepository(com.strangerhelp.app.data.api.ApiClient.api)
        )
    )
"""

if "val meetViewModel" not in content:
    content = content.replace("val chatViewModel: ChatViewModel = viewModel()", "val chatViewModel: ChatViewModel = viewModel()\n" + viewmodel_init)

routes = """
                composable("meets") {
                    MeetsListScreen(viewModel = meetViewModel, navController = navController)
                }
                composable("create_meet") {
                    CreateMeetScreen(viewModel = meetViewModel, navController = navController)
                }
                composable("meet_detail/{meetId}") { backStackEntry ->
                    val meetId = backStackEntry.arguments?.getString("meetId") ?: ""
                    MeetDetailScreen(meetId = meetId, viewModel = meetViewModel, navController = navController)
                }
"""

if "composable(\"meets\")" not in content:
    # insert before the final '}' of NavHost block
    # Actually just insert it after karma_wallet
    content = content.replace('KarmaWalletScreen(navController = navController)\n                }', 'KarmaWalletScreen(navController = navController)\n                }\n' + routes)

with open("app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt", "w") as f:
    f.write(content)

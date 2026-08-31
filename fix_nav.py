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

content = content.replace("import com.strangerhelp.app.ui.screens.profile.KarmaWalletScreen", "import com.strangerhelp.app.ui.screens.profile.KarmaWalletScreen\n" + imports)

with open("app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt", "w") as f:
    f.write(content)

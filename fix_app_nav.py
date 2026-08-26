import re

with open("app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

# Add imports
imports = """import com.strangerhelp.app.ui.screens.profile.VerifyIdScreen
import com.strangerhelp.app.ui.screens.profile.ReferEarnScreen
import com.strangerhelp.app.ui.screens.profile.KarmaWalletScreen
"""

if "VerifyIdScreen" not in content:
    content = content.replace("import com.strangerhelp.app.ui.screens.profile.VerificationScreen", imports + "\import com.strangerhelp.app.ui.screens.profile.VerificationScreen")
    
# Add routes
routes = """
                composable("verify_id") {
                    VerifyIdScreen(navController = navController)
                }
                
                composable("refer_earn") {
                    ReferEarnScreen(navController = navController)
                }
                
                composable("karma_wallet") {
                    KarmaWalletScreen(navController = navController)
                }
"""

if "composable(\"verify_id\")" not in content:
    content = content.replace("composable(\"edit_profile\") { EditProfileScreen(navController, user) }", "composable(\"edit_profile\") { EditProfileScreen(navController, user) }\n" + routes)

with open("app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt", "w") as f:
    f.write(content)

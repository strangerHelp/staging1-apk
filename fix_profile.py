import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt", "r") as f:
    content = f.read()

# Add ProfileRating import
if "import com.strangerhelp.app.ui.screens.tasks.ProfileRating" not in content:
    content = content.replace("import com.strangerhelp.app.ui.theme.*", "import com.strangerhelp.app.ui.theme.*\nimport com.strangerhelp.app.ui.screens.tasks.ProfileRating")

# Add ProfileRating to ProfileScreen
new_rating_section = """                    }
                    Spacer(Modifier.height(8.dp))
                    if (user != null) {
                        ProfileRating(rating = user.rating, totalReviews = user.totalReviews)
                    }
                    Spacer(Modifier.height(24.dp))"""
content = content.replace("                    }\n                    Spacer(Modifier.height(24.dp))", new_rating_section)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt", "w") as f:
    f.write(content)

import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt", "r") as f:
    content = f.read()

# Define the pattern for the Legal Section
legal_pattern = r"(\s*// ⭐ Legal Section\s*Text\([\s\S]*?Spacer\(Modifier\.height\(24\.dp\)\)\s*)+"

# We want to replace all occurrences of this repeated block with a single correct block
single_legal = """
            // ⭐ Legal Section
            Text(
                text = "Legal",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Muted,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            LegalMenuItem(
                icon = "📜",
                title = "Terms of Service",
                subtitle = "Includes P2P payment terms",
                onClick = { navController.navigate("webview?url=${java.net.URLEncoder.encode("https://strangerhelp.com/terms", "UTF-8")}") }
            )
            LegalMenuItem(
                icon = "🔒",
                title = "Privacy Policy",
                subtitle = "We do not collect payment data",
                onClick = { navController.navigate("webview?url=${java.net.URLEncoder.encode("https://strangerhelp.com/privacy", "UTF-8")}") }
            )
            LegalMenuItem(
                icon = "⚠️",
                title = "Disclaimer",
                subtitle = "P2P payments — platform not liable",
                onClick = { navController.navigate("disclaimer") }
            )
            LegalMenuItem(
                icon = "🍪",
                title = "Cookie Policy",
                subtitle = "",
                onClick = { navController.navigate("cookie_policy") }
            )
            LegalMenuItem(
                icon = "📋",
                title = "Community Guidelines",
                subtitle = "",
                onClick = { navController.navigate("community_guidelines") }
            )
            
            Spacer(Modifier.height(24.dp))
"""

content = re.sub(legal_pattern, single_legal, content)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt", "w") as f:
    f.write(content)

print("ProfileScreen.kt updated!")

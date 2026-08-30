with open('app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
skip = False
for line in lines:
    if "fun TrustStatsCard(" in line:
        new_lines.append(line)
        skip = True
        continue
    
    if skip:
        if line.strip() == "{":
            skip = False
            new_lines.append(") {\n")
            continue
        elif "verified: Boolean" in line:
            new_lines.append(line)
        elif "trustScore: Int" in line:
            new_lines.append(line)
        elif "completionRate: Int" in line:
            new_lines.append(line)
        elif "tasksCompleted: Int" in line:
            new_lines.append(line)
        elif "totalReviews: Int" in line:
            new_lines.append(line)
        elif "rating: Double" in line:
            new_lines.append(line)
    else:
        new_lines.append(line)

found = -1
for i, line in enumerate(new_lines):
    if "verified = user?.verified == 1" in line:
        found = i + 2
        break

legal_section = """
            Spacer(Modifier.height(24.dp))
            
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
                onClick = { navController.navigate("webview?url=${java.net.URLEncoder.encode("https://strangerhelp.com/disclaimer", "UTF-8")}") }
            )
            LegalMenuItem(
                icon = "🍪",
                title = "Cookie Policy",
                subtitle = "",
                onClick = { navController.navigate("webview?url=${java.net.URLEncoder.encode("https://strangerhelp.com/cookies", "UTF-8")}") }
            )
            LegalMenuItem(
                icon = "📋",
                title = "Community Guidelines",
                subtitle = "",
                onClick = { navController.navigate("webview?url=${java.net.URLEncoder.encode("https://strangerhelp.com/guidelines", "UTF-8")}") }
            )
            
            Spacer(Modifier.height(24.dp))
"""

if found != -1:
    new_lines.insert(found, legal_section)

with open('app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt', 'w') as f:
    f.writelines(new_lines)


import re

with open('app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt', 'r') as f:
    content = f.read()

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

content = re.sub(r'(TrustStatsCard\(.*?\n\s*\))', r'\1' + legal_section, content, flags=re.DOTALL)

legal_menu_item = """
@Composable
fun LegalMenuItem(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Muted
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Muted
            )
        }
    }
}
"""

content += legal_menu_item

with open('app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt', 'w') as f:
    f.write(content)

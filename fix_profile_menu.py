import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt", "r") as f:
    content = f.read()

# Remove the old Identity Verification menu item row
target_menu_item = """                    MenuItemRow(
                        icon = Icons.Outlined.VerifiedUser, 
                        label = "Identity Verification", 
                        onClick = { }, 
                        showChevron = true,
                        trailingContent = {
                            if (user?.verified == 1) {
                                Text(
                                    "Verified", 
                                    color = Color(0xFF00BFA5), 
                                    fontSize = 12.sp,
                                    modifier = Modifier.background(Color(0xFF004D40), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    )
                    Divider(color = Color(0xFFF0F0F0))"""

content = content.replace(target_menu_item, "")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt", "w") as f:
    f.write(content)

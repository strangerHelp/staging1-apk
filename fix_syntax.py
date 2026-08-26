with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt", "r") as f:
    content = f.read()

target = """, modifier = Modifier.weight(1f))
                QuickActionCard(icon = Icons.Default.CardGiftcard, label = "Refer & Earn", onClick = { }, modifier = Modifier.weight(1f))
                QuickActionCard(icon = Icons.Outlined.AccountBalanceWallet, label = "Karma Wallet", onClick = { }, modifier = Modifier.weight(1f))
            }"""

if target in content:
    content = content.replace(target, "")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt", "w") as f:
    f.write(content)

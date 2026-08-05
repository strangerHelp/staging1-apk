sed -i 's/    Scaffold(modifier = Modifier.imePadding(),/    val isDark = when (chatTheme) {\n        "dark" -> true\n        "light" -> false\n        else -> androidx.compose.foundation.isSystemInDarkTheme()\n    }\n    StrangerHelpTheme(darkTheme = isDark) {\n    Scaffold(modifier = Modifier.imePadding(),/' app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatDetailScreen.kt

sed -i 's/^    }$/    }\n    }/' app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatDetailScreen.kt

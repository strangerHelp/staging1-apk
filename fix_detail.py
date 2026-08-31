with open("app/src/main/java/com/strangerhelp/app/ui/screens/meets/MeetDetailScreen.kt", "r") as f:
    text = f.read()

text = text.replace("tint = Error", "tint = com.strangerhelp.app.ui.screens.meets.Warning")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/meets/MeetDetailScreen.kt", "w") as f:
    f.write(text)


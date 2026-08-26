with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt", "r") as f:
    content = f.read()

# Fix conflicting TextAlign imports
lines = content.split('\n')
unique_lines = []
text_align_imported = False
for line in lines:
    if line == "import androidx.compose.ui.text.style.TextAlign":
        if text_align_imported:
            continue
        text_align_imported = True
    unique_lines.append(line)

content = '\n'.join(unique_lines)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt", "w") as f:
    f.write(content)


# Fix TrustColor in VerifyIdScreen
with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/VerifyIdScreen.kt", "r") as f:
    content2 = f.read()

content2 = content2.replace("import com.strangerhelp.app.ui.theme.TrustColor", "import com.strangerhelp.app.ui.theme.CyanDeep")
content2 = content2.replace("color = TrustColor", "color = CyanDeep")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/VerifyIdScreen.kt", "w") as f:
    f.write(content2)


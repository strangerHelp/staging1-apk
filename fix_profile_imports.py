import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt'
with open(path, 'r') as f:
    content = f.read()

content = content.replace('androidx.compose.ui.text.withStyle', 'withStyle')
if 'import androidx.compose.ui.text.withStyle' not in content:
    content = content.replace('import androidx.compose.ui.unit.sp', 'import androidx.compose.ui.unit.sp\nimport androidx.compose.ui.text.withStyle\nimport androidx.compose.ui.text.buildAnnotatedString\nimport androidx.compose.ui.text.SpanStyle')

with open(path, 'w') as f:
    f.write(content)
print("Fixed ProfileScreen imports")

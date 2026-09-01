import re

file_path = "app/src/main/java/com/strangerhelp/app/MainActivity.kt"
with open(file_path, "r") as f:
    content = f.read()

# Remove the broken imports from the middle of the file
bad_imports = "import com.google.firebase.messaging.FirebaseMessaging\nimport android.os.Build\nimport android.Manifest\nimport android.content.Intent\nclass MainActivity"
content = content.replace(bad_imports, "class MainActivity")

# Add them to the top of the file (after package declaration)
good_imports = "import com.google.firebase.messaging.FirebaseMessaging\nimport android.os.Build\nimport android.Manifest\nimport android.content.Intent\n"
content = content.replace("package com.strangerhelp.app\n", "package com.strangerhelp.app\n\n" + good_imports)

with open(file_path, "w") as f:
    f.write(content)

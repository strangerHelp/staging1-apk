import re

file_path = "app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt"
with open(file_path, "r") as f:
    content = f.read()

content = content.replace(
    'com.strangerhelp.app.data.api.ApiClient.create(context)',
    'com.strangerhelp.app.data.api.ApiClient.api'
)

with open(file_path, "w") as f:
    f.write(content)

import os

file_path = "app/build.gradle.kts"
with open(file_path, "r") as f:
    content = f.read()

content = content.replace('applicationId = "com.aistudio.strangerhelp.hxmpzq"', 'applicationId = "com.strangerhelp.app"')

with open(file_path, "w") as f:
    f.write(content)

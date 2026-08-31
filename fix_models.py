import re

with open("app/src/main/java/com/strangerhelp/app/data/model/Models.kt", "r") as f:
    content = f.read()

content = content.replace("    val completionStatus: String? = null,\n", "")

with open("app/src/main/java/com/strangerhelp/app/data/model/Models.kt", "w") as f:
    f.write(content)

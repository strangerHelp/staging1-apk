import re

with open("app/src/main/java/com/strangerhelp/app/data/model/Models.kt", "r") as f:
    content = f.read()

# Replace val emailVerified: Boolean = false,
content = re.sub(r'val emailVerified:\s*Boolean\s*=\s*false,', r'@SerializedName("email_verified") val emailVerified: Int = 0,', content)

with open("app/src/main/java/com/strangerhelp/app/data/model/Models.kt", "w") as f:
    f.write(content)

import re

with open("app/src/main/java/com/strangerhelp/app/data/model/Models.kt", "r") as f:
    content = f.read()

if "data class VerificationStatus" not in content:
    content += """
data class VerificationStatus(
    val status: String = "",
    val verified: Boolean = false
)"""

with open("app/src/main/java/com/strangerhelp/app/data/model/Models.kt", "w") as f:
    f.write(content)

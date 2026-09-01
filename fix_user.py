import re

with open("app/src/main/java/com/strangerhelp/app/data/model/Models.kt", "r") as f:
    content = f.read()

content = content.replace("    val is_admin: Int = 0,\n)", "    val is_admin: Int = 0,\n    val rating: Double = 0.0,\n    val totalReviews: Int = 0,\n    val trustScore: Int = 0\n)")

with open("app/src/main/java/com/strangerhelp/app/data/model/Models.kt", "w") as f:
    f.write(content)

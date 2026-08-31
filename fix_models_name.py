import re

with open("app/src/main/java/com/strangerhelp/app/data/model/Models.kt", "r") as f:
    text = f.read()

# Fix User:
text = text.replace("""data class User(
    val id: String = "",
    val userName: String = "",""", """data class User(
    val id: String = "",
    val name: String = "",""")

# Fix AuthResponse:
text = text.replace("""data class AuthResponse(val id: String = "", val userName: String = "")""", """data class AuthResponse(val id: String = "", val name: String = "")""")

with open("app/src/main/java/com/strangerhelp/app/data/model/Models.kt", "w") as f:
    f.write(text)


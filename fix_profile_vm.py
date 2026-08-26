import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("ApiClient.api.verifyEmail()", "ApiClient.api.resendVerification()")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileViewModel.kt", "w") as f:
    f.write(content)

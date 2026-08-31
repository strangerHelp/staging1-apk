import sys

with open("app/src/main/java/com/strangerhelp/app/ui/screens/meets/MeetViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("_currentUser.value = authRepository.getCurrentUser()", """
            val response = authRepository.getCurrentUser()
            if (response.isSuccessful) {
                _currentUser.value = response.body()?.user
            }
""")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/meets/MeetViewModel.kt", "w") as f:
    f.write(content)

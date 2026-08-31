with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "r") as f:
    lines = f.readlines()
    
in_helper = False
for line in lines:
    if "TaskUiState.HELPER_PROOF_PENDING ->" in line:
        in_helper = True
    if in_helper:
        print(line, end="")
    if "TaskUiState.POSTER_WAITING ->" in line:
        break

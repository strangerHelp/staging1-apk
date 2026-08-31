with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/PosterComponents.kt", "r") as f:
    lines = f.readlines()
    
in_rejection = False
for line in lines:
    if "fun RejectionDialog" in line:
        in_rejection = True
    if in_rejection:
        print(line, end="")
        if line.startswith("}"):
            break

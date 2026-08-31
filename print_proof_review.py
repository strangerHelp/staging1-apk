with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/PosterComponents.kt", "r") as f:
    lines = f.readlines()
    
in_proof = False
for line in lines:
    if "fun ProofReviewSection" in line:
        in_proof = True
    if in_proof:
        print(line, end="")
        if line.startswith("}"):
            break

with open("app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt", "r") as f:
    text = f.read()

idx = text.find("// Meets", text.find("// Meets") + 1)
if idx != -1:
    clean = text[:idx] + "}"
    with open("app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt", "w") as f:
        f.write(clean)


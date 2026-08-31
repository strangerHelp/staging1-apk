with open("app/src/main/java/com/strangerhelp/app/data/model/Models.kt", "r") as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    new_lines.append(line)
    if "data class UserResponse(val user: User?)" in line:
        break

with open("app/src/main/java/com/strangerhelp/app/data/model/Models.kt", "w") as f:
    f.writelines(new_lines)

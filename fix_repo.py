import re

with open("app/src/main/java/com/strangerhelp/app/data/repository/ReviewRepository.kt", "r") as f:
    content = f.read()

content = content.replace("import javax.inject.Inject\n", "")
content = content.replace("class ReviewRepository @Inject constructor(", "class ReviewRepository(")

with open("app/src/main/java/com/strangerhelp/app/data/repository/ReviewRepository.kt", "w") as f:
    f.write(content)

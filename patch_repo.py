import re

file_path = "app/src/main/java/com/strangerhelp/app/data/repository/AskRepository.kt"
with open(file_path, "r") as f:
    content = f.read()

content = re.sub(r'import javax\.inject\.Inject\n', '', content)
content = re.sub(r'@Inject constructor', 'constructor', content)

with open(file_path, "w") as f:
    f.write(content)

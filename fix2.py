import re
with open('app/src/main/java/com/strangerhelp/app/ui/screens/post/PostTaskScreen.kt', 'r') as f:
    content = f.read()

content = re.sub(r'colors = OutlinedTextFieldDefaults\.colors\(unfocusedBorderColor = MaterialTheme\.colorScheme\.outline\)\)?,?', '', content)

with open('app/src/main/java/com/strangerhelp/app/ui/screens/post/PostTaskScreen.kt', 'w') as f:
    f.write(content)

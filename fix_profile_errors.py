with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt", "r") as f:
    content = f.read()

# Fix \n inside strings
content = content.replace('Text("Tasks\nCompleted"', 'Text("Tasks\\nCompleted"')
content = content.replace('Text("Completion\nRate"', 'Text("Completion\\nRate"')
content = content.replace('viewModel.loadData()', '// viewModel.loadData()')

with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt", "w") as f:
    f.write(content)

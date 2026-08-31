with open("app/src/main/java/com/strangerhelp/app/ui/components/LocationPicker.kt", "r") as f:
    content = f.read()

content = content.replace(
    'query = it\n                if (it.length >= 3) {',
    'query = it\n                onLocationSelected(0.0, 0.0, it)\n                if (it.length >= 3) {'
)

with open("app/src/main/java/com/strangerhelp/app/ui/components/LocationPicker.kt", "w") as f:
    f.write(content)

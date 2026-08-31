with open("app/src/main/java/com/strangerhelp/app/MainActivity.kt", "r") as f:
    content = f.read()

# Remove enableEdgeToEdge()
content = content.replace("        enableEdgeToEdge()\n", "")

# We can also remove windowInsetsPadding(WindowInsets.statusBars) since it's not edge-to-edge anymore,
# but it's okay to leave it, though maybe it will add double padding?
# Let's remove .windowInsetsPadding(WindowInsets.statusBars)
content = content.replace(".windowInsetsPadding(WindowInsets.statusBars)", "")

with open("app/src/main/java/com/strangerhelp/app/MainActivity.kt", "w") as f:
    f.write(content)

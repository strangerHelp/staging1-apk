import os
import shutil

# 1. Revert Theme.kt
theme_path = "app/src/main/java/com/strangerhelp/app/ui/theme/Theme.kt"
with open(theme_path, "r") as f:
    content = f.read()

content = content.replace("darkTheme: Boolean = isSystemInDarkTheme(),", "darkTheme: Boolean = false, // Forced white/light theme")

with open(theme_path, "w") as f:
    f.write(content)

# 2. Revert colors.xml
colors_path = "app/src/main/res/values/colors.xml"
colors_content = """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="ic_bg">#FFFFFF</color>
</resources>
"""
with open(colors_path, "w") as f:
    f.write(colors_content)

# 3. Revert styles.xml
styles_path = "app/src/main/res/values/styles.xml"
styles_content = """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.StrangerHelp" parent="android:Theme.Material.Light.NoActionBar">
        <item name="android:statusBarColor">#171717</item>
        <item name="android:navigationBarColor">#171717</item>
    </style>
</resources>
"""
with open(styles_path, "w") as f:
    f.write(styles_content)

# 4. Remove values-night directory
night_dir = "app/src/main/res/values-night"
if os.path.exists(night_dir):
    shutil.rmtree(night_dir)

print("Reverted successfully.")

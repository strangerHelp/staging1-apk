import os
import shutil

# Copy the PNG to mipmap-xxxhdpi for legacy devices
os.makedirs("app/src/main/res/mipmap-xxxhdpi", exist_ok=True)
shutil.copy("app/src/main/res/drawable-nodpi/ic_launcher_png.png", "app/src/main/res/mipmap-xxxhdpi/ic_launcher.png")
shutil.copy("app/src/main/res/drawable-nodpi/ic_launcher_png.png", "app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png")

# Create the adaptive icon XML
adaptive_xml = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background>
        <color android:color="#012048"/>
    </background>
    <foreground android:drawable="@drawable/ic_launcher_png"/>
</adaptive-icon>
"""

os.makedirs("app/src/main/res/mipmap-anydpi-v26", exist_ok=True)
with open("app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml", "w") as f:
    f.write(adaptive_xml)

with open("app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml", "w") as f:
    f.write(adaptive_xml)

print("Icons updated")

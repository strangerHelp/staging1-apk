import os

adaptive_xml = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background>
        <color android:color="#FFFFFF"/>
    </background>
    <foreground android:drawable="@drawable/ic_launcher_foreground_image"/>
</adaptive-icon>
"""

os.makedirs("app/src/main/res/mipmap-anydpi-v26", exist_ok=True)
with open("app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml", "w") as f:
    f.write(adaptive_xml)

with open("app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml", "w") as f:
    f.write(adaptive_xml)

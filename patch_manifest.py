import re

with open("app/src/main/AndroidManifest.xml", "r") as f:
    content = f.read()

if "StopTrackingReceiver" not in content:
    content = content.replace("</application>", """
        <receiver
            android:name=".service.StopTrackingReceiver"
            android:exported="false" />
    </application>""")

with open("app/src/main/AndroidManifest.xml", "w") as f:
    f.write(content)

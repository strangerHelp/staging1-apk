import re

file_path = "app/src/main/AndroidManifest.xml"
with open(file_path, "r") as f:
    content = f.read()

service_entry = """        <service
            android:name=".service.StrangerHelpFirebaseMessagingService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>

        <service"""

if "StrangerHelpFirebaseMessagingService" not in content:
    content = content.replace('        <service', service_entry)

with open(file_path, "w") as f:
    f.write(content)

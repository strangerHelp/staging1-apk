import os

path = 'app/src/main/AndroidManifest.xml'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

target = '''<uses-permission android:name="android.permission.INTERNET" />'''
replacement = '''<uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />'''

content = content.replace(target, replacement)

target2 = '''</application>'''
replacement2 = '''    <service
            android:name=".service.TrackingService"
            android:foregroundServiceType="location"
            android:exported="false" />
    </application>'''

content = content.replace(target2, replacement2)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Updated AndroidManifest.xml")

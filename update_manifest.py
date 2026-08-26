import re

with open('app/src/main/AndroidManifest.xml', 'r') as f:
    content = f.read()

permissions = """
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
"""

if "ACCESS_FINE_LOCATION" not in content:
    content = content.replace('<uses-permission android:name="android.permission.INTERNET" />', permissions)

with open('app/src/main/AndroidManifest.xml', 'w') as f:
    f.write(content)

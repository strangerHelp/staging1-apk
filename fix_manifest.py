with open("app/src/main/AndroidManifest.xml", "r") as f:
    content = f.read()

deep_links = """                <data
                    android:host="strangerhelp.com"
                    android:pathPrefix="/reset-password"
                    android:scheme="https" />
                <data
                    android:host="strangerhelp.com"
                    android:pathPrefix="/api/auth/verify-email"
                    android:scheme="https" />"""

content = content.replace('''                <data
                    android:host="strangerhelp.com"
                    android:pathPrefix="/reset-password"
                    android:scheme="https" />''', deep_links)

with open("app/src/main/AndroidManifest.xml", "w") as f:
    f.write(content)

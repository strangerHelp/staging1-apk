with open("app/src/main/java/com/strangerhelp/app/ui/components/EmailVerificationBanner.kt", "r") as f:
    content = f.read()

content = content.replace("TrustColor", "CyanDeep")
content = content.replace("TextButtonDefaults.textButtonColors", "ButtonDefaults.textButtonColors")

with open("app/src/main/java/com/strangerhelp/app/ui/components/EmailVerificationBanner.kt", "w") as f:
    f.write(content)

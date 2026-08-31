with open("app/src/main/java/com/strangerhelp/app/utils/GpsCameraHelper.kt", "r") as f:
    text = f.read()
    
# Let's completely rewrite it to match the requested `GpsCameraHelper` but with the GpsStampHelper logic combined or separated as in the guide.
# Wait, the guide suggests having `GpsCameraHelper` (for location) and `GpsStampHelper` (for stamping).
# Let's split them.

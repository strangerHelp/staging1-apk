import re
with open("app/src/main/java/com/strangerhelp/app/ui/screens/post/PostTaskScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
    'var location by remember { mutableStateOf("") }',
    'var location by remember { mutableStateOf("") }\n    var taskLat by remember { mutableStateOf("") }\n    var taskLng by remember { mutableStateOf("") }'
)

content = content.replace(
    'LocationPicker(onLocationSelected = { lat, lng, addr -> location = addr })',
    'LocationPicker(onLocationSelected = { lat, lng, addr -> location = addr; taskLat = lat.toString(); taskLng = lng.toString() })'
)

content = content.replace(
    'builder.addFormDataPart("lat", "12.9716")',
    'if (taskLat.isNotEmpty() && taskLat != "0.0") builder.addFormDataPart("lat", taskLat)'
)

content = content.replace(
    'builder.addFormDataPart("lng", "77.5946")',
    'if (taskLng.isNotEmpty() && taskLng != "0.0") builder.addFormDataPart("lng", taskLng)'
)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/post/PostTaskScreen.kt", "w") as f:
    f.write(content)

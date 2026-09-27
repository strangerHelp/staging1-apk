import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TrackingComponents.kt", "r") as f:
    content = f.read()

content = content.replace("viewModel.startTracking(task._id)", "viewModel.startTracking(task._id, context)")
content = content.replace("viewModel.stopTracking(task._id)", "viewModel.stopTracking(task._id, context)")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TrackingComponents.kt", "w") as f:
    f.write(content)

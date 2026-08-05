sed -i '/fun TasksScreen/a \
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)\
\
    LaunchedEffect(Unit) {\
        if (!locationPermissionState.status.isGranted) {\
            locationPermissionState.launchPermissionRequest()\
        }\
    }' app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TasksScreen.kt

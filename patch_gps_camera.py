import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/GpsCameraScreen.kt", "r") as f:
    content = f.read()

# Replace single permission launcher with multiple
old_launcher = """
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.checkLocation()
        } else {
            // Handle denied permission
        }
    }

    LaunchedEffect(Unit) {
        if (!locationHelper.hasLocationPermission()) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            viewModel.checkLocation()
        }
    }
""".strip()

new_launcher = """
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                         permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        val camGranted = permissions.getOrDefault(Manifest.permission.CAMERA, false)
        if (locGranted && camGranted) {
            viewModel.checkLocation()
        } else {
            // Handle denied permission
        }
    }

    LaunchedEffect(Unit) {
        val hasLoc = locationHelper.hasLocationPermission()
        val hasCam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!hasLoc || !hasCam) {
            requestPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA)
            )
        } else {
            viewModel.checkLocation()
        }
    }
""".strip()

content = content.replace(old_launcher, new_launcher)

# Replace the button onClick as well
old_button_click = """
                            onClick = {
                                if (!locationHelper.hasLocationPermission()) {
                                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                } else {
                                    viewModel.retryLocation()
                                }
                            },
""".strip()

new_button_click = """
                            onClick = {
                                val hasLoc = locationHelper.hasLocationPermission()
                                val hasCam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                if (!hasLoc || !hasCam) {
                                    requestPermissionLauncher.launch(
                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA)
                                    )
                                } else {
                                    viewModel.retryLocation()
                                }
                            },
""".strip()

content = content.replace(old_button_click, new_button_click)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/GpsCameraScreen.kt", "w") as f:
    f.write(content)

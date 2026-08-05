sed -i '1iimport com.google.accompanist.permissions.ExperimentalPermissionsApi\nimport com.google.accompanist.permissions.isGranted\nimport com.google.accompanist.permissions.rememberPermissionState\nimport android.Manifest' app/src/main/java/com/strangerhelp/app/ui/screens/feed/FeedScreen.kt
sed -i 's/@OptIn(ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)/@OptIn(ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)/' app/src/main/java/com/strangerhelp/app/ui/screens/feed/FeedScreen.kt
sed -i '/fun FeedScreen/a \
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)\
\
    LaunchedEffect(Unit) {\
        if (!locationPermissionState.status.isGranted) {\
            locationPermissionState.launchPermissionRequest()\
        }\
    }' app/src/main/java/com/strangerhelp/app/ui/screens/feed/FeedScreen.kt

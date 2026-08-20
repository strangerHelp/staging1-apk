import re

with open('app/src/main/java/com/strangerhelp/app/ui/screens/pulse/PulseScreen.kt', 'r') as f:
    content = f.read()

# 1. Add downloadProgress state
state_injection = """    val downloadProgress by MapHelper.offlineDownloadProgress.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {"""
content = content.replace("    LaunchedEffect(Unit) {", state_injection, 1)

# 2. Add progress bar to the top of the map
# Looking for:
#                 Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
#                     AndroidView(
#                         factory = { context ->

progress_injection = """                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    AndroidView(
                        factory = { context ->"""

new_progress_injection = """                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (downloadProgress != null) {
                        LinearProgressIndicator(
                            progress = { downloadProgress!! / 100f },
                            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(top = 8.dp, start = 16.dp, end = 16.dp),
                            color = Saffron
                        )
                    }
                    AndroidView(
                        factory = { context ->"""
content = content.replace(progress_injection, new_progress_injection, 1)

# 3. Add a download button to the map controls
# Looking for:
#                 SmallFloatingActionButton(
#                     onClick = {
#                         mapRef?.animateCamera(CameraUpdateFactory.newLatLngZoom(centerPoint, 13.0))
#                     },
#                     containerColor = MaterialTheme.colorScheme.surface,
#                     contentColor = MaterialTheme.colorScheme.onSurface
#                 ) {
#                     Icon(Icons.Outlined.MyLocation, "Locate")
#                 }
#             }

download_btn_injection = """                SmallFloatingActionButton(
                    onClick = {
                        mapRef?.animateCamera(CameraUpdateFactory.newLatLngZoom(centerPoint, 13.0))
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Icon(Icons.Outlined.MyLocation, "Locate")
                }
                
                SmallFloatingActionButton(
                    onClick = {
                        MapHelper.downloadOfflineRegion(context, centerPoint, radiusKm = 10.0, regionName = "PulseRegion")
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Outlined.CloudDownload, "Download Offline Map")
                }
            }"""

content = re.sub(
    r'                SmallFloatingActionButton\(\s*onClick = \{\s*mapRef\?\.animateCamera\(CameraUpdateFactory\.newLatLngZoom\(centerPoint, 13\.0\)\)\s*\},[^)]*\)[^{]*\{\s*Icon\(Icons\.Outlined\.MyLocation, "Locate"\)\s*\}\s*\}',
    download_btn_injection,
    content,
    flags=re.MULTILINE
)

with open('app/src/main/java/com/strangerhelp/app/ui/screens/pulse/PulseScreen.kt', 'w') as f:
    f.write(content)

import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TrackingComponents.kt", "r") as f:
    content = f.read()

# Make sure we add necessary imports
imports = """
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
"""

if "Manifest.permission" not in content:
    content = content.replace("import androidx.compose.ui.unit.dp", "import androidx.compose.ui.unit.dp\n" + imports)

# We want to replace HelperTrackingActions
helper_actions_new = """
@Composable
fun HelperTrackingActions(
    task: Task,
    viewModel: TaskDetailViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isTracking by viewModel.isTracking.collectAsState()
    val trackingError by viewModel.trackingError.collectAsState()
    var showDisclosure by remember { mutableStateOf(false) }

    val requiredPermissions = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val fineGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            result[Manifest.permission.POST_NOTIFICATIONS] == true
        } else true

        if ((fineGranted || coarseGranted) && notifGranted) {
            viewModel.startTracking(task._id)
        } else {
            // handle denied logic
        }
    }

    val TrustColor = Color(0xFF10B981)
    val Error = Color.Red
    val Body = Color.Gray
    val Muted = Color.LightGray

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isTracking) {
            // Active tracking card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = TrustColor.copy(alpha = 0.12f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📍 Live tracking active")
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { viewModel.stopTracking(task._id) }) {
                        Text("Stop sharing")
                    }
                }
            }
        } else {
            OutlinedButton(
                onClick = { showDisclosure = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📍 Start Task (Share Live Location)")
            }
        }

        trackingError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )
        }
    }

    if (showDisclosure) {
        AlertDialog(
            onDismissRequest = { showDisclosure = false },
            title = { Text("Share your location?") },
            text = {
                Text(
                    "StrangerHelp will share your live location with the task " +
                    "poster while you complete this task. You can stop sharing " +
                    "any time from the notification or this screen."
                )
            },
            confirmButton = {
                Button(onClick = {
                    showDisclosure = false
                    permissionLauncher.launch(requiredPermissions)
                }) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisclosure = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
"""

old_helper_actions_pattern = r"@Composable\s*fun HelperTrackingActions.*?if \(showDisclosure\) \{.*?\}\s*\}"
content = re.sub(old_helper_actions_pattern, helper_actions_new.strip(), content, flags=re.DOTALL)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TrackingComponents.kt", "w") as f:
    f.write(content)

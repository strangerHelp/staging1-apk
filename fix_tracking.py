import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TrackingComponents.kt", "r") as f:
    content = f.read()

# I will find the @Composable fun HelperTrackingControls up to @Composable fun PosterTrackingView
start_idx = content.find("@Composable\nfun HelperTrackingControls")
end_idx = content.find("@Composable\nfun PosterTrackingView")

if start_idx != -1 and end_idx != -1:
    new_controls = """@Composable
fun HelperTrackingControls(
    task: Task,
    viewModel: TaskDetailViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isTracking by viewModel.isTracking.collectAsState()
    val trackingError by viewModel.trackingError.collectAsState()
    var showDisclosure by remember { mutableStateOf(false) }

    val TrustColor = Color(0xFF10B981) // Green
    val Body = Color.Gray
    val Muted = Color.LightGray
    val Error = Color.Red

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
            viewModel.startTracking(task._id, context)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when {
            isTracking -> {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = TrustColor.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(TrustColor)
                                    .animateContentSize()
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "📍 Live tracking active",
                                fontSize = 13.sp,
                                color = TrustColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Button(
                            onClick = { viewModel.stopTracking(task._id, context) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Error
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                "Stop",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            else -> {
                OutlinedButton(
                    onClick = { showDisclosure = true },
                    modifier = modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TrustColor
                    ),
                    border = BorderStroke(1.dp, TrustColor)
                ) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = TrustColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "📍 Start Task (Share Live Location)",
                        color = TrustColor
                    )
                }
            }
        }

        trackingError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    if (showDisclosure) {
        AlertDialog(
            onDismissRequest = { showDisclosure = false },
            title = { Text("Share Your Location") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "StrangerHelp will share your live location with the task poster while you complete this task.",
                        fontSize = 14.sp,
                        color = Body
                    )
                    Text(
                        "You can stop sharing anytime.",
                        fontSize = 13.sp,
                        color = Muted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDisclosure = false
                        permissionLauncher.launch(requiredPermissions)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TrustColor
                    )
                ) {
                    Text("Start Sharing", color = Color.White)
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
    
    content = content[:start_idx] + new_controls + content[end_idx:]

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TrackingComponents.kt", "w") as f:
    f.write(content)


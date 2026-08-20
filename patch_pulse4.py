import re

with open('app/src/main/java/com/strangerhelp/app/ui/screens/pulse/PulseScreen.kt', 'r') as f:
    content = f.read()

# Add BatteryMonitor state
import_injection = """import com.strangerhelp.app.util.MapHelper
import com.strangerhelp.app.utils.BatteryMonitor"""
content = content.replace("import com.strangerhelp.app.util.MapHelper", import_injection)


state_injection = """    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    
    val isBatterySaver by BatteryMonitor.isBatterySaverMode.collectAsStateWithLifecycle()
    
    // Simulating map refresh frequency adapting to battery state
    LaunchedEffect(isBatterySaver) {
        while(true) {
            val delayMillis = if (isBatterySaver) 30000L else 10000L
            kotlinx.coroutines.delay(delayMillis)
            // Simulated map data refresh...
        }
    }"""
content = content.replace("    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)", state_injection)


# Add UI Indicator on map
# Looking for:
#         Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
#             if (downloadProgress != null) {

ui_injection = """        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isBatterySaver) {
                Card(
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.BatterySaver, contentDescription = "Battery Saver", tint = Saffron, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Low Battery: Reduced Refresh", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Saffron)
                    }
                }
            }

            if (downloadProgress != null) {"""
content = content.replace("""        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (downloadProgress != null) {""", ui_injection)


with open('app/src/main/java/com/strangerhelp/app/ui/screens/pulse/PulseScreen.kt', 'w') as f:
    f.write(content)

import re

file_path = "app/src/main/java/com/strangerhelp/app/ui/screens/pulse/PulseScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

old_pins = """    val pins = remember {
        List(15) {
            MapPin(
                location = LatLng(28.6139 + (Random.nextDouble() - 0.5) * 0.1, 77.2090 + (Random.nextDouble() - 0.5) * 0.1),
                isHelper = Random.nextBoolean()
            )
        }
    }"""

new_pins = """    var pins by remember {
        mutableStateOf(List(15) {
            MapPin(
                location = LatLng(28.6139 + (Random.nextDouble() - 0.5) * 0.1, 77.2090 + (Random.nextDouble() - 0.5) * 0.1),
                isHelper = Random.nextBoolean()
            )
        })
    }"""

content = content.replace(old_pins, new_pins)

old_effect = """    LaunchedEffect(isBatterySaver) {
        while(true) {
            val delayMillis = if (isBatterySaver) 30000L else 10000L
            kotlinx.coroutines.delay(delayMillis)
            // Simulated map data refresh...
        }
    }"""

new_effect = """    LaunchedEffect(isBatterySaver) {
        while(true) {
            val delayMillis = if (isBatterySaver) 30000L else 10000L
            kotlinx.coroutines.delay(delayMillis)
            
            // Refresh map pins
            pins = List(15) {
                MapPin(
                    location = LatLng(28.6139 + (Random.nextDouble() - 0.5) * 0.1, 77.2090 + (Random.nextDouble() - 0.5) * 0.1),
                    isHelper = Random.nextBoolean()
                )
            }
        }
    }"""

content = content.replace(old_effect, new_effect)

# Now we need to make sure the GeoJsonSource is updated when pins change!
# Wait, the MapLibre view in PulseScreen just draws it once? Let's check how pins are rendered.
with open(file_path, "w") as f:
    f.write(content)

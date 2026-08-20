import re

with open('app/src/main/java/com/strangerhelp/app/ui/screens/pulse/PulseScreen.kt', 'r') as f:
    content = f.read()

target = """    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),"""

replacement = """    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),"""
                
new_replacement = """    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->"""

better_target = """        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            AndroidView("""

better_replacement = """        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (downloadProgress != null) {
                LinearProgressIndicator(
                    progress = { downloadProgress!! / 100f },
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(top = 16.dp, start = 16.dp, end = 16.dp),
                    color = Saffron
                )
            }
            AndroidView("""
            
content = content.replace(better_target, better_replacement, 1)

with open('app/src/main/java/com/strangerhelp/app/ui/screens/pulse/PulseScreen.kt', 'w') as f:
    f.write(content)

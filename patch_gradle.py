import os

path = 'app/build.gradle.kts'
with open(path, 'r') as f:
    content = f.read()

target = "    debugImplementation(\"androidx.compose.ui:ui-tooling\")"
replacement = """    debugImplementation("androidx.compose.ui:ui-tooling")
    
    // Vico Charts
    implementation("com.patrykandpatrick.vico:compose:1.12.0")
    implementation("com.patrykandpatrick.vico:compose-m3:1.12.0")
    implementation("com.patrykandpatrick.vico:core:1.12.0")"""

if target in content:
    content = content.replace(target, replacement)
    with open(path, 'w') as f:
        f.write(content)
        print("Patched build.gradle.kts")
else:
    print("Target not found.")

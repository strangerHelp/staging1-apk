import sys
import glob

color_replacements = {
    "TrustColor": "androidx.compose.ui.graphics.Color(0xFF10B981)",
    "ErrorColor": "androidx.compose.ui.graphics.Color(0xFFEE0000)",
    "MutedText": "androidx.compose.ui.graphics.Color(0xFF666666)",
    "SurfaceVariantColor": "androidx.compose.ui.graphics.Color(0xFFF5F5F5)"
}

files = glob.glob("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/*.kt")

for file in files:
    with open(file, "r") as f:
        content = f.read()
    
    # Remove top-level val TrustColor = ... to avoid ambiguity
    content = content.replace("val TrustColor = Color(0xFF10B981)", "")
    content = content.replace("val TrustColor = Color(0xFF2A9D8F)", "")
    content = content.replace("val TrustColor = Color(0xFF00C853)", "")
    content = content.replace("val ErrorColor = Color(0xFFEE0000)", "")
    content = content.replace("val MutedText = Color(0xFF666666)", "")
    content = content.replace("val SurfaceVariantColor = Color(0xFFF5F5F5)", "")
    
    for key, val in color_replacements.items():
        content = content.replace(key, val)
        
    with open(file, "w") as f:
        f.write(content)


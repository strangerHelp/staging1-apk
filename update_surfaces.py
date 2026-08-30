import os
import re

def process_file(path):
    with open(path, 'r') as f:
        content = f.read()

    # We want to find Surface(...) and if it has a shape but NO border, add a border.
    # We will use regex to find Surface(...) block. Since parenthesis can be nested, regex is tricky.
    # Instead, we will look for `Surface(` and if the next characters up to `{` have `shape` but not `border`.
    
    # Let's just do a simple replacement for the specific ones we know.
    # PathActiveScreen
    original = content
    content = re.sub(r'Surface\(\s*color = Color\(0xFFF0F0F0\),\s*shape = RoundedCornerShape\(16\.dp\)\s*\)', 
                     r'Surface(\n                        color = Color(0xFFF0F0F0),\n                        shape = RoundedCornerShape(16.dp),\n                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)\n                    )', content)
    content = re.sub(r'Surface\(color = Color\(0xFFF0F0F0\), shape = RoundedCornerShape\(6\.dp\)\)', 
                     r'Surface(color = Color(0xFFF0F0F0), shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline))', content)

    # TaskDetailScreen
    content = re.sub(r'Surface\(color = containerColor, shape = RoundedCornerShape\(16\.dp\)\)', 
                     r'Surface(color = containerColor, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline))', content)
    content = re.sub(r'Surface\(color = AccentOrange, shape = RoundedCornerShape\(16\.dp\)\)', 
                     r'Surface(color = AccentOrange, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline))', content)

    # MeetsScreen
    content = re.sub(r'Surface\(color = Color\(0xFFE8F5E9\), shape = RoundedCornerShape\(8\.dp\)\)', 
                     r'Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline))', content)
    content = re.sub(r'Surface\(color = Color\(0xFFFFF3E0\), shape = RoundedCornerShape\(8\.dp\)\)', 
                     r'Surface(color = Color(0xFFFFF3E0), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline))', content)
    
    # LandingScreen
    content = re.sub(r'Surface\(color = verifiedCyan, shape = RoundedCornerShape\(4\.dp\)\)', 
                     r'Surface(color = verifiedCyan, shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline))', content)

    if original != content:
        # Check imports
        if 'BorderStroke' not in content:
            content = content.replace('import androidx.compose.ui.Modifier', 'import androidx.compose.ui.Modifier\nimport androidx.compose.foundation.BorderStroke')
        with open(path, 'w') as f:
            f.write(content)
        print(f"Updated {path}")

for root, dirs, files in os.walk('app/src/main/java/com/strangerhelp/app/ui/screens'):
    for file in files:
        if file.endswith('.kt'):
            process_file(os.path.join(root, file))

import os
import re

replacements = [
    (r'BorderStroke\(1\.dp, Color\(0xFFE5E5E5\)\)', r'BorderStroke(1.dp, MaterialTheme.colorScheme.outline)'),
    (r'BorderStroke\(1\.dp, Color\(0xFFE0E0E0\)\)', r'BorderStroke(1.dp, MaterialTheme.colorScheme.outline)'),
    (r'BorderStroke\(1\.dp, OutlineColor\)', r'BorderStroke(1.dp, MaterialTheme.colorScheme.outline)'),
    (r'BorderStroke\(1\.dp, CardOutlineColor\)', r'BorderStroke(1.dp, MaterialTheme.colorScheme.outline)'),
]

def process_file(path):
    with open(path, 'r') as f:
        content = f.read()
    
    original = content
    for pattern, repl in replacements:
        content = re.sub(pattern, repl, content)
    
    if content != original:
        if 'MaterialTheme.colorScheme.outline' in content and 'androidx.compose.material3.MaterialTheme' not in content:
            if 'import androidx.compose.material3.*' not in content:
                content = content.replace('import androidx.compose.runtime.*', 'import androidx.compose.runtime.*\nimport androidx.compose.material3.MaterialTheme')

        with open(path, 'w') as f:
            f.write(content)
        print(f"Updated {path}")

for root, dirs, files in os.walk('app/src/main/java/com/strangerhelp/app/ui/screens'):
    for file in files:
        if file.endswith('.kt'):
            process_file(os.path.join(root, file))

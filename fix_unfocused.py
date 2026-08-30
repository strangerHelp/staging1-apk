import os
import re

def process_file(path):
    with open(path, 'r') as f:
        content = f.read()
    
    original = content
    content = re.sub(r'unfocusedBorderColor = [^,)]+', 'unfocusedBorderColor = MaterialTheme.colorScheme.outline', content)
    
    if content != original:
        with open(path, 'w') as f:
            f.write(content)
        print(f"Updated {path}")

for root, dirs, files in os.walk('app/src/main/java/com/strangerhelp/app/ui/screens'):
    for file in files:
        if file.endswith('.kt'):
            process_file(os.path.join(root, file))

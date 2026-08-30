import os
import re

def process_file(path):
    with open(path, 'r') as f:
        content = f.read()

    # We need to make sure MaterialTheme is imported if we are using MaterialTheme.colorScheme.outline
    # It usually is, but let's be careful.
    if 'Hairline' in content:
        # replace Hairline with MaterialTheme.colorScheme.outline
        # But wait, Hairline is sometimes used in import: import com.strangerhelp.app.ui.theme.Hairline
        # we should remove that import
        content = re.sub(r'import com.strangerhelp.app.ui.theme.Hairline\n?', '', content)
        content = re.sub(r'\bHairlineColor\b', 'MaterialTheme.colorScheme.outline', content)
        content = re.sub(r'\bHairline\b', 'MaterialTheme.colorScheme.outline', content)

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

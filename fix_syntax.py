import os
import re

def fix_file(path, replacements):
    with open(path, 'r') as f:
        content = f.read()
    
    for old, new in replacements:
        content = content.replace(old, new)
        
    with open(path, 'w') as f:
        f.write(content)

fix_file('app/src/main/java/com/strangerhelp/app/ui/screens/auth/ResetPasswordScreen.kt', [
    ('unfocusedBorderColor = MaterialTheme.colorScheme.outline\n', 'unfocusedBorderColor = MaterialTheme.colorScheme.outline,\n'),
    ('unfocusedBorderColor = MaterialTheme.colorScheme.outline)', 'unfocusedBorderColor = MaterialTheme.colorScheme.outline)') # check this
])

fix_file('app/src/main/java/com/strangerhelp/app/ui/screens/post/PostTaskScreen.kt', [
    ('unfocusedBorderColor = MaterialTheme.colorScheme.outline\n            singleLine = true,', 'unfocusedBorderColor = MaterialTheme.colorScheme.outline),\n            singleLine = true,'),
    ('unfocusedBorderColor = MaterialTheme.colorScheme.outline\n            )', 'unfocusedBorderColor = MaterialTheme.colorScheme.outline)\n            )')
])

fix_file('app/src/main/java/com/strangerhelp/app/ui/screens/profile/ComingSoonScreens.kt', [
    ('unfocusedBorderColor = MaterialTheme.colorScheme.outline\n                    focusedBorderColor = MaterialTheme.colorScheme.primary', 'unfocusedBorderColor = MaterialTheme.colorScheme.outline,\n                    focusedBorderColor = MaterialTheme.colorScheme.primary')
])

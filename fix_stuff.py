import os

# 1. Fix Models.kt
models_path = 'app/src/main/java/com/strangerhelp/app/data/model/Models.kt'
with open(models_path, 'r') as f:
    content = f.read()

if 'val skills: String' not in content:
    content = content.replace('val bio: String = "",\n    val verified: Int = 0,', 'val bio: String = "",\n    val skills: String = "[]",\n    val verified: Int = 0,')
    with open(models_path, 'w') as f:
        f.write(content)

# 2. Fix ProfileScreen.kt package and imports
profile_path = 'app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt'
with open(profile_path, 'r') as f:
    content = f.read()

# Remove the broken top imports if present
content = content.replace('import androidx.compose.foundation.layout.ExperimentalLayoutApiimport androidx.compose.foundation.layout.FlowRow', '')
content = content.replace('package com.strangerhelp.app.ui.screens.profile', 'package com.strangerhelp.app.ui.screens.profile\n\nimport androidx.compose.foundation.layout.ExperimentalLayoutApi\nimport androidx.compose.foundation.layout.FlowRow')

# Fix JSONArray
content = content.replace('org.json.JSONArray(user.skills).let { arr -> List(arr.length()) { arr.getString(it) } }', 'org.json.JSONArray(user.skills ?: "[]").let { arr -> List(arr.length()) { arr.getString(it) } }')

with open(profile_path, 'w') as f:
    f.write(content)


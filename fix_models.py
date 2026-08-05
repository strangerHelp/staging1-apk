import os

models_path = 'app/src/main/java/com/strangerhelp/app/data/model/Models.kt'
with open(models_path, 'r') as f:
    content = f.read()

# Let's insert before '@Entity(tableName = "conversations")'
target = '@Entity(tableName = "conversations")'
insert_text = '    val trackingActive: Boolean = false,\n    val helperLat: Double? = null,\n    val helperLng: Double? = null,\n    val visibility: String = "public",\n    val inviteCode: String? = null,\n'

if insert_text not in content:
    content = content.replace('    val createdAt: String = "",\n)\n\n@Entity(tableName = "conversations")',
                              '    val createdAt: String = "",\n' + insert_text + ')\n\n@Entity(tableName = "conversations")')
    
with open(models_path, 'w') as f:
    f.write(content)


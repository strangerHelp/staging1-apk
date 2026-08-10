import os

path = 'app/src/main/java/com/strangerhelp/app/data/model/Models.kt'
with open(path, 'r') as f:
    content = f.read()

new_entity = '''@Entity(tableName = "search_history")
data class SearchHistory(
    @PrimaryKey val query: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

'''

if 'SearchHistory' not in content:
    content += '\n' + new_entity
    with open(path, 'w') as f:
        f.write(content)
    print("Added SearchHistory model")
else:
    print("Already exists")

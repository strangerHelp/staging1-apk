import os

path = 'app/src/main/java/com/strangerhelp/app/data/local/AppDatabase.kt'
with open(path, 'r') as f:
    content = f.read()

target1 = 'import com.strangerhelp.app.data.local.dao.HelpRequestDao'
replacement1 = 'import com.strangerhelp.app.data.local.dao.HelpRequestDao\nimport com.strangerhelp.app.data.local.dao.SearchHistoryDao\nimport com.strangerhelp.app.data.model.SearchHistory'

target2 = '@Database(entities = [Task::class, Conversation::class, Notification::class, Meet::class, HelpRequest::class], version = 3, exportSchema = false)'
replacement2 = '@Database(entities = [Task::class, Conversation::class, Notification::class, Meet::class, HelpRequest::class, SearchHistory::class], version = 4, exportSchema = false)'

target3 = '    abstract fun helpRequestDao(): HelpRequestDao'
replacement3 = '    abstract fun helpRequestDao(): HelpRequestDao\n    abstract fun searchHistoryDao(): SearchHistoryDao'

if target1 in content and target2 in content and target3 in content:
    content = content.replace(target1, replacement1)
    content = content.replace(target2, replacement2)
    content = content.replace(target3, replacement3)
    with open(path, 'w') as f:
        f.write(content)
    print("Patched AppDatabase")
else:
    print("Targets not found")

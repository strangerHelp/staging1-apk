import os

path = 'app/src/main/java/com/strangerhelp/app/data/local/dao/SearchHistoryDao.kt'
with open(path, 'r') as f:
    content = f.read()

content = content.replace('suspend fun insertSearch', 'suspend fun insertSearch')
content = content.replace('suspend fun deleteSearch(query: String)', 'suspend fun deleteSearch(query: String): Int')
content = content.replace('suspend fun clearHistory()', 'suspend fun clearHistory(): Int')

with open(path, 'w') as f:
    f.write(content)

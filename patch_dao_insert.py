import os

path = 'app/src/main/java/com/strangerhelp/app/data/local/dao/SearchHistoryDao.kt'
with open(path, 'r') as f:
    content = f.read()

content = content.replace('suspend fun insertSearch(searchHistory: SearchHistory)', 'suspend fun insertSearch(searchHistory: SearchHistory): Long')

with open(path, 'w') as f:
    f.write(content)

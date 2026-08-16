import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatListScreen.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

target = '''    fun loadConversations() {
        scope.launch {
            try {
                val res = ApiClient.api.getConversations()
                if (res.isSuccessful) conversations = res.body() ?: emptyList()
            } catch (e: Exception) {}
            loading = false
        }
    }

    LaunchedEffect(Unit) { loadConversations() }'''
    
replacement = '''    fun loadConversations() {
        scope.launch {
            try {
                val res = ApiClient.api.getConversations()
                if (res.isSuccessful) conversations = res.body() ?: emptyList()
            } catch (e: Exception) {}
            loading = false
        }
    }

    LaunchedEffect(Unit) { 
        loadConversations() 
        while(true) {
            kotlinx.coroutines.delay(5000)
            loadConversations()
        }
    }'''

content = content.replace(target, replacement)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Added polling to ChatListScreen")

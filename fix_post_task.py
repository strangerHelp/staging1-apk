import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/post/PostTaskScreen.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

target = '''                val mediaType = okhttp3.MediaType.parse("text/plain")
                val t = okhttp3.RequestBody.create(mediaType, title)
                val d = okhttp3.RequestBody.create(mediaType, description)
                val c = okhttp3.RequestBody.create(mediaType, category)
                val b = okhttp3.RequestBody.create(mediaType, budget)
                val l = okhttp3.RequestBody.create(mediaType, location)
                val u = okhttp3.RequestBody.create(mediaType, if (isUrgent) "1" else "0")
                val v = okhttp3.RequestBody.create(mediaType, if (isPrivate) "private" else "public")'''
                
replacement = '''                val mediaType = okhttp3.MediaType.parse("text/plain")
                val t = okhttp3.RequestBody.create(mediaType, title)
                val d = okhttp3.RequestBody.create(mediaType, description)
                val c = okhttp3.RequestBody.create(mediaType, category)
                val b = okhttp3.RequestBody.create(mediaType, budget)
                val l = okhttp3.RequestBody.create(mediaType, location)
                val u = okhttp3.RequestBody.create(mediaType, if (isUrgent) "1" else "0")
                val v = okhttp3.RequestBody.create(mediaType, if (isPrivate) "private" else "public")'''

content = content.replace(target, replacement)

target2 = '''import kotlinx.coroutines.launch'''
replacement2 = '''import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody'''
content = content.replace(target2, replacement2)

target3 = '''                val mediaType = okhttp3.MediaType.parse("text/plain")
                val t = okhttp3.RequestBody.create(mediaType, title)
                val d = okhttp3.RequestBody.create(mediaType, description)
                val c = okhttp3.RequestBody.create(mediaType, category)
                val b = okhttp3.RequestBody.create(mediaType, budget)
                val l = okhttp3.RequestBody.create(mediaType, location)
                val u = okhttp3.RequestBody.create(mediaType, if (isUrgent) "1" else "0")
                val v = okhttp3.RequestBody.create(mediaType, if (isPrivate) "private" else "public")'''
                
replacement3 = '''                val mediaType = "text/plain".toMediaTypeOrNull()
                val t = title.toRequestBody(mediaType)
                val d = description.toRequestBody(mediaType)
                val c = category.toRequestBody(mediaType)
                val b = budget.toRequestBody(mediaType)
                val l = location.toRequestBody(mediaType)
                val u = (if (isUrgent) "1" else "0").toRequestBody(mediaType)
                val v = (if (isPrivate) "private" else "public").toRequestBody(mediaType)'''
                
content = content.replace(target3, replacement3)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Fixed okhttp3 usage")

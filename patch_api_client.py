import re

with open('app/src/main/java/com/strangerhelp/app/data/api/ApiClient.kt', 'r') as f:
    content = f.read()

inject_method = """    fun clearSession() {
        cookieStore.clear()
        prefs.edit().remove("cookies").apply()
    }

    fun injectCookies(cookieString: String) {
        val url = HttpUrl.Builder().scheme("https").host("strangerhelp.com").build()
        val pairs = cookieString.split(";")
        for (pair in pairs) {
            val parts = pair.trim().split("=", limit = 2)
            if (parts.size == 2) {
                val name = parts[0].trim()
                val value = parts[1].trim()
                val cookie = Cookie.parse(url, "$name=$value; Path=/; Secure; HttpOnly")
                if (cookie != null) {
                    cookieStore.removeAll { it.name == cookie.name }
                    cookieStore.add(cookie)
                }
            }
        }
        val set = cookieStore.map { "${it.name}|${it.value}|${it.path}" }.toSet()
        prefs.edit().putStringSet("cookies", set).apply()
    }"""

new_content = content.replace("    fun clearSession() {\n        cookieStore.clear()\n        prefs.edit().remove(\"cookies\").apply()\n    }", inject_method)

with open('app/src/main/java/com/strangerhelp/app/data/api/ApiClient.kt', 'w') as f:
    f.write(new_content)
print("Patched ApiClient.kt")

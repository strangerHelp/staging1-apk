import re

file_path = "app/src/main/java/com/strangerhelp/app/ui/screens/path/PathViewModel.kt"
with open(file_path, "r") as f:
    content = f.read()

old_content = """    private fun parseUTC(timestamp: String): Date {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.parse(timestamp) ?: Date()
    }"""

new_content = """    private fun parseUTC(timestamp: String): Date {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.parse(timestamp) ?: Date()
        } catch (e: Exception) {
            try {
                val sdf2 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                sdf2.timeZone = TimeZone.getTimeZone("UTC")
                sdf2.parse(timestamp) ?: Date()
            } catch (e2: Exception) {
                Date()
            }
        }
    }"""

content = content.replace(old_content, new_content)

with open(file_path, "w") as f:
    f.write(content)

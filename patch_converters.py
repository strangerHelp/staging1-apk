import re

with open("app/src/main/java/com/strangerhelp/app/data/local/Converters.kt", "r") as f:
    text = f.read()

converter_code = """
    @TypeConverter
    fun fromAttendeeList(value: List<com.strangerhelp.app.data.model.Attendee>?): String {
        if (value == null) return "[]"
        return gson.toJson(value)
    }

    @TypeConverter
    fun toAttendeeList(value: String?): List<com.strangerhelp.app.data.model.Attendee> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<com.strangerhelp.app.data.model.Attendee>>() {}.type
        return gson.fromJson(value, listType)
    }
}
"""

text = text.replace("}", converter_code, 1)

# Because there are multiple } in the file?
# Actually replace the last } only.
idx = text.rfind("}")
if idx != -1:
    clean = text[:idx] + converter_code
    with open("app/src/main/java/com/strangerhelp/app/data/local/Converters.kt", "w") as f:
        f.write(clean)

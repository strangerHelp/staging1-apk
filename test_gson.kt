import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun main() {
    val json = """{"id":"56615445624d854abf28a0d6","visibility":"public","inviteCode":null}"""
    val type = object : TypeToken<Map<String, String>>() {}.type
    try {
        val map: Map<String, String> = Gson().fromJson(json, type)
        println(map)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

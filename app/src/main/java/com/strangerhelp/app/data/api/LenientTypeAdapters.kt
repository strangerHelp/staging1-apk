package com.strangerhelp.app.data.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException

/**
 * TypeAdapter for Int / Integer that handles strings like "100", empty strings, decimals, and nulls.
 */
class LenientIntTypeAdapter : TypeAdapter<Int>() {
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: Int?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    @Throws(IOException::class)
    override fun read(reader: JsonReader): Int {
        return when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                0
            }
            JsonToken.BOOLEAN -> {
                if (reader.nextBoolean()) 1 else 0
            }
            JsonToken.NUMBER -> {
                val numStr = reader.nextString()
                numStr.toIntOrNull() ?: numStr.toDoubleOrNull()?.toInt() ?: 0
            }
            JsonToken.STRING -> {
                val str = reader.nextString().trim()
                if (str.isEmpty() || str.equals("null", ignoreCase = true)) {
                    0
                } else {
                    str.toIntOrNull() ?: str.toDoubleOrNull()?.toInt() ?: 0
                }
            }
            else -> {
                reader.skipValue()
                0
            }
        }
    }
}

/**
 * TypeAdapter for Double that handles numbers, strings, and null values gracefully.
 */
class LenientDoubleTypeAdapter : TypeAdapter<Double?>() {
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: Double?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    @Throws(IOException::class)
    override fun read(reader: JsonReader): Double? {
        return when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                null
            }
            JsonToken.NUMBER -> {
                reader.nextDouble()
            }
            JsonToken.STRING -> {
                val str = reader.nextString().trim()
                if (str.isEmpty() || str.equals("null", ignoreCase = true)) {
                    null
                } else {
                    str.toDoubleOrNull()
                }
            }
            else -> {
                reader.skipValue()
                null
            }
        }
    }
}

/**
 * TypeAdapter for Boolean that handles boolean literals, numbers (0/1), and string representations ("true", "1").
 */
class LenientBooleanTypeAdapter : TypeAdapter<Boolean>() {
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: Boolean?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    @Throws(IOException::class)
    override fun read(reader: JsonReader): Boolean {
        return when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                false
            }
            JsonToken.BOOLEAN -> {
                reader.nextBoolean()
            }
            JsonToken.NUMBER -> {
                reader.nextInt() != 0
            }
            JsonToken.STRING -> {
                val str = reader.nextString().trim()
                str.equals("true", ignoreCase = true) || str == "1"
            }
            else -> {
                reader.skipValue()
                false
            }
        }
    }
}

/**
 * TypeAdapter for List<String> that handles both JSON array [] and stringified array "[]" or single strings.
 */
class LenientStringListTypeAdapter : TypeAdapter<List<String>>() {
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: List<String>?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.beginArray()
            for (item in value) {
                out.value(item)
            }
            out.endArray()
        }
    }

    @Throws(IOException::class)
    override fun read(reader: JsonReader): List<String> {
        return when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                emptyList()
            }
            JsonToken.BEGIN_ARRAY -> {
                val list = mutableListOf<String>()
                reader.beginArray()
                while (reader.hasNext()) {
                    when (reader.peek()) {
                        JsonToken.STRING, JsonToken.NUMBER, JsonToken.BOOLEAN -> list.add(reader.nextString())
                        JsonToken.NULL -> reader.nextNull()
                        else -> reader.skipValue()
                    }
                }
                reader.endArray()
                list
            }
            JsonToken.STRING -> {
                val str = reader.nextString().trim()
                if (str.isEmpty() || str == "[]" || str.equals("null", ignoreCase = true)) {
                    emptyList()
                } else if (str.startsWith("[") && str.endsWith("]")) {
                    val inner = str.substring(1, str.length - 1).trim()
                    if (inner.isEmpty()) {
                        emptyList()
                    } else {
                        inner.split(",").map { it.trim().removeSurrounding("\"").removeSurrounding("'") }.filter { it.isNotEmpty() }
                    }
                } else {
                    listOf(str)
                }
            }
            else -> {
                reader.skipValue()
                emptyList()
            }
        }
    }
}

class LenientStringListTypeAdapterFactory : com.google.gson.TypeAdapterFactory {
    override fun <T : Any?> create(gson: Gson, type: com.google.gson.reflect.TypeToken<T>): TypeAdapter<T>? {
        val rawType = type.rawType
        if (java.util.List::class.java.isAssignableFrom(rawType)) {
            val typeArgs = (type.type as? java.lang.reflect.ParameterizedType)?.actualTypeArguments
            if (typeArgs != null && typeArgs.isNotEmpty() && typeArgs[0] == String::class.java) {
                @Suppress("UNCHECKED_CAST")
                return LenientStringListTypeAdapter() as TypeAdapter<T>
            }
        }
        return null
    }
}

object GsonFactory {
    fun createLenientGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .registerTypeAdapterFactory(LenientStringListTypeAdapterFactory())
            .registerTypeAdapter(Int::class.java, LenientIntTypeAdapter())
            .registerTypeAdapter(java.lang.Integer::class.java, LenientIntTypeAdapter())
            .registerTypeAdapter(Double::class.javaObjectType, LenientDoubleTypeAdapter())
            .registerTypeAdapter(Double::class.javaPrimitiveType, LenientDoubleTypeAdapter())
            .registerTypeAdapter(Boolean::class.java, LenientBooleanTypeAdapter())
            .registerTypeAdapter(java.lang.Boolean::class.java, LenientBooleanTypeAdapter())
            .create()
    }
}

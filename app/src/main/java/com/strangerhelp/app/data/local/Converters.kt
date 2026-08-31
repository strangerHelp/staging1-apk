package com.strangerhelp.app.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.strangerhelp.app.data.model.ClaimRequest
import com.strangerhelp.app.data.model.ClaimedUser
import com.strangerhelp.app.data.model.Attendee

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        if (value == null) return "[]"
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromClaimRequestList(value: List<ClaimRequest>?): String {
        if (value == null) return "[]"
        return gson.toJson(value)
    }

    @TypeConverter
    fun toClaimRequestList(value: String?): List<ClaimRequest> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<ClaimRequest>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromClaimedUserList(value: List<ClaimedUser>?): String {
        if (value == null) return "[]"
        return gson.toJson(value)
    }

    @TypeConverter
    fun toClaimedUserList(value: String?): List<ClaimedUser> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<ClaimedUser>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromAttendeeList(value: List<Attendee>?): String {
        if (value == null) return "[]"
        return gson.toJson(value)
    }

    @TypeConverter
    fun toAttendeeList(value: String?): List<Attendee> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<Attendee>>() {}.type
        return gson.fromJson(value, listType)
    }
}

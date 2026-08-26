package com.strangerhelp.app.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object TimeUtils {
    fun formatTime(timestamp: String?): String {
        if (timestamp == null) return ""
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(timestamp) ?: return ""

            val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            outputFormat.timeZone = TimeZone.getDefault()
            outputFormat.format(date)
        } catch (e: Exception) {
            try {
                val inputFormat2 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                inputFormat2.timeZone = TimeZone.getTimeZone("UTC")
                val date2 = inputFormat2.parse(timestamp) ?: return ""
                val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                outputFormat.timeZone = TimeZone.getDefault()
                outputFormat.format(date2)
            } catch (_: Exception) { "" }
        }
    }

    fun getTimeAgo(timestamp: String?): String {
        if (timestamp == null) return ""
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(timestamp) ?: return ""
            val now = Date()
            val diffMs = now.time - date.time
            val diffSec = diffMs / 1000

            when {
                diffSec < 60 -> "Just now"
                diffSec < 3600 -> "${diffSec / 60} min ago"
                diffSec < 86400 -> "${diffSec / 3600} hours ago"
                diffSec < 604800 -> "${diffSec / 86400} days ago"
                else -> {
                    val outputFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                    outputFormat.timeZone = TimeZone.getDefault()
                    outputFormat.format(date)
                }
            }
        } catch (e: Exception) {
            try {
                val inputFormat2 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                inputFormat2.timeZone = TimeZone.getTimeZone("UTC")
                val date2 = inputFormat2.parse(timestamp) ?: return ""
                val now = Date()
                val diffMs = now.time - date2.time
                val diffSec = diffMs / 1000

                when {
                    diffSec < 60 -> "Just now"
                    diffSec < 3600 -> "${diffSec / 60} min ago"
                    diffSec < 86400 -> "${diffSec / 3600} hours ago"
                    diffSec < 604800 -> "${diffSec / 86400} days ago"
                    else -> {
                        val outputFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                        outputFormat.timeZone = TimeZone.getDefault()
                        outputFormat.format(date2)
                    }
                }
            } catch (_: Exception) { "" }
        }
    }


    fun formatMessageTime(utcTimestamp: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(utcTimestamp) ?: return ""

            val cal = java.util.Calendar.getInstance().apply { time = date }
            val today = java.util.Calendar.getInstance()

            val isToday = cal.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
                    cal.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR)

            val yesterday = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -1) }
            val isYesterday = cal.get(java.util.Calendar.YEAR) == yesterday.get(java.util.Calendar.YEAR) &&
                    cal.get(java.util.Calendar.DAY_OF_YEAR) == yesterday.get(java.util.Calendar.DAY_OF_YEAR)

            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            timeFormat.timeZone = TimeZone.getDefault()

            when {
                isToday -> timeFormat.format(date)
                isYesterday -> "Yesterday ${timeFormat.format(date)}"
                else -> {
                    val dateFormat = SimpleDateFormat("dd MMM hh:mm a", Locale.getDefault())
                    dateFormat.timeZone = TimeZone.getDefault()
                    dateFormat.format(date)
                }
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun formatMessageTimeShort(utcTimestamp: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(utcTimestamp) ?: return ""

            val now = Date()
            val diffMs = now.time - date.time
            val diffMins = diffMs / 60_000

            when {
                diffMins < 1 -> "Just now"
                diffMins < 60 -> "${diffMins}m ago"
                diffMins < 1440 -> "${diffMins / 60}h ago"
                diffMins < 10080 -> "${diffMins / 1440}d ago"
                else -> {
                    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                    dateFormat.timeZone = TimeZone.getDefault()
                    dateFormat.format(date)
                }
            }
        } catch (e: Exception) {
            ""
        }
    }

}

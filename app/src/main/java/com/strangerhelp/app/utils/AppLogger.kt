package com.strangerhelp.app.utils

import android.util.Log

/**
 * Lightweight logging service for monitoring app stability,
 * capturing component mount failures and unhandled exceptions.
 */
object AppLogger {
    private const val DEFAULT_TAG = "AppLogger"

    fun d(tag: String = DEFAULT_TAG, message: String) {
        try {
            Log.d(tag, message)
        } catch (_: Throwable) {
            println("[$tag] D: $message")
        }
    }

    fun i(tag: String = DEFAULT_TAG, message: String) {
        try {
            Log.i(tag, message)
        } catch (_: Throwable) {
            println("[$tag] I: $message")
        }
    }

    fun w(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        try {
            Log.w(tag, message, throwable)
        } catch (_: Throwable) {
            println("[$tag] W: $message ${throwable?.message.orEmpty()}")
        }
    }

    fun e(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        try {
            Log.e(tag, message, throwable)
        } catch (_: Throwable) {
            System.err.println("[$tag] E: $message ${throwable?.message.orEmpty()}")
            throwable?.printStackTrace()
        }
    }
}

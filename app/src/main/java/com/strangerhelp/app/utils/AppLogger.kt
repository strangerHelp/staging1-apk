package com.strangerhelp.app.utils

import android.util.Log

/**
 * Lightweight logging service for monitoring app stability,
 * capturing component mount failures and unhandled exceptions.
 */
object AppLogger {
    private const val DEFAULT_TAG = "AppLogger"

    fun d(tag: String = DEFAULT_TAG, message: String) {
        Log.d(tag, message)
    }

    fun i(tag: String = DEFAULT_TAG, message: String) {
        Log.i(tag, message)
    }

    fun w(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        Log.w(tag, message, throwable)
    }

    fun e(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        // In a production environment, this would forward the error to a service 
        // like Sentry, Firebase Crashlytics, or Datadog.
    }
}

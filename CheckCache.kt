package com.strangerhelp.app

import android.content.Context
import org.maplibre.android.offline.OfflineManager

fun setCacheLimit(context: Context) {
    val manager = OfflineManager.getInstance(context)
    manager.setMaximumAmbientCacheSize(1024 * 1024 * 100, object : OfflineManager.FileSourceCallback {
        override fun onSuccess() {}
        override fun onError(message: String) {}
    })
}

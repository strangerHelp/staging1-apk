package com.strangerhelp.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Handles the "Stop sharing" action from the tracking notification.
 * Kept as a receiver so the notification action works even if the activity
 * has been destroyed.
 */
class StopTrackingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        TrackingService.stop(context)
    }
}

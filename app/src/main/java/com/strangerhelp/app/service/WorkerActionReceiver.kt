package com.strangerhelp.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Optional broadcast receiver to stop the service from outside the app,
 * for example from an ongoing notification action or a widget.
 */
class WorkerActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            WorkerTrackingService.ACTION_STOP ->
                WorkerTrackingService.stop(context)
        }
    }
}

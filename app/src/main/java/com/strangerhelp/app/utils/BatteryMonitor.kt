package com.strangerhelp.app.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object BatteryMonitor {
    private val _isBatterySaverMode = MutableStateFlow(false)
    val isBatterySaverMode: StateFlow<Boolean> = _isBatterySaverMode.asStateFlow()

    fun init(context: Context) {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_BATTERY_OKAY)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        
        val initialBatteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = initialBatteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = initialBatteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (level != -1 && scale != -1) {
            val batteryPct = level * 100 / scale.toFloat()
            _isBatterySaverMode.value = batteryPct <= 15f
        }

        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_BATTERY_LOW -> _isBatterySaverMode.value = true
                    Intent.ACTION_BATTERY_OKAY -> _isBatterySaverMode.value = false
                    Intent.ACTION_POWER_CONNECTED -> _isBatterySaverMode.value = false
                    Intent.ACTION_POWER_DISCONNECTED -> {
                        // Check level again
                        val curLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                        val curScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                        if (curLevel != -1 && curScale != -1) {
                            _isBatterySaverMode.value = (curLevel * 100 / curScale.toFloat()) <= 15f
                        }
                    }
                }
            }
        }, filter)
    }
}

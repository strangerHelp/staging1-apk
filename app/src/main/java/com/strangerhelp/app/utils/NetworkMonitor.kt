package com.strangerhelp.app.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NetworkMonitor {
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private var connectivityManager: ConnectivityManager? = null

    fun init(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        connectivityManager = cm
        if (cm == null) {
            _isOnline.value = true
            return
        }

        // Check initial state
        _isOnline.value = checkConnectivity(cm)

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isOnline.value = true
                    AppLogger.d("NetworkMonitor", "Network became available")
                }

                override fun onLost(network: Network) {
                    _isOnline.value = checkConnectivity(cm)
                    AppLogger.d("NetworkMonitor", "Network lost. isOnline: ${_isOnline.value}")
                }

                override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                    val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    _isOnline.value = hasInternet
                }
            })
        } catch (e: Exception) {
            AppLogger.e("NetworkMonitor", "Failed to register network callback", e)
        }
    }

    fun isCurrentlyOnline(): Boolean {
        val cm = connectivityManager ?: return true
        return checkConnectivity(cm)
    }

    private fun checkConnectivity(cm: ConnectivityManager): Boolean {
        return try {
            val activeNetwork = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            true // default to true if check fails
        }
    }
}

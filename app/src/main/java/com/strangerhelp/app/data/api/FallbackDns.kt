package com.strangerhelp.app.data.api

import com.strangerhelp.app.utils.AppLogger
import okhttp3.Dns
import java.net.Inet4Address
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * Resilient DNS resolver that prioritizes system DNS, but seamlessly falls back
 * to known Anycast edge IP addresses for strangerhelp.com if Android emulator DNS
 * or local carrier DNS lookup fails with UnknownHostException.
 */
class FallbackDns : Dns {

    companion object {
        private const val TAG = "FallbackDns"

        // Anycast Cloudflare IPv4 edge addresses for strangerhelp.com
        private val KNOWN_STRANGERHELP_IPV4 = listOf(
            byteArrayOf(104.toByte(), 21.toByte(), 23.toByte(), 236.toByte()),
            byteArrayOf(172.toByte(), 67.toByte(), 214.toByte(), 40.toByte())
        )
    }

    override fun lookup(hostname: String): List<InetAddress> {
        // 1. First attempt standard system DNS
        try {
            val addresses = Dns.SYSTEM.lookup(hostname)
            if (addresses.isNotEmpty()) {
                // In Android emulator environments, IPv6 resolution can sometimes result in
                // unreachable routes or timeouts. Prefer IPv4 addresses first.
                return addresses.sortedBy { if (it is Inet4Address) 0 else 1 }
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "System DNS lookup failed for $hostname (${e.javaClass.simpleName}: ${e.message}). Checking fallback...")
        }

        // 2. If host is strangerhelp.com or subdomain, use verified Anycast edge IPs
        if (hostname.equals("strangerhelp.com", ignoreCase = true) ||
            hostname.endsWith(".strangerhelp.com", ignoreCase = true)) {
            AppLogger.i(TAG, "Resolved $hostname via Anycast edge fallback IP addresses")
            val fallbackAddresses = KNOWN_STRANGERHELP_IPV4.mapNotNull { ipBytes ->
                try {
                    InetAddress.getByAddress(hostname, ipBytes)
                } catch (e: Exception) {
                    null
                }
            }
            if (fallbackAddresses.isNotEmpty()) {
                return fallbackAddresses
            }
        }

        // 3. Fallback exhausted
        throw UnknownHostException("Unable to resolve host \"$hostname\": System DNS and Anycast fallback failed")
    }
}

package com.strangerhelp.app.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class GpsCameraHelper(private val context: Context) {
    
    @SuppressLint("MissingPermission")
    private suspend fun getLastLocation(): Location? = withContext(Dispatchers.IO) {
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val locationTask = fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            Tasks.await(locationTask)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun reverseGeocode(lat: Double, lng: Double): String = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lng")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "StrangerHelpAndroidApp")
            
            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(response)
                json.getString("display_name").split(",").take(3).joinToString(",").trim()
            } else {
                "${"%.6f".format(lat)}, ${"%.6f".format(lng)}"
            }
        } catch (e: Exception) {
            "${"%.6f".format(lat)}, ${"%.6f".format(lng)}"
        }
    }

    suspend fun stampPhotoFlow(photo: File): File = withContext(Dispatchers.IO) {
        val location = getLastLocation()
        val lat = location?.latitude ?: 0.0
        val lng = location?.longitude ?: 0.0
        val address = reverseGeocode(lat, lng)
        
        val bitmap = BitmapFactory.decodeFile(photo.absolutePath)
        
        val maxW = 1200
        val scale = if (bitmap.width > maxW) maxW.toFloat() / bitmap.width else 1f
        val scaled = Bitmap.createScaledBitmap(bitmap, 
            (bitmap.width * scale).toInt(), 
            (bitmap.height * scale).toInt(), true)
        val c = Canvas(scaled)

        // Draw watermark bar at bottom
        val barH = 100f
        val barTop = scaled.height - barH
        val paint = Paint().apply { color = Color.argb(216, 23, 23, 23) } // rgba(23,23,23,0.85)
        c.drawRect(0f, barTop, scaled.width.toFloat(), scaled.height.toFloat(), paint)

        // Draw logo dots (cyan circles representing the face logo)
        val logoPaint = Paint().apply { color = Color.parseColor("#50E3C2"); isAntiAlias = true }
        c.drawCircle(30f, barTop + 36f, 6f, logoPaint)
        logoPaint.color = Color.parseColor("#00DFD8")
        c.drawCircle(48f, barTop + 36f, 6f, logoPaint)

        // Text: "StrangerHelp" + "Verified Completion Proof"
        val textPaint = Paint().apply {
            color = Color.WHITE; textSize = 20f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true
        }
        c.drawText("StrangerHelp", 66f, barTop + 30f, textPaint)
        textPaint.apply { color = Color.parseColor("#50E3C2"); textSize = 14f; typeface = Typeface.DEFAULT }
        c.drawText("Verified Completion Proof", 66f, barTop + 48f, textPaint)

        // Location + time
        textPaint.color = Color.parseColor("#CCCCCC")
        val ist = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault())
            .apply { timeZone = TimeZone.getTimeZone("Asia/Kolkata") }
            .format(Date())
        c.drawText("📍 $address", 66f, barTop + 70f, textPaint)
        c.drawText("🕐 $ist  |  ${"%.6f".format(lat)}, ${"%.6f".format(lng)}", 
            66f, barTop + 88f, textPaint)

        // Branding (bottom-right)
        textPaint.apply { color = Color.parseColor("#888888"); textSize = 12f; textAlign = Paint.Align.RIGHT }
        c.drawText("strangerhelp.com", scaled.width - 16f, scaled.height - 16f, textPaint)

        // Save
        val output = File(context.cacheDir, "gps-proof-${System.currentTimeMillis()}.jpg")
        FileOutputStream(output).use { scaled.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        output
    }
}

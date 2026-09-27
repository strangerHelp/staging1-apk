package com.strangerhelp.app.service

import android.graphics.*
import java.text.SimpleDateFormat
import java.util.*

object ProofStamper {

    fun stamp(
        src: Bitmap,
        lat: Double,
        lng: Double,
        placeName: String,
        logo: Bitmap
    ): Bitmap {
        val maxW = 1200
        val scale = if (src.width > maxW) maxW.toFloat() / src.width else 1f
        val out = Bitmap.createScaledBitmap(
            src,
            (src.width * scale).toInt(),
            (src.height * scale).toInt(),
            true
        ).copy(Bitmap.Config.ARGB_8888, true)

        val canvas = Canvas(out)
        val barH = maxOf(96f, out.height * 0.14f)
        val top = out.height - barH

        // Navy bar
        canvas.drawRect(
            0f, top, out.width.toFloat(), out.height.toFloat(),
            Paint().apply { color = Color.argb(235, 16, 24, 40) }
        )
        // Marigold strip
        canvas.drawRect(
            0f, top, out.width.toFloat(), top + 3f,
            Paint().apply { color = Color.parseColor("#F5A623") }
        )

        // Logo circle
        val pad = barH * 0.16f
        val logoSz = maxOf(16f, barH - pad * 2 - 12f)
        canvas.drawCircle(
            pad + logoSz / 2,
            top + pad + 4 + logoSz / 2,
            logoSz / 2 + 3,
            Paint().apply { color = Color.WHITE; isAntiAlias = true }
        )
        val safeLogoInt = maxOf(16, logoSz.toInt())
        canvas.drawBitmap(
            Bitmap.createScaledBitmap(logo, safeLogoInt, safeLogoInt, true),
            pad, top + pad + 4, null
        )

        val tx = pad + logoSz + 14f
        val p = Paint().apply { isAntiAlias = true }

        p.color = Color.WHITE
        p.textSize = 17f
        p.isFakeBoldText = true
        canvas.drawText("StrangerHelp", tx, top + pad + 19, p)

        p.color = Color.parseColor("#F5A623")
        p.textSize = 11f
        canvas.drawText("✓ VERIFIED COMPLETION PROOF", tx, top + pad + 36, p)

        p.color = Color.parseColor("#D0D5DD")
        p.isFakeBoldText = false
        val now = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
            .apply { timeZone = TimeZone.getTimeZone("Asia/Kolkata") }
            .format(Date())
        val safePlace = placeName.ifBlank { "Location Unavailable" }
        canvas.drawText("📍 ${safePlace.take(50)}", tx, top + pad + 54, p)
        canvas.drawText(
            "🕐 $now · ${"%.6f".format(lat)}, ${"%.6f".format(lng)}",
            tx, top + pad + 70, p
        )

        p.textAlign = Paint.Align.RIGHT
        p.color = Color.parseColor("#98A2B3")
        p.textSize = 10f
        canvas.drawText("strangerhelp.com", out.width - 10f, out.height - 8f, p)

        return out
    }

    fun toJpeg(bitmap: Bitmap, quality: Int = 85): ByteArray {
        val baos = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        return baos.toByteArray()
    }
}

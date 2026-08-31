package com.strangerhelp.app.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

object GpsStampHelper {
    fun stampProof(
        src: Bitmap,
        lat: Double,
        lng: Double,
        placeName: String,
        logo: Bitmap
    ): Bitmap {
        val maxW = 1200
        val scale = if (src.width > maxW) maxW.toFloat() / src.width else 1f
        val width = (src.width * scale).toInt()
        val height = (src.height * scale).toInt()

        val scaled = Bitmap.createScaledBitmap(src, width, height, true)
        val out = scaled.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(out)

        val barH = maxOf(84f, out.height * 0.14f)
        val top = out.height - barH

        val navyPaint = Paint().apply {
            color = Color.argb(235, 16, 24, 40)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, top, out.width.toFloat(), out.height.toFloat(), navyPaint)

        val accentPaint = Paint().apply {
            color = Color.parseColor("#F5A623")
        }
        canvas.drawRect(0f, top, out.width.toFloat(), top + 3f, accentPaint)

        val pad = barH * 0.16f
        val logoSz = barH - pad * 2 - 12f
        val circlePaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        canvas.drawCircle(
            pad + logoSz / 2,
            top + pad + 4 + logoSz / 2,
            logoSz / 2 + 3,
            circlePaint
        )
        canvas.drawBitmap(
            Bitmap.createScaledBitmap(logo, logoSz.toInt(), logoSz.toInt(), true),
            pad,
            top + pad + 4,
            null
        )

        val tx = pad + logoSz + 14f
        val p = Paint().apply { isAntiAlias = true }

        p.color = Color.WHITE
        p.textSize = 17f
        p.isFakeBoldText = true
        canvas.drawText("StrangerHelp", tx, top + pad + 19, p)

        p.color = Color.parseColor("#F5A623")
        p.textSize = 11f
        p.isFakeBoldText = false
        canvas.drawText("✓ VERIFIED COMPLETION PROOF", tx, top + pad + 36, p)

        p.color = Color.parseColor("#d0d5dd")
        p.isFakeBoldText = false
        canvas.drawText(
            "📍 ${placeName.take(50)}",
            tx,
            top + pad + 54,
            p
        )

        val now = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
            .apply { timeZone = TimeZone.getTimeZone("Asia/Kolkata") }
            .format(Date())
        canvas.drawText(
            "🕐 $now · ${"%.6f".format(lat)}, ${"%.6f".format(lng)}",
            tx,
            top + pad + 70,
            p
        )

        p.textAlign = Paint.Align.RIGHT
        p.color = Color.parseColor("#98a2b3")
        p.textSize = 10f
        canvas.drawText(
            "strangerhelp.com",
            out.width - 10f,
            out.height - 8f,
            p
        )

        return out
    }

    fun compressStampedProof(bitmap: Bitmap, quality: Int = 85): ByteArray {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        return baos.toByteArray()
    }
}

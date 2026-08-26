package com.strangerhelp.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

fun compressAndSaveImage(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val bitmap = BitmapFactory.decodeStream(inputStream) ?: return null

        val maxDim = 1200
        val scale = minOf(1f, maxDim.toFloat() / maxOf(bitmap.width, bitmap.height))
        val scaledBitmap = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else {
            bitmap
        }

        val outputFile = File(context.cacheDir, "image_${System.currentTimeMillis()}.jpg")
        FileOutputStream(outputFile).use { stream ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        }

        outputFile
    } catch (e: Exception) {
        null
    }
}

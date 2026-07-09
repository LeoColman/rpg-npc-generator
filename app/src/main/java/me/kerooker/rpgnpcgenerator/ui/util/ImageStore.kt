package me.kerooker.rpgnpcgenerator.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.util.UUID
import kotlin.math.max

/** Copies picked portraits into app-private storage so they survive after the picker grant ends. */
object ImageStore {

    private const val DIRECTORY = "portraits"
    private const val MAX_DIMENSION = 1024
    private const val JPEG_QUALITY = 85

    fun persistPortrait(context: Context, uri: Uri): String? = runCatching {
        val bitmap = decodeScaledBitmap(context, uri) ?: return null
        val path = writeJpeg(context, bitmap)
        bitmap.recycle()
        path
    }.getOrNull()

    /** Persists an in-memory bitmap (e.g. a generated portrait) to app storage. */
    fun persistBitmap(context: Context, bitmap: Bitmap): String? =
        runCatching { writeJpeg(context, bitmap) }.getOrNull()

    private fun writeJpeg(context: Context, bitmap: Bitmap): String {
        val directory = File(context.filesDir, DIRECTORY).apply { mkdirs() }
        val destination = File(directory, "${UUID.randomUUID()}.jpg")
        destination.outputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
        }
        return destination.absolutePath
    }

    private fun decodeScaledBitmap(context: Context, uri: Uri): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        val largestSide = max(bounds.outWidth, bounds.outHeight)
        val options = BitmapFactory.Options().apply {
            inSampleSize = if (largestSide > MAX_DIMENSION) largestSide / MAX_DIMENSION else 1
        }
        return context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }
    }
}

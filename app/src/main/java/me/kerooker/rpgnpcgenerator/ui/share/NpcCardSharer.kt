package me.kerooker.rpgnpcgenerator.ui.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

/**
 * Persists a rendered share-card bitmap into app cache and hands it to the system share sheet through
 * a [FileProvider]. Mirrors [me.kerooker.rpgnpcgenerator.ui.util.ImageStore]'s file-handling style but
 * writes PNGs to `cacheDir/shared_images` (transient, exposed via `res/xml/file_paths.xml`) instead of
 * the private portraits directory.
 */
object NpcCardSharer {

    private const val DIRECTORY = "shared_images"
    private const val AUTHORITY_SUFFIX = ".fileprovider"
    private const val MIME_TYPE = "image/png"

    /** Writes [bitmap] as a PNG under `cacheDir/shared_images` and returns the file. */
    fun saveCardPng(context: Context, bitmap: Bitmap): File {
        val directory = File(context.cacheDir, DIRECTORY).apply { mkdirs() }
        val destination = File(directory, "npc_card_${UUID.randomUUID()}.png")
        destination.outputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        return destination
    }

    /** FileProvider authority for this build; matches the `${applicationId}.fileprovider` in the manifest. */
    fun authority(context: Context): String = context.packageName + AUTHORITY_SUFFIX

    /**
     * Fires an `ACTION_SEND` chooser for [file] with [text] as the caption/fallback body. The read
     * grant is attached so the target app can open the shared PNG.
     */
    fun share(context: Context, file: File, text: String, chooserTitle: String) {
        val uri: Uri = FileProvider.getUriForFile(context, authority(context), file)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = MIME_TYPE
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, chooserTitle).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (context !is android.app.Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}

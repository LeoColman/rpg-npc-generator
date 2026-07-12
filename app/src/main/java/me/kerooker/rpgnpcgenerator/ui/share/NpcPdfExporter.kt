package me.kerooker.rpgnpcgenerator.ui.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import java.io.File
import java.util.UUID

/**
 * Wraps a rendered sheet [Bitmap] into a single-page PDF under `cacheDir/shared_images` (the same
 * transient, FileProvider-exposed directory [NpcCardSharer] uses for PNGs), so the export can be
 * handed to the system share sheet the same way. The page is sized to the bitmap and the bitmap is
 * drawn 1:1 into it, so the PDF looks identical to the PNG export.
 */
object NpcPdfExporter {

    private const val DIRECTORY = "shared_images"

    /** Writes [bitmap] as a one-page PDF and returns the file. */
    fun savePdf(context: Context, bitmap: Bitmap): File {
        val directory = File(context.cacheDir, DIRECTORY).apply { mkdirs() }
        val destination = File(directory, "npc_sheet_${UUID.randomUUID()}.pdf")
        val document = PdfDocument()
        try {
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
            val page = document.startPage(pageInfo)
            page.canvas.drawBitmap(bitmap, 0f, 0f, null)
            document.finishPage(page)
            destination.outputStream().use { document.writeTo(it) }
        } finally {
            document.close()
        }
        return destination
    }
}

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
        // PdfDocument pages draw on a software canvas, which rejects HARDWARE bitmaps — and the
        // GraphicsLayer capture produces one. Copy to a software config before drawing.
        val software = if (bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            bitmap
        }
        val document = PdfDocument()
        try {
            val pageInfo = PdfDocument.PageInfo.Builder(software.width, software.height, 1).create()
            val page = document.startPage(pageInfo)
            try {
                page.canvas.drawBitmap(software, 0f, 0f, null)
            } finally {
                // Always finish the page: close() throws "Current page not finished!" on an open
                // page, which would mask the actual drawing failure.
                document.finishPage(page)
            }
            destination.outputStream().use { document.writeTo(it) }
        } finally {
            document.close()
            if (software !== bitmap) software.recycle()
        }
        return destination
    }
}

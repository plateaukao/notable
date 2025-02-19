package com.olup.notable

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.compose.ui.unit.IntOffset
import com.olup.notable.db.BookRepository
import com.olup.notable.db.PageRepository
import com.olup.notable.db.Stroke
import android.util.Log
import java.io.FileOutputStream
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.io.path.div

fun exportBook(context: Context, bookId: String) {
    val book = BookRepository(context).getById(bookId) ?: return
    val pages = PageRepository(context)
    exportPdf("notebooks", book.title) {
        book.pageIds.forEachIndexed { i, pageId -> writePage(i + 1, pages, pageId) }
    }
}

fun exportPage(context: Context, pageId: String) {
    val pages = PageRepository(context)
    exportPdf("pages", "notable-page-${pageId.takeLast(6)}") {
        writePage(1, pages, pageId)
    }
}

private inline fun exportPdf(dir: String, name: String, write: PdfDocument.() -> Unit) {
    val document = PdfDocument()
    document.write()
    val filePath = Environment.getExternalStorageDirectory().toPath() /
            Environment.DIRECTORY_DOCUMENTS / "notable" / dir / "$name.pdf"
    Files.createDirectories(filePath.parent)
    FileOutputStream(filePath.absolutePathString()).use(document::writeTo)
    document.close()
}

private fun PdfDocument.writePage(number: Int, repo: PageRepository, id: String) {
    val (page, strokes) = repo.getWithStrokeById(id)

    val strokeHeight = if (strokes.isEmpty()) 0 else strokes.maxOf(Stroke::bottom).toInt() + 50
    val strokeWidth = if (strokes.isEmpty()) 0 else strokes.maxOf(Stroke::right).toInt() + 50

    val height = strokeHeight.coerceAtLeast(SCREEN_HEIGHT) // todo do not rely on this anymore
    val width = strokeWidth.coerceAtLeast(SCREEN_WIDTH) // todo do not rely on this anymore

    val documentPage =
        startPage(PdfDocument.PageInfo.Builder(width, height, number).create())

    drawBg(documentPage.canvas, page.nativeTemplate, 0)

    for (stroke in strokes) {
        drawStroke(documentPage.canvas, stroke, IntOffset(0, 0))
    }

    finishPage(documentPage)
}

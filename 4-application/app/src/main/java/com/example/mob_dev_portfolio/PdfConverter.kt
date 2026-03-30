package com.example.mob_dev_portfolio

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.content.Intent
import androidx.core.content.FileProvider

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.text.PDFTextStripper
import org.json.JSONArray
import java.io.File
import java.io.InputStream


fun  convertPdfToTxt(context: Context, uri: Uri): String {

    var inputStream: InputStream? = null
    var document: PDDocument? = null

    try {
        PDFBoxResourceLoader.init(context)
        inputStream = context.contentResolver.openInputStream(uri)
        document = PDDocument.load(inputStream)

        val txtStripper = PDFTextStripper()
        val text = txtStripper.getText(document)

        // println("PDF text extracted successfully: $text")
        return text
    } catch (e : Exception) {
        println("YOU RAN INTO AN ISSUE: ${e.message}")
    } finally {
        inputStream?.close()
        document?.close()
    }

    return "fail"
}



fun generatePdfFromQuoteArray(context: Context, quoteArray: JSONArray) {
    PDFBoxResourceLoader.init(context)

    val document    = PDDocument()
    val font        = PDType1Font.HELVETICA
    val fontBold    = PDType1Font.HELVETICA_BOLD
//    val pageWidth   = 595f
    val pageHeight  = 842f
    val margin      = 40f
    val lineHeight  = 22f

    // PDFBox origin is BOTTOM-LEFT, so yPos starts near the top and decreases
    var yPos = pageHeight - margin - 40f

    var page          = PDPage()
    document.addPage(page)
    var contentStream = PDPageContentStream(document, page)

    // Title
    contentStream.beginText()
    contentStream.setFont(fontBold, 20f)
    contentStream.newLineAtOffset(margin, yPos)
    contentStream.showText("Quotes Report")
    contentStream.endText()
    yPos -= lineHeight * 2

    for (i in 0 until quoteArray.length()) {
        val quote = quoteArray.getJSONObject(i)

        quote.keys().forEach { key ->
            val value = quote.optString(key, "-")

            // New page if we're running out of space
            if (yPos - lineHeight < margin) {
                contentStream.close()
                page          = PDPage()
                document.addPage(page)
                contentStream = PDPageContentStream(document, page)
                yPos          = pageHeight - margin - lineHeight
            }

            // Key (bold)
            contentStream.beginText()
            contentStream.setFont(fontBold, 13f)
            contentStream.newLineAtOffset(margin, yPos)
            contentStream.showText(key)
            contentStream.endText()

            // Value (regular)
            contentStream.beginText()
            contentStream.setFont(font, 13f)
            contentStream.newLineAtOffset(margin + 160f, yPos)
            contentStream.showText(value)
            contentStream.endText()

            yPos -= lineHeight
        }

        yPos -= lineHeight / 2 // gap between quotes
    }

    contentStream.close()

    val file = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "quotes_report.pdf"
    )
    document.save(file)
    document.close()

    println("PDF saved to: ${file.absolutePath}")

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    context.startActivity(intent)
}

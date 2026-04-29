package com.example.mob_dev_portfolio

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.InputStream
import org.json.JSONObject

fun sanitisePdfText(text: String): String {
    return text
        .replace("£", "GBP ")
        .replace("€", "EUR ")
        .replace(Regex("[^\\x20-\\x7E]"), "")
}

fun convertPdfToTxt(context: Context, uri: Uri): String {
    var inputStream: InputStream? = null
    var document: PDDocument? = null

    try {
        inputStream = context.contentResolver.openInputStream(uri)
        document = PDDocument.load(inputStream)

        val txtStripper = PDFTextStripper()
        return txtStripper.getText(document)
    } catch (e: Exception) {
        android.util.Log.e("PdfConverter", "Failed to extract text", e)
        throw Exception("Failed to extract text from PDF")
    } finally {
        inputStream?.close()
        document?.close()
    }
}

suspend fun generatePdfFromQuoteArray(
    context: Context,
    quoteArray: JSONArray,
    uri: Uri,
    recommendation: JSONObject?,
    recommendationType: String
) {
    val document    = PDDocument()
    val font        = PDType1Font.HELVETICA
    val fontBold    = PDType1Font.HELVETICA_BOLD

    // Default PDFBox Page is US Letter
    val pageWidth   = 612f
    val pageHeight  = 792f
    val margin      = 40f

    var page = PDPage()
    document.addPage(page)
    var contentStream = PDPageContentStream(document, page)

    var yPos = pageHeight - 120f

    // Helper to add new pages when we run out of vertical space
    fun checkPageBreak(requiredSpace: Float) {
        if (yPos - requiredSpace < margin) {
            contentStream.close()
            page = PDPage()
            document.addPage(page)
            contentStream = PDPageContentStream(document, page)
            yPos = pageHeight - 60f
        }
    }

    try {
        //  header banner
        contentStream.setNonStrokingColor(28, 27, 31) // Ink Color
        contentStream.addRect(0f, pageHeight - 80f, pageWidth, 80f)
        contentStream.fill()

        contentStream.setNonStrokingColor(255, 255, 255) // White text
        contentStream.beginText()
        contentStream.setFont(fontBold, 22f)
        contentStream.newLineAtOffset(margin, pageHeight - 50f)
        contentStream.showText(sanitisePdfText("QuoteScout Report"))
        contentStream.endText()

        // recommendation card
        if (recommendation != null) {
            checkPageBreak(140f)

            // Subtitle
            contentStream.setNonStrokingColor(255, 107, 53) // Amber Color
            contentStream.beginText()
            contentStream.setFont(fontBold, 11f)
            contentStream.newLineAtOffset(margin, yPos)
            contentStream.showText(sanitisePdfText("TOP RECOMMENDATION: ${recommendationType.uppercase()}"))
            contentStream.endText()
            yPos -= 15f

            // Card Background
            contentStream.setNonStrokingColor(245, 245, 245) // Light gray
            contentStream.addRect(margin, yPos - 60f, pageWidth - (margin * 2), 70f)
            contentStream.fill()

            contentStream.setNonStrokingColor(28, 27, 31) // Ink text

            // Amount
            contentStream.beginText()
            contentStream.setFont(fontBold, 20f)
            contentStream.newLineAtOffset(margin + 15f, yPos - 25f)
            contentStream.showText(sanitisePdfText(recommendation.optString("totalAmount", "-")))
            contentStream.endText()

            // Supplier Name
            contentStream.beginText()
            contentStream.setFont(fontBold, 13f)
            contentStream.newLineAtOffset(margin + 160f, yPos - 15f)
            contentStream.showText(sanitisePdfText(recommendation.optString("supplier", "Unknown Supplier")))
            contentStream.endText()

            // Details
            contentStream.setNonStrokingColor(100, 100, 100)
            contentStream.beginText()
            contentStream.setFont(font, 10f)
            contentStream.newLineAtOffset(margin + 160f, yPos - 35f)
            contentStream.showText(sanitisePdfText("REF: " + recommendation.optString("quoteNumber", "N/A") + "   |   DATE: " + recommendation.optString("date", "-")))
            contentStream.endText()

            yPos -= 90f
        }

        // list of quotes
        checkPageBreak(60f)
        yPos -= 10f

        contentStream.setNonStrokingColor(120, 120, 120)
        contentStream.beginText()
        contentStream.setFont(fontBold, 10f)
        contentStream.newLineAtOffset(margin, yPos)
        contentStream.showText(sanitisePdfText("ALL IMPORTED QUOTES"))
        contentStream.endText()
        yPos -= 10f

        // Solid divider line
        contentStream.setStrokingColor(200, 200, 200)
        contentStream.setLineWidth(1f)
        contentStream.moveTo(margin, yPos)
        contentStream.lineTo(pageWidth - margin, yPos)
        contentStream.stroke()
        yPos -= 25f

        // Sort quotes by amount to match the UI list
        val sortedQuotes = mutableListOf<JSONObject>()
        for (i in 0 until quoteArray.length()) {
            sortedQuotes.add(quoteArray.getJSONObject(i))
        }
        sortedQuotes.sortBy { parseAmount(it.optString("totalAmount", "")) }

        // Print each quote in a structured row
        for ((index, quote) in sortedQuotes.withIndex()) {
            checkPageBreak(50f)

            contentStream.setNonStrokingColor(28, 27, 31)

            // Rank & Supplier
            contentStream.beginText()
            contentStream.setFont(fontBold, 12f)
            contentStream.newLineAtOffset(margin, yPos)
            contentStream.showText(sanitisePdfText("${index + 1}. " + quote.optString("supplier", "Unknown Supplier")))
            contentStream.endText()

            // Amount (Right aligned visually)
            contentStream.beginText()
            contentStream.setFont(fontBold, 12f)
            contentStream.newLineAtOffset(pageWidth - margin - 80f, yPos)
            contentStream.showText(sanitisePdfText(quote.optString("totalAmount", "-")))
            contentStream.endText()

            yPos -= 15f

            // Ref & Date
            contentStream.setNonStrokingColor(100, 100, 100)
            contentStream.beginText()
            contentStream.setFont(font, 10f)
            contentStream.newLineAtOffset(margin + 15f, yPos)
            contentStream.showText(sanitisePdfText("REF: " + quote.optString("quoteNumber", "N/A") + "   |   DATE: " + quote.optString("date", "-")))
            contentStream.endText()

            yPos -= 15f

            // Light divider between rows
            contentStream.setStrokingColor(235, 235, 235)
            contentStream.moveTo(margin, yPos)
            contentStream.lineTo(pageWidth - margin, yPos)
            contentStream.stroke()

            yPos -= 20f // Spacing for next row
        }

    } catch (e: Exception) {
        android.util.Log.e("PdfGenerator", "Error drawing PDF: ${e.message}", e)
    } finally {
        contentStream.close()
    }

    // save file to device
    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            document.save(outputStream)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        document.close()
    }

    withContext(Dispatchers.Main) {
        android.widget.Toast.makeText(context, "Report exported successfully!", android.widget.Toast.LENGTH_SHORT).show()
    }
}
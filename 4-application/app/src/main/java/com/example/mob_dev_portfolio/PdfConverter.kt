package com.example.mob_dev_portfolio

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.InputStream

fun  convertPdfToJson(context: Context, uri: Uri): String {

    var inputStream: InputStream? = null
    var document: PDDocument? = null

    try {
        PDFBoxResourceLoader.init(context)
        inputStream = context.contentResolver.openInputStream(uri)
        document = PDDocument.load(inputStream)

        val txtStripper = PDFTextStripper()
        val text = txtStripper.getText(document)

        println("PDF text extracted successfully: $text")
        return text
    } catch (e : Exception) {
        println("YOU RAN INTO AN ISSUE: ${e.message}")
    } finally {
        inputStream?.close()
        document?.close()
    }

    return "this is crap"
}

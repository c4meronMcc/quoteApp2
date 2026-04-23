package com.example.mob_dev_portfolio

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Date

class QuoteLogicSadPathTest {

    // --- Amount Parsing Sad Paths ---

    @Test
    fun `parseAmount handles empty string safely`() {
        // If the ML Kit completely misses the text
        val result = parseAmount("")
        assertEquals("Empty string should return Double.MAX_VALUE to drop to bottom of list", Double.MAX_VALUE, result, 0.0)
    }

    @Test
    fun `parseAmount handles pure alphabetical gibberish`() {
        // If the regex accidentally grabs words instead of numbers
        val result = parseAmount("One hundred pounds")
        assertEquals("Gibberish should return Double.MAX_VALUE", Double.MAX_VALUE, result, 0.0)
    }

    @Test
    fun `parseAmount handles ML Kit 'not found' default`() {
        // This is the literal string your QuoteDataExtractor inserts when it fails
        val result = parseAmount("not found")
        assertEquals("'not found' should return Double.MAX_VALUE", Double.MAX_VALUE, result, 0.0)
    }

    // --- Date Parsing Sad Paths ---

    @Test
    fun `parseDate handles completely wrong formats`() {
        // Your app expects dd/MM/yyyy. What if the PDF has US format or ISO format?
        val result = parseDate("2026-12-25")

        // It should gracefully default to the epoch baseline (Jan 1, 1970) without crashing
        assertEquals("Wrong format should return Epoch date", Date(0), result)
    }

    @Test
    fun `parseDate handles empty string`() {
        val result = parseDate("")
        assertEquals("Empty string should return Epoch date", Date(0), result)
    }

    // --- Recommendation Logic Sad Paths ---

    @Test
    fun `getBestOverallQuote returns null for empty list`() {
        // If the user hasn't imported anything yet, the recommendation shouldn't crash
        val emptyList = emptyList<JSONObject>()
        val result = getBestOverallQuote(emptyList)

        assertNull("Empty list should return a null recommendation", result)
    }

    @Test
    fun `recommendation algorithms do not crash on missing JSON keys`() {
        // What if the JSON is corrupted and doesn't even have the expected keys?
        val brokenQuote = JSONObject().apply {
            put("quoteNumber", "REF-BROKEN")
            // Intentionally missing "totalAmount" and "date"
        }

        val quotes = listOf(brokenQuote)

        // Ensure the minByOrNull functions don't throw NullPointerExceptions
        val cheapest = getCheapestQuote(quotes)
        val mostRecent = getMostRecentQuote(quotes)

        assertEquals("Should safely handle missing amount key using optString defaults", brokenQuote, cheapest)
        assertEquals("Should safely handle missing date key using optString defaults", brokenQuote, mostRecent)
    }
}
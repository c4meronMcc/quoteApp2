package com.example.mob_dev_portfolio

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuoteEdgeCaseTest {

    @Test
    fun `empty list returns null for all recommendation functions`() {
        val emptyList = emptyList<JSONObject>()

        assertNull("Cheapest should be null for empty list", getCheapestQuote(emptyList))
        assertNull("Most recent should be null for empty list", getMostRecentQuote(emptyList))
        assertNull("Best overall should be null for empty list", getBestOverallQuote(emptyList))
    }

    @Test
    fun `getBestOverallQuote correctly ranks known list`() {
        // Setup a known scenario with varying prices and dates
        val expensiveAndOld = JSONObject().apply {
            put("quoteNumber", "REF-1")
            put("totalAmount", "£1000.00")
            put("date", "01/01/2020")
        }
        val cheapButOld = JSONObject().apply {
            put("quoteNumber", "REF-2")
            put("totalAmount", "£10.00")
            put("date", "01/01/2021")
        }
        val expensiveButNew = JSONObject().apply {
            put("quoteNumber", "REF-3")
            put("totalAmount", "£500.00")
            put("date", "01/01/2024")
        }
        val bestOverall = JSONObject().apply {
            put("quoteNumber", "REF-WINNER")
            put("totalAmount", "£15.00") // Very cheap (Rank 2)
            put("date", "31/12/2023")    // Very new (Rank 2)
        }

        val quotes = listOf(expensiveAndOld, cheapButOld, expensiveButNew, bestOverall)

        val winner = getBestOverallQuote(quotes)

        // The algorithm adds the Price Rank and Date Rank together.
        // "REF-WINNER" has the lowest combined rank, so it should win.
        assertEquals("The algorithm should correctly identify REF-WINNER as the best overall", bestOverall, winner)
    }

    @Test
    fun `getBestOverallQuote resolves ties correctly`() {
        // Two quotes with the EXACT same price
        val cheapOld = JSONObject().apply {
            put("quoteNumber", "TIE-OLD")
            put("totalAmount", "£100.00")
            put("date", "01/01/2022")
        }
        val cheapNew = JSONObject().apply {
            put("quoteNumber", "TIE-NEW")
            put("totalAmount", "£100.00")
            put("date", "01/01/2024")
        }

        val quotes = listOf(cheapOld, cheapNew)

        val winner = getBestOverallQuote(quotes)

        // Because prices are tied, the newer date should give TIE-NEW the winning edge
        assertEquals("When prices are tied, the newer quote should win", cheapNew, winner)
    }
}
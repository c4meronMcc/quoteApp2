package com.example.mob_dev_portfolio

import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuoteLogicTest {

    @Test
    fun `parseAmount strips currency and commas correctly`() {
        val input1 = "£1,234.56"
        val input2 = "$99.99"
        val input3 = "invalid_string"

        // Assert that currency symbols and commas are stripped out
        assertEquals(1234.56, parseAmount(input1), 0.0)
        assertEquals(99.99, parseAmount(input2), 0.0)

        // Assert that invalid strings are pushed to the bottom of the cheapest list
        assertEquals(Double.MAX_VALUE, parseAmount(input3), 0.0)
    }

    @Test
    fun `parseDate handles valid dates correctly`() {
        val input = "15/04/2026"
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val expected = format.parse(input)

        assertEquals(expected, parseDate(input))
    }

    @Test
    fun `parseDate handles invalid dates by pushing to epoch baseline`() {
        val input = "not a date"
        val result = parseDate(input)

        // Assert that an invalid date defaults to the epoch (Jan 1, 1970)
        assertEquals(Date(0), result)
    }
}
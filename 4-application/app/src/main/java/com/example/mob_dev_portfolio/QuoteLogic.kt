package com.example.mob_dev_portfolio

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun parseAmount(amountStr: String): Double {
    return try {
        amountStr.replace(Regex("[^0-9.]"), "").toDouble()
    } catch (e: Exception) {
        Double.MAX_VALUE
    }
}

fun parseDate(dateStr: String): Date {
    return try {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateStr) ?: Date(0)
    } catch (e: Exception) {
        Date(0)
    }
}

fun getCheapestQuote(quotes: List<JSONObject>): JSONObject? =
    quotes.minByOrNull { parseAmount(it.optString("totalAmount", "")) }

fun getMostRecentQuote(quotes: List<JSONObject>): JSONObject? =
    quotes.maxByOrNull { parseDate(it.optString("date", "")) }


fun getBestOverallQuote(quotes: List<JSONObject>): JSONObject? {
    if (quotes.isEmpty()) return null

    val byPrice = quotes.sortedBy { parseAmount(it.optString("totalAmount", "")) }
    val byDate  = quotes.sortedByDescending { parseDate(it.optString("date", "")) }

    return quotes.minByOrNull { quote ->
        val quotePrice = parseAmount(quote.optString("totalAmount", ""))
        val quoteDate = parseDate(quote.optString("date", ""))

        val priceRank = byPrice.indexOfFirst { parseAmount(it.optString("totalAmount", "")) == quotePrice }
        val dateRank  = byDate.indexOfFirst { parseDate(it.optString("date", "")) == quoteDate }

        priceRank * priceRank + dateRank * dateRank
    }
}
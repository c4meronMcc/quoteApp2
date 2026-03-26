package com.example.mob_dev_portfolio

import com.google.mlkit.nl.entityextraction.Entity
import com.google.mlkit.nl.entityextraction.EntityAnnotation
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.nl.entityextraction.EntityExtractionParams
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import org.json.JSONObject
import kotlin.coroutines.suspendCoroutine

object QuoteExtractor {

    private val extractor = EntityExtraction.getClient(
        EntityExtractorOptions.Builder(
            EntityExtractorOptions.ENGLISH
        ).build())

    suspend fun extractQuoteDataAsJson(rawText: String): JSONObject {

        suspendCoroutine { continuation ->
            extractor.downloadModelIfNeeded()
                .addOnSuccessListener { continuation.resumeWith(Result.success(Unit)) }
                .addOnFailureListener { continuation.resumeWith(Result.failure(it)) }
        }

        val entities = suspendCoroutine { continuation ->
            extractor.annotate(
                EntityExtractionParams.Builder(rawText).build()
            ).addOnSuccessListener { result ->
                continuation.resumeWith(Result.success(result))
            }.addOnFailureListener { error ->
                continuation.resumeWith(Result.failure(error))
            }
        }

        val json = JSONObject()
        json.put("quoteNumber", findReference(rawText))
        json.put("date", findDate(entities))
        json.put("totalAmount", findTotal(entities, rawText))

        return json
    }

    fun findDate(entities: List<EntityAnnotation>): String {

        return entities.firstOrNull { annotation ->
            annotation.entities.any { it.type == Entity.TYPE_DATE_TIME }
        }
            ?.annotatedText
            ?: "not found"
    }

    fun findTotal(entities: List<EntityAnnotation>, rawText: String): String {

        val moneyEntities = entities.filter { annotation ->
            annotation.entities.any { it.type == Entity.TYPE_MONEY }
        }

        val entitiesWithTotal = moneyEntities.filter { annotation ->
            val startWindow = maxOf(0, annotation.start - 50)
            val endWindow = minOf(rawText.length, annotation.end + 50)

            val surroundingText = rawText.substring(startWindow, endWindow)
            surroundingText.contains("total", ignoreCase = true)
        }

        return entitiesWithTotal.lastOrNull()?.annotatedText
            ?: moneyEntities.lastOrNull()?.annotatedText
            ?: "not found"
    }

    fun findReference(rawText: String): String {
        val pattern = Regex("""[A-Z]{1,6}-?\d{4,8}""")
        return pattern.find(rawText)?.value ?: "Not Found"
    }
}


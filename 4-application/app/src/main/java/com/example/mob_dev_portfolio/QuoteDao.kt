package com.example.mob_dev_portfolio

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface QuoteDao {
    @Insert
    suspend fun insertQuote(quote: QuoteEntity)

    @Query("SELECT id, profileName, createdAt FROM quotes")
    suspend fun getAllProfiles(): List<QuoteProfileSummary>

    @Query("SELECT * FROM quotes WHERE id = :id")
    suspend fun getQuoteById(id: Int): QuoteEntity

    @Delete
    suspend fun deleteQuote(quote: QuoteEntity)

    @Query("DELETE FROM quotes")
    suspend fun deleteAllQuotes()
}



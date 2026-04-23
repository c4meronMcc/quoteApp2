package com.example.mob_dev_portfolio

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface QuoteDao {
    @Insert
    suspend fun insertQuote(quote: QuoteEntity): Long

    @Query("SELECT id, profileName, createdAt, isPinned FROM quotes")
    suspend fun getAllProfiles(): List<QuoteProfileSummary>

    @Query("UPDATE quotes SET isPinned = :isPinned WHERE id = :id")
    suspend fun updatePinned(id: Int, isPinned: Boolean)

    @Query("SELECT * FROM quotes WHERE id = :id")
    suspend fun getQuoteById(id: Int): QuoteEntity

    @Delete
    suspend fun deleteQuote(quote: QuoteEntity)

    @Update
    suspend fun updateQuote(quote: QuoteEntity)

    @Query("DELETE FROM quotes")
    suspend fun deleteAllQuotes()
}



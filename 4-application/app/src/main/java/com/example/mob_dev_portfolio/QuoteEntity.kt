package com.example.mob_dev_portfolio

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val profileName: String,
    val quoteList: String,
    val createdAt: String,
    val isPinned: Boolean = false
)

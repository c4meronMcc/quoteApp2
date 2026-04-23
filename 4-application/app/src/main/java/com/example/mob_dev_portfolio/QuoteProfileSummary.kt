package com.example.mob_dev_portfolio

data class QuoteProfileSummary(
    val id: Int,
    val profileName: String,
    val createdAt: String,
    val isPinned: Boolean = false
)

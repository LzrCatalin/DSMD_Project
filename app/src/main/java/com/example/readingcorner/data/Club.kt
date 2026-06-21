package com.example.readingcorner.data

/** A reading club (Firestore). Members read a shared `currentBook` and chat. */
data class Club(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val currentBook: String = "",
    val currentBookId: String = "",
    val currentBookCover: String = "",
    val currentBookAuthor: String = "",
    val ownerUid: String = "",
    val members: List<String> = emptyList(),
    val createdAt: Long = 0L
)

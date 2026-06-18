package com.example.readingcorner.data

/** A single chat message inside a club (Firestore). */
data class ClubMessage(
    val id: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val text: String = "",
    val createdAt: Long = 0L
)

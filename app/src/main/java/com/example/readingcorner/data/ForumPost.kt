package com.example.readingcorner.data

/** A single message in a per-book discussion forum (Firestore). */
data class ForumPost(
    val id: String = "",
    val bookId: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val text: String = "",
    val createdAt: Long = 0L
)

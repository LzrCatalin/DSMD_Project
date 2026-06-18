package com.example.readingcorner.data

/** A discussion forum (thread) under a book. Each thread has its own chat of [ForumPost]s. */
data class ForumThread(
    val id: String = "",
    val bookId: String = "",
    val title: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val createdAt: Long = 0L
)

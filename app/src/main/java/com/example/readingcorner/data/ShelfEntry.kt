package com.example.readingcorner.data

/**
 * A book on a user's shelf, mirrored to Firestore (`users/{uid}/shelf/{googleId}`) so other
 * users (e.g. club members) can see it. The local source of truth stays in Room.
 */
data class ShelfEntry(
    val googleId: String = "",
    val title: String = "",
    val authors: String = "",
    val coverUrl: String = "",
    val status: String = "TO_READ",
    val myRating: Float = 0f,
    val addedAt: Long = 0L
)

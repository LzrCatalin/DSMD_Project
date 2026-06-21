package com.example.readingcorner.data.local

import androidx.room.Entity

/** Where a book sits on the user's shelves. */
enum class ShelfStatus {
    TO_READ,
    READING,
    READ
}

/**
 * A book the user saved locally (Room). This is the local-database-backed,
 * scrollable list required by the project.
 *
 * Scoped per user via [userUid]: the composite key lets different accounts on the same
 * device each keep their own shelf for the same book.
 */
@Entity(tableName = "shelf_books", primaryKeys = ["userUid", "googleId"])
data class ShelfBook(
    val userUid: String = "",
    val googleId: String = "",
    val title: String = "",
    val authors: String = "",
    val coverUrl: String = "",
    val status: ShelfStatus = ShelfStatus.TO_READ,
    val myRating: Float = 0f,
    val addedAt: Long = 0L,
    val categories: String = ""
)

package com.example.readingcorner.data.repository

import com.example.readingcorner.data.Book
import com.example.readingcorner.data.local.ShelfBook
import com.example.readingcorner.data.local.ShelfStatus
import com.example.readingcorner.data.remote.dto.VolumeItem

/** Google Books volume -> UI/domain [Book]. */
fun VolumeItem.toBook(): Book {
    val info = volumeInfo
    // Google Books often returns http:// thumbnails; upgrade to https so they load.
    val cover = (info?.imageLinks?.thumbnail ?: info?.imageLinks?.smallThumbnail ?: "")
        .replace("http://", "https://")
    return Book(
        id = id,
        title = info?.title ?: "Untitled",
        author = info?.authors?.joinToString(", ") ?: "Unknown author",
        coverUrl = cover,
        rating = info?.averageRating ?: 0.0,
        description = info?.description ?: "",
        pageCount = info?.pageCount ?: 0,
        categories = info?.categories ?: emptyList()
    )
}

/** Saved shelf row -> UI/domain [Book]. */
fun ShelfBook.toBook(): Book = Book(
    id = googleId,
    title = title,
    author = authors,
    coverUrl = coverUrl,
    rating = myRating.toDouble(),
    categories = categories.split(",").filter { it.isNotBlank() }
)

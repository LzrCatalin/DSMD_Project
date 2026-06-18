package com.example.readingcorner.data.repository

import com.example.readingcorner.data.Book
import com.example.readingcorner.data.remote.BooksApiConfig
import com.example.readingcorner.data.remote.GoogleBooksApi
import com.example.readingcorner.data.remote.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Talks to the Google Books API (the remote HTTP data source). */
class BookRepository(
    private val api: GoogleBooksApi = NetworkModule.googleBooksApi
) {

    /** HTTP request #1: search volumes by free-text query. */
    suspend fun searchBooks(query: String): Result<List<Book>> = withContext(Dispatchers.IO) {
        runCatching {
            api.searchVolumes(query = query, apiKey = BooksApiConfig.keyOrNull)
                .items.orEmpty()
                .map { it.toBook() }
        }
    }

    /** HTTP request #2: fetch full details for a single volume. */
    suspend fun getBook(volumeId: String): Result<Book> = withContext(Dispatchers.IO) {
        runCatching {
            api.getVolume(volumeId = volumeId, apiKey = BooksApiConfig.keyOrNull).toBook()
        }
    }
}

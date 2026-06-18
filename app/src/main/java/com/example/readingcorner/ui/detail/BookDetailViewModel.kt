package com.example.readingcorner.ui.detail

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingcorner.data.Book
import com.example.readingcorner.data.ForumThread
import com.example.readingcorner.data.User
import com.example.readingcorner.data.local.AppDatabase
import com.example.readingcorner.data.local.ShelfBook
import com.example.readingcorner.data.local.ShelfStatus
import com.example.readingcorner.data.repository.BookRepository
import com.example.readingcorner.data.repository.ShelfRepository
import com.example.readingcorner.data.repository.SocialRepository
import com.example.readingcorner.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class BookDetailViewModel(app: Application) : AndroidViewModel(app) {

    private val bookRepository = BookRepository()
    private val shelfRepository =
        ShelfRepository(AppDatabase.getInstance(app).shelfBookDao())
    private val socialRepository = SocialRepository()
    private val userRepository = UserRepository()

    var book by mutableStateOf<Book?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var shelf by mutableStateOf<ShelfBook?>(null)
        private set
    var threads by mutableStateOf<List<ForumThread>>(emptyList())
        private set

    private var currentUser: User? = null
    private var observeJob: Job? = null
    private var threadsJob: Job? = null
    private var loadedId: String? = null

    init {
        viewModelScope.launch {
            userRepository.observeUser().collect { currentUser = it }
        }
    }

    fun load(bookId: String) {
        if (loadedId == bookId) return
        loadedId = bookId

        viewModelScope.launch {
            isLoading = true
            error = null
            bookRepository.getBook(bookId)
                .onSuccess { book = it }
                .onFailure { error = it.message ?: "Could not load this book" }
            isLoading = false
        }

        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            shelfRepository.observeShelfBook(bookId).collect { shelf = it }
        }

        threadsJob?.cancel()
        threadsJob = viewModelScope.launch {
            socialRepository.observeThreads(bookId).collect { threads = it }
        }
    }

    /** Create a new forum (thread) for this book (awards bookstars). */
    fun createForum(title: String) {
        val bookId = loadedId ?: return
        val uid = userRepository.currentUid ?: return
        val name = title.trim()
        if (name.isBlank()) return
        val authorName = currentUser?.username?.ifBlank { null } ?: "Reader"
        socialRepository.createThread(bookId, name, uid, authorName) {
            userRepository.awardBookstars(5)
        }
    }

    fun addToShelf(status: ShelfStatus) {
        val current = book ?: return
        viewModelScope.launch {
            shelfRepository.saveToShelf(current, status, System.currentTimeMillis())
        }
    }

    fun setRating(rating: Float) {
        val current = book ?: return
        viewModelScope.launch {
            // A rating implies the book is on a shelf; default it to READ if not yet saved.
            if (shelf == null) {
                shelfRepository.saveToShelf(current, ShelfStatus.READ, System.currentTimeMillis())
            }
            shelfRepository.setRating(current.id, rating)
        }
    }

    fun removeFromShelf() {
        val current = book ?: return
        viewModelScope.launch { shelfRepository.removeFromShelf(current.id) }
    }
}

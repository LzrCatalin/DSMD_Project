package com.example.readingcorner.ui.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingcorner.data.Book
import com.example.readingcorner.data.User
import com.example.readingcorner.data.local.AppDatabase
import com.example.readingcorner.data.local.ShelfStatus
import com.example.readingcorner.data.repository.BookRepository
import com.example.readingcorner.data.repository.ShelfRepository
import com.example.readingcorner.data.repository.toBook
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val shelfRepository = ShelfRepository(AppDatabase.getInstance(app).shelfBookDao())
    private val bookRepository = BookRepository()

    val userData = mutableStateOf<User?>(null)

    var currentlyReading by mutableStateOf<List<Book>>(emptyList())
        private set
    var wantToRead by mutableStateOf<List<Book>>(emptyList())
        private set
    var recommendations by mutableStateOf<List<Book>>(emptyList())
        private set
    var recommendationsLoading by mutableStateOf(false)
        private set

    init {
        fetchUserData()
        observeShelves()
        refreshRecommendations()
        // Backfill: mirror any locally-saved books to Firestore so club members can see them.
        viewModelScope.launch { shelfRepository.syncShelfToCloud() }
    }

    private fun fetchUserData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                userData.value = document.toObject(User::class.java)
            }
    }

    private fun observeShelves() {
        viewModelScope.launch {
            shelfRepository.observeByStatus(ShelfStatus.READING).collect { rows ->
                currentlyReading = rows.map { it.toBook() }
            }
        }
        viewModelScope.launch {
            shelfRepository.observeByStatus(ShelfStatus.TO_READ).collect { rows ->
                wantToRead = rows.map { it.toBook() }
            }
        }
    }

    /**
     * HTTP request: recommendations from Google Books. Seeded from an author already on
     * the user's shelf so it feels personal; falls back to popular fiction for new users.
     * Books already saved on a shelf are filtered out.
     */
    fun refreshRecommendations() {
        viewModelScope.launch {
            val shelf = shelfRepository.observeAll().first()
            val ownedIds = shelf.map { it.googleId }.toSet()

            val likedBooks = shelf.filter { it.myRating >= 4}

            val dislikedAuthors = shelf.filter { it.myRating in 1.0..2.0 }
                .map { it.authors.substringBefore(",").trim() }
                .filter{it.isNotBlank()}
                .toSet()

            val favoriteAuthors = likedBooks.map {it.authors.substringBefore(",").trim()}
                .filter{it.isNotBlank()}
                .distinct()

            val favoriteGenres = likedBooks.flatMap {it.categories.split(",")}
                .map{it.trim()}
                .filter{it.isNotBlank()}
                .distinct()

            recommendationsLoading = true
            val pool = mutableListOf<Book>()

            if (favoriteAuthors.isEmpty() && favoriteGenres.isEmpty()){
                val res = bookRepository.searchBooks("subject:fiction").getOrNull() ?: emptyList()
                pool.addAll(res)
            } else {
                favoriteAuthors.shuffled().take(2).forEach { author ->
                    val res = bookRepository.searchBooks("inauthor:\"$author\"").getOrNull() ?: emptyList()
                    pool.addAll(res)
                }

                favoriteGenres.shuffled().take(2).forEach { genre ->
                    val res = bookRepository.searchBooks("subject:\"$genre\"").getOrNull() ?: emptyList()
                    pool.addAll(res)
                }
            }

            recommendations = pool
                .filter{it.id !in ownedIds}
                .filter{it.author !in dislikedAuthors}
                .distinctBy{"${it.title.trim().lowercase()}_${it.author.trim().lowercase()}"}
                .shuffled()
                .take(15)

            recommendationsLoading = false
        }
    }
}

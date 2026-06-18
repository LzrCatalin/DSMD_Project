package com.example.readingcorner.ui.search

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingcorner.data.Book
import com.example.readingcorner.data.prefs.PreferencesManager
import com.example.readingcorner.data.repository.BookRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SearchViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = BookRepository()
    private val prefs = PreferencesManager(app)

    var query by mutableStateOf("")
        private set
    var results by mutableStateOf<List<Book>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    init {
        // Restore the last query from DataStore and run it.
        viewModelScope.launch {
            val last = prefs.lastSearchQuery.first()
            if (last.isNotBlank()) {
                query = last
                search()
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        query = newQuery
    }

    fun search() {
        val q = query.trim()
        if (q.isBlank()) return
        viewModelScope.launch {
            isLoading = true
            error = null
            prefs.setLastSearchQuery(q)
            repository.searchBooks(q)
                .onSuccess { results = it }
                .onFailure { error = it.message ?: "Something went wrong" }
            isLoading = false
        }
    }
}

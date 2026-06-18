package com.example.readingcorner.ui.forum

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingcorner.data.ForumPost
import com.example.readingcorner.data.ForumThread
import com.example.readingcorner.data.User
import com.example.readingcorner.data.repository.SocialRepository
import com.example.readingcorner.data.repository.UserRepository
import kotlinx.coroutines.launch

class ForumThreadViewModel(app: Application) : AndroidViewModel(app) {

    private val social = SocialRepository()
    private val userRepository = UserRepository()

    val currentUid: String? get() = userRepository.currentUid

    var thread by mutableStateOf<ForumThread?>(null)
        private set
    var posts by mutableStateOf<List<ForumPost>>(emptyList())
        private set

    private var currentUser: User? = null
    private var loadedKey: String? = null
    private var bookId: String = ""
    private var threadId: String = ""

    init {
        viewModelScope.launch {
            userRepository.observeUser().collect { currentUser = it }
        }
    }

    fun load(bookId: String, threadId: String) {
        val key = "$bookId/$threadId"
        if (loadedKey == key) return
        loadedKey = key
        this.bookId = bookId
        this.threadId = threadId

        viewModelScope.launch {
            social.observeThread(bookId, threadId).collect { thread = it }
        }
        viewModelScope.launch {
            social.observeThreadPosts(bookId, threadId).collect { posts = it }
        }
    }

    fun post(text: String) {
        val uid = currentUid ?: return
        val message = text.trim()
        if (message.isBlank() || bookId.isBlank() || threadId.isBlank()) return
        val authorName = currentUser?.username?.ifBlank { null } ?: "Reader"
        social.addThreadPost(bookId, threadId, uid, authorName, message) {
            userRepository.awardBookstars(3)
        }
    }
}

package com.example.readingcorner.ui.clubs

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingcorner.data.Book
import com.example.readingcorner.data.Club
import com.example.readingcorner.data.ClubMessage
import com.example.readingcorner.data.ShelfEntry
import com.example.readingcorner.data.User
import com.example.readingcorner.data.repository.BookRepository
import com.example.readingcorner.data.repository.SocialRepository
import com.example.readingcorner.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** A club member together with their shelves (To Read / Reading / Read). */
data class MemberShelf(
    val uid: String,
    val username: String,
    val toRead: List<ShelfEntry>,
    val reading: List<ShelfEntry>,
    val read: List<ShelfEntry>
)

class ClubDetailViewModel(app: Application) : AndroidViewModel(app) {

    private val social = SocialRepository()
    private val userRepository = UserRepository()
    private val bookRepository = BookRepository()

    val currentUid: String? get() = userRepository.currentUid

    var club by mutableStateOf<Club?>(null)
        private set
    var messages by mutableStateOf<List<ClubMessage>>(emptyList())
        private set
    var members by mutableStateOf<List<MemberShelf>>(emptyList())
        private set

    var bookSearchResults by mutableStateOf<List<Book>>(emptyList())
        private set
    var bookSearchLoading by mutableStateOf(false)
        private set

    private var currentUser: User? = null
    private var loadedId: String? = null
    private var membersJob: Job? = null
    private var lastMemberUids: List<String> = emptyList()

    val isMember: Boolean
        get() = club?.members?.contains(currentUid) == true

    init {
        viewModelScope.launch {
            userRepository.observeUser().collect { currentUser = it }
        }
    }

    fun load(clubId: String) {
        if (loadedId == clubId) return
        loadedId = clubId

        viewModelScope.launch {
            social.observeClub(clubId).collect { c ->
                club = c
                val uids = c?.members.orEmpty()
                if (uids != lastMemberUids) {
                    lastMemberUids = uids
                    loadMembers(uids)
                }
            }
        }
        viewModelScope.launch {
            social.observeMessages(clubId).collect { messages = it }
        }
    }

    private fun loadMembers(uids: List<String>) {
        membersJob?.cancel()
        membersJob = viewModelScope.launch {
            members = uids.map { uid ->
                val user = userRepository.observeUser(uid).first()
                val shelf = social.observeUserShelf(uid).first()
                MemberShelf(
                    uid = uid,
                    username = user?.username?.ifBlank { null } ?: "Reader",
                    toRead = shelf.filter { it.status == "TO_READ" },
                    reading = shelf.filter { it.status == "READING" },
                    read = shelf.filter { it.status == "READ" }
                )
            }
        }
    }

    fun toggleMembership() {
        val clubId = loadedId ?: return
        val uid = currentUid ?: return
        if (isMember) {
            social.leaveClub(clubId, uid)
        } else {
            social.joinClub(clubId, uid) { userRepository.awardBookstars(15) }
        }
    }

    fun searchBooks(query: String) {
        if (query.isBlank()) {
            bookSearchResults = emptyList()
            return
        }
        bookSearchLoading = true
        viewModelScope.launch {
            bookRepository.searchBooks(query)
                .onSuccess { bookSearchResults = it }
                .onFailure { bookSearchResults = emptyList() }
            bookSearchLoading = false
        }
    }

    fun clearBookSearch() {
        bookSearchResults = emptyList()
    }

    fun setCurrentBook(book: Book) {
        val clubId = loadedId ?: return
        social.setCurrentBook(
            clubId = clubId,
            bookId = book.id,
            title = book.title,
            author = book.author,
            cover = book.coverUrl
        )
    }

    fun sendMessage(text: String) {
        val clubId = loadedId ?: return
        val uid = currentUid ?: return
        val message = text.trim()
        if (message.isBlank()) return
        val authorName = currentUser?.username?.ifBlank { null } ?: "Reader"
        social.sendMessage(clubId, uid, authorName, message)
    }
}

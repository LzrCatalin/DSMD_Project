package com.example.readingcorner.data.repository

import com.example.readingcorner.data.Book
import com.example.readingcorner.data.ShelfEntry
import com.example.readingcorner.data.local.ShelfBook
import com.example.readingcorner.data.local.ShelfBookDao
import com.example.readingcorner.data.local.ShelfStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Reads/writes the signed-in user's saved books in Room (the local source of truth) and
 * mirrors each change to Firestore (`users/{uid}/shelf`) so club members can see each
 * other's shelves.
 */
class ShelfRepository(
    private val dao: ShelfBookDao,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /** Current user id; all queries are scoped to it so shelves don't leak across accounts. */
    private val uid: String get() = auth.currentUser?.uid.orEmpty()

    private fun shelfDoc(owner: String, googleId: String) =
        firestore.collection("users").document(owner).collection("shelf").document(googleId)

    fun observeAll(): Flow<List<ShelfBook>> = dao.observeAll(uid)

    fun observeByStatus(status: ShelfStatus): Flow<List<ShelfBook>> = dao.observeByStatus(uid, status)

    fun observeShelfBook(googleId: String): Flow<ShelfBook?> = dao.observeByGoogleId(uid, googleId)

    fun countByStatus(status: ShelfStatus): Flow<Int> = dao.countByStatus(uid, status)

    /** Add or move a book to a shelf, preserving any rating already set. */
    suspend fun saveToShelf(book: Book, status: ShelfStatus, addedAt: Long) {
        val owner = uid
        val existing = dao.getByGoogleId(owner, book.id)
        val row = ShelfBook(
            userUid = owner,
            googleId = book.id,
            title = book.title,
            authors = book.author,
            coverUrl = book.coverUrl,
            status = status,
            myRating = existing?.myRating ?: 0f,
            addedAt = existing?.addedAt ?: addedAt
        )
        dao.upsert(row)
        if (owner.isNotBlank()) shelfDoc(owner, book.id).set(row.toEntry())
    }

    suspend fun setRating(googleId: String, rating: Float) {
        val owner = uid
        dao.updateRating(owner, googleId, rating)
        if (owner.isNotBlank()) {
            shelfDoc(owner, googleId).set(mapOf("myRating" to rating), SetOptions.merge())
        }
    }

    suspend fun removeFromShelf(googleId: String) {
        val owner = uid
        dao.delete(owner, googleId)
        if (owner.isNotBlank()) shelfDoc(owner, googleId).delete()
    }

    /** One-time backfill: push every locally saved book to Firestore (idempotent). */
    suspend fun syncShelfToCloud() {
        val owner = uid
        if (owner.isBlank()) return
        val books = dao.observeAll(owner).first()
        books.forEach { shelfDoc(owner, it.googleId).set(it.toEntry(), SetOptions.merge()) }
    }
}

private fun ShelfBook.toEntry() = ShelfEntry(
    googleId = googleId,
    title = title,
    authors = authors,
    coverUrl = coverUrl,
    status = status.name,
    myRating = myRating,
    addedAt = addedAt
)

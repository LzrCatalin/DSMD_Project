package com.example.readingcorner.data.repository

import com.example.readingcorner.data.Club
import com.example.readingcorner.data.ClubMessage
import com.example.readingcorner.data.ForumPost
import com.example.readingcorner.data.ForumThread
import com.example.readingcorner.data.ShelfEntry
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Cloud-backed social features (Firestore):
 *  - per-book forums:  forums/{bookId}/threads/{threadId}/posts/{postId}
 *  - reading clubs:    clubs/{clubId} (+ clubs/{clubId}/messages/{msgId})
 *  - shared shelves:   users/{uid}/shelf/{googleId} (read-only here; written by ShelfRepository)
 *
 * Reads are exposed as real-time [Flow]s backed by snapshot listeners; writes are
 * fire-and-forget with an optional completion callback.
 */
class SocialRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // ---------------------------------------------------------------- Forums

    /** Forums (threads) for a book, newest first. */
    fun observeThreads(bookId: String): Flow<List<ForumThread>> = callbackFlow {
        val registration = db.collection("forums").document(bookId)
            .collection("threads")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.documents?.mapNotNull { it.toObject(ForumThread::class.java) }.orEmpty())
            }
        awaitClose { registration.remove() }
    }

    fun observeThread(bookId: String, threadId: String): Flow<ForumThread?> = callbackFlow {
        val registration = db.collection("forums").document(bookId)
            .collection("threads").document(threadId)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.toObject(ForumThread::class.java))
            }
        awaitClose { registration.remove() }
    }

    fun createThread(
        bookId: String,
        title: String,
        authorUid: String,
        authorName: String,
        onSuccess: (String) -> Unit = {}
    ) {
        val ref = db.collection("forums").document(bookId).collection("threads").document()
        val thread = ForumThread(
            id = ref.id,
            bookId = bookId,
            title = title,
            authorUid = authorUid,
            authorName = authorName,
            createdAt = System.currentTimeMillis()
        )
        ref.set(thread).addOnSuccessListener { onSuccess(ref.id) }
    }

    /** Chat messages inside a single forum thread, oldest first. */
    fun observeThreadPosts(bookId: String, threadId: String): Flow<List<ForumPost>> = callbackFlow {
        val registration = db.collection("forums").document(bookId)
            .collection("threads").document(threadId)
            .collection("posts")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.documents?.mapNotNull { it.toObject(ForumPost::class.java) }.orEmpty())
            }
        awaitClose { registration.remove() }
    }

    fun addThreadPost(
        bookId: String,
        threadId: String,
        authorUid: String,
        authorName: String,
        text: String,
        onSuccess: () -> Unit = {}
    ) {
        val ref = db.collection("forums").document(bookId)
            .collection("threads").document(threadId)
            .collection("posts").document()
        val post = ForumPost(
            id = ref.id,
            bookId = bookId,
            authorUid = authorUid,
            authorName = authorName,
            text = text,
            createdAt = System.currentTimeMillis()
        )
        ref.set(post).addOnSuccessListener { onSuccess() }
    }

    // ----------------------------------------------------------------- Clubs

    fun observeClubs(): Flow<List<Club>> = callbackFlow {
        val registration = db.collection("clubs")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.documents?.mapNotNull { it.toObject(Club::class.java) }.orEmpty())
            }
        awaitClose { registration.remove() }
    }

    fun observeClub(clubId: String): Flow<Club?> = callbackFlow {
        val registration = db.collection("clubs").document(clubId)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.toObject(Club::class.java))
            }
        awaitClose { registration.remove() }
    }

    fun createClub(
        name: String,
        description: String,
        ownerUid: String,
        onSuccess: (String) -> Unit = {}
    ) {
        val ref = db.collection("clubs").document()
        val club = Club(
            id = ref.id,
            name = name,
            description = description,
            ownerUid = ownerUid,
            members = listOf(ownerUid),
            createdAt = System.currentTimeMillis()
        )
        ref.set(club).addOnSuccessListener { onSuccess(ref.id) }
    }

    fun joinClub(clubId: String, uid: String, onSuccess: () -> Unit = {}) {
        db.collection("clubs").document(clubId)
            .update("members", FieldValue.arrayUnion(uid))
            .addOnSuccessListener { onSuccess() }
    }

    fun leaveClub(clubId: String, uid: String) {
        db.collection("clubs").document(clubId)
            .update("members", FieldValue.arrayRemove(uid))
    }

    fun setCurrentBook(clubId: String, bookId: String, title: String, author: String, cover: String) {
        db.collection("clubs").document(clubId).update(
            mapOf(
                "currentBook" to title,
                "currentBookId" to bookId,
                "currentBookAuthor" to author,
                "currentBookCover" to cover
            )
        )
    }

    fun observeMessages(clubId: String): Flow<List<ClubMessage>> = callbackFlow {
        val registration = db.collection("clubs").document(clubId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.documents?.mapNotNull { it.toObject(ClubMessage::class.java) }.orEmpty())
            }
        awaitClose { registration.remove() }
    }

    fun sendMessage(clubId: String, authorUid: String, authorName: String, text: String) {
        val ref = db.collection("clubs").document(clubId).collection("messages").document()
        val message = ClubMessage(
            id = ref.id,
            authorUid = authorUid,
            authorName = authorName,
            text = text,
            createdAt = System.currentTimeMillis()
        )
        ref.set(message)
    }

    // ------------------------------------------------------- Shared shelves

    /** Another user's mirrored shelf (used to show club members' books). */
    fun observeUserShelf(uid: String): Flow<List<ShelfEntry>> = callbackFlow {
        val registration = db.collection("users").document(uid)
            .collection("shelf")
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.documents?.mapNotNull { it.toObject(ShelfEntry::class.java) }.orEmpty())
            }
        awaitClose { registration.remove() }
    }
}

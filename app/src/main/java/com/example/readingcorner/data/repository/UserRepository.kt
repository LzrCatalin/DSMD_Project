package com.example.readingcorner.data.repository

import com.example.readingcorner.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Reads the signed-in user's profile doc and awards "bookstars" (Firestore). */
class UserRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    val currentUid: String? get() = auth.currentUser?.uid

    /** Real-time view of a user document. Emits null while unavailable. */
    fun observeUser(uid: String = currentUid.orEmpty()): Flow<User?> = callbackFlow {
        if (uid.isBlank()) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val registration = db.collection("users").document(uid)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.toObject(User::class.java))
            }
        awaitClose { registration.remove() }
    }

    /** Increment the signed-in user's bookstars by [points] (server-side). */
    fun awardBookstars(points: Int) {
        val uid = currentUid ?: return
        db.collection("users").document(uid)
            .update("bookstars", FieldValue.increment(points.toLong()))
    }
}

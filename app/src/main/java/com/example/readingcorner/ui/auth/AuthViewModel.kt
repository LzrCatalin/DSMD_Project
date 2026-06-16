package com.example.readingcorner.ui.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.State
import com.example.readingcorner.data.User
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    val currentUser = mutableStateOf(auth.currentUser)

    fun signUp(username: String, email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank() || username.isBlank()) {
            _errorMessage.value = "Username, email and password cannot be empty"
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        val newUser = User(
                            uid = firebaseUser.uid,
                            username = username,
                            email = email
                        )
                        // Save the user to Firestore
                        db.collection("users").document(firebaseUser.uid)
                            .set(newUser)
                            .addOnSuccessListener {
                                _errorMessage.value = null
                                currentUser.value = auth.currentUser
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                _errorMessage.value = e.message ?: "Failed to save user data"
                            }
                    }
                } else {
                    _errorMessage.value = task.exception?.message ?: "Sign up failed"
                }
            }
    }

    fun logIn(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Email and password cannot be empty"
            return
        }

        // Firebase checks the credentials
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _errorMessage.value = null
                    currentUser.value = auth.currentUser
                    onSuccess() // Tell the UI it worked!
                } else {
                    _errorMessage.value = task.exception?.message ?: "Login failed"
                }
            }
    }

    fun logOut(onSuccess: () -> Unit) {
        auth.signOut()
        currentUser.value = null
        onSuccess()
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
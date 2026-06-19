package com.example.readingcorner.ui.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import androidx.compose.runtime.State
import com.example.readingcorner.data.User
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    val currentUser = mutableStateOf(auth.currentUser)

    fun signUp(username: String, email: String, password: String, onSuccess: () -> Unit) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Username, email and password cannot be empty"
            return
        }
        if (password.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters"
            return
        }
        if (!password.any { it.isDigit() }) {
            _errorMessage.value = "Password must contain at least one number"
            return
        }

        _isLoading.value = true
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
                        db.collection("users").document(firebaseUser.uid)
                            .set(newUser)
                            .addOnSuccessListener {
                                _isLoading.value = false
                                _errorMessage.value = null
                                currentUser.value = auth.currentUser
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                _isLoading.value = false
                                _errorMessage.value = "Account created but profile save failed. Please try again."
                            }
                    }
                } else {
                    _isLoading.value = false
                    _errorMessage.value = friendlyError(task.exception, "Sign up failed. Please try again.")
                }
            }
    }

    fun logIn(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Email and password cannot be empty"
            return
        }

        _isLoading.value = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    _errorMessage.value = null
                    currentUser.value = auth.currentUser
                    onSuccess()
                } else {
                    _errorMessage.value = friendlyError(task.exception, "Login failed. Please try again.")
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

    private fun friendlyError(exception: Exception?, fallback: String): String {
        val code = (exception as? FirebaseAuthException)?.errorCode ?: ""
        return when {
            code == "ERROR_EMAIL_ALREADY_IN_USE" ||
            exception?.message?.contains("already in use", ignoreCase = true) == true ->
                "An account with this email already exists."
            code == "ERROR_INVALID_EMAIL" ||
            exception?.message?.contains("badly formatted", ignoreCase = true) == true ->
                "Please enter a valid email address."
            code == "ERROR_WRONG_PASSWORD" ||
            code == "ERROR_INVALID_CREDENTIAL" ||
            exception?.message?.contains("password is invalid", ignoreCase = true) == true ||
            exception?.message?.contains("no user record", ignoreCase = true) == true ->
                "Incorrect email or password."
            code == "ERROR_USER_NOT_FOUND" ->
                "No account found with this email."
            code == "ERROR_USER_DISABLED" ->
                "This account has been disabled."
            code == "ERROR_TOO_MANY_REQUESTS" ||
            exception?.message?.contains("too many", ignoreCase = true) == true ->
                "Too many attempts. Please try again later."
            code == "ERROR_NETWORK_REQUEST_FAILED" ||
            exception?.message?.contains("network", ignoreCase = true) == true ->
                "No internet connection. Please check your network."
            else -> fallback
        }
    }
}
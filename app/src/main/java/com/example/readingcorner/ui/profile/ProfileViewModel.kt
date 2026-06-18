package com.example.readingcorner.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.readingcorner.data.local.AppDatabase
import com.example.readingcorner.data.local.ShelfStatus
import com.example.readingcorner.data.repository.ShelfRepository
import com.example.readingcorner.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfileViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = ShelfRepository(AppDatabase.getInstance(app).shelfBookDao())
    private val userRepository = UserRepository()

    val email: String = FirebaseAuth.getInstance().currentUser?.email ?: "Guest"

    val toReadCount: Flow<Int> = repository.countByStatus(ShelfStatus.TO_READ)
    val readingCount: Flow<Int> = repository.countByStatus(ShelfStatus.READING)
    val readCount: Flow<Int> = repository.countByStatus(ShelfStatus.READ)

    /** Bookstars earned from social activity (forum posts, club joins), stored in Firestore. */
    val socialBookstars: Flow<Int> = userRepository.observeUser().map { (it?.bookstars ?: 0L).toInt() }
}

package com.example.readingcorner.ui.clubs

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingcorner.data.Club
import com.example.readingcorner.data.repository.SocialRepository
import com.example.readingcorner.data.repository.UserRepository
import kotlinx.coroutines.launch

class ClubsViewModel(app: Application) : AndroidViewModel(app) {

    private val social = SocialRepository()
    private val userRepository = UserRepository()

    val currentUid: String? get() = userRepository.currentUid

    var clubs by mutableStateOf<List<Club>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            social.observeClubs().collect { clubs = it }
        }
    }

    fun createClub(name: String, description: String) {
        val uid = currentUid ?: return
        if (name.isBlank()) return
        social.createClub(name.trim(), description.trim(), uid)
    }
}

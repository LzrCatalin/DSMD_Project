package com.example.readingcorner.data

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val bookstars: Long = 0,
    val joinedClubs: List<String> = emptyList(),
    val favoriteGenres: List<String> = emptyList()
)
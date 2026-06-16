package com.example.readingcorner.data

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val joinedClubs: List<String> = emptyList(),
    val favoriteGenres: List<String> = emptyList()
)
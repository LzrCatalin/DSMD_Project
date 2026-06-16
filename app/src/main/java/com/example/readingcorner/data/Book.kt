package com.example.readingcorner.data

data class Book(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val coverUrl: String = "",
    val rating: Double = 0.0,
    val description: String = "",
    val pageCount: Int = 0,
    val categories: List<String> = emptyList()
)
package com.example.readingcorner.ui.mybooks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.readingcorner.data.local.AppDatabase
import com.example.readingcorner.data.local.ShelfBook
import com.example.readingcorner.data.local.ShelfStatus
import com.example.readingcorner.data.repository.ShelfRepository
import kotlinx.coroutines.flow.Flow

class MyBooksViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = ShelfRepository(AppDatabase.getInstance(app).shelfBookDao())

    fun shelf(status: ShelfStatus): Flow<List<ShelfBook>> = repository.observeByStatus(status)
}

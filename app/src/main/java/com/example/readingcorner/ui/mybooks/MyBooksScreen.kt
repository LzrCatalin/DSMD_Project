package com.example.readingcorner.ui.mybooks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.readingcorner.data.local.ShelfStatus
import com.example.readingcorner.data.repository.toBook
import com.example.readingcorner.ui.components.BookRow

private val TABS = listOf(
    ShelfStatus.TO_READ to "To Read",
    ShelfStatus.READING to "Reading",
    ShelfStatus.READ to "Read"
)

@Composable
fun MyBooksScreen(
    onBookClick: (String) -> Unit,
    viewModel: MyBooksViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val status = TABS[selectedTab].first

    val shelfBooks by viewModel.shelf(status).collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            TABS.forEachIndexed { index, (_, label) ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(label) }
                )
            }
        }

        if (shelfBooks.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(
                    text = "No books here yet.\nFind some in Search and add them.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                items(shelfBooks, key = { it.googleId }) { shelfBook ->
                    BookRow(
                        book = shelfBook.toBook(),
                        onClick = { onBookClick(shelfBook.googleId) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

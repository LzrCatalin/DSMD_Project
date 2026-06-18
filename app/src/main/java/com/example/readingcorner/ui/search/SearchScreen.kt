package com.example.readingcorner.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.readingcorner.ui.components.BookRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBookClick: (String) -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val keyboard = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = viewModel.query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search books, authors…") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                keyboard?.hide()
                viewModel.search()
            })
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            viewModel.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }

            viewModel.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(
                    text = viewModel.error ?: "Error",
                    color = MaterialTheme.colorScheme.error
                )
            }

            viewModel.results.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(
                    text = "Search Google Books to get started.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(viewModel.results, key = { it.id }) { book ->
                    BookRow(book = book, onClick = { onBookClick(book.id) })
                    HorizontalDivider()
                }
            }
        }
    }
}

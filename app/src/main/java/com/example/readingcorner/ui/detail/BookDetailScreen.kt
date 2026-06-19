package com.example.readingcorner.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.readingcorner.data.ForumThread
import com.example.readingcorner.data.local.ShelfStatus
import com.example.readingcorner.ui.components.BookCover
import com.example.readingcorner.ui.components.RatingBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: String,
    onBack: () -> Unit,
    onOpenForum: (bookId: String, threadId: String) -> Unit = { _, _ -> },
    viewModel: BookDetailViewModel = viewModel()
) {
    LaunchedEffect(bookId) { viewModel.load(bookId) }

    val book = viewModel.book
    val shelf = viewModel.shelf

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book?.title ?: "Book", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            viewModel.isLoading -> Box(
                Modifier.fillMaxSize().padding(padding), Alignment.Center
            ) { CircularProgressIndicator() }

            viewModel.error != null || book == null -> Box(
                Modifier.fillMaxSize().padding(padding), Alignment.Center
            ) {
                Text(
                    text = viewModel.error ?: "Book not found",
                    color = MaterialTheme.colorScheme.error
                )
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Row {
                    BookCover(
                        coverUrl = book.coverUrl,
                        modifier = Modifier.size(120.dp, 180.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(book.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("by ${book.author}", style = MaterialTheme.typography.bodyMedium)
                        if (book.pageCount > 0) {
                            Spacer(Modifier.height(4.dp))
                            Text("${book.pageCount} pages", style = MaterialTheme.typography.bodySmall)
                        }
                        if (book.categories.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                book.categories.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text("Your rating", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                RatingBar(
                    rating = shelf?.myRating ?: 0f,
                    onRatingChange = viewModel::setRating,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(20.dp))

                Text("Add to shelf", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShelfChip("To Read", shelf?.status == ShelfStatus.TO_READ) {
                        viewModel.addToShelf(ShelfStatus.TO_READ)
                    }
                    ShelfChip("Reading", shelf?.status == ShelfStatus.READING) {
                        viewModel.addToShelf(ShelfStatus.READING)
                    }
                    ShelfChip("Read", shelf?.status == ShelfStatus.READ) {
                        viewModel.addToShelf(ShelfStatus.READ)
                    }
                }

                if (shelf != null) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = viewModel::removeFromShelf) {
                        Text("Remove from shelf")
                    }
                }

                if (book.description.isNotBlank()) {
                    Spacer(Modifier.height(20.dp))
                    Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        AnnotatedString.fromHtml(book.description),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(Modifier.height(20.dp))
                ForumsSection(
                    threads = viewModel.threads,
                    onCreateForum = viewModel::createForum,
                    onOpenForum = { threadId -> onOpenForum(book.id, threadId) }
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShelfChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
}

@Composable
private fun ForumsSection(
    threads: List<ForumThread>,
    onCreateForum: (String) -> Unit,
    onOpenForum: (String) -> Unit
) {
    var draft by remember { mutableStateOf("") }

    Text("Forums", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("New forum topic…") },
            singleLine = true
        )
        IconButton(
            onClick = {
                onCreateForum(draft)
                draft = ""
            },
            enabled = draft.isNotBlank()
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create forum")
        }
    }

    Spacer(Modifier.height(12.dp))

    if (threads.isEmpty()) {
        Text(
            "No forums yet. Create one to start a discussion!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            threads.forEach { thread -> ForumRow(thread, onClick = { onOpenForum(thread.id) }) }
        }
    }
}

@Composable
private fun ForumRow(thread: ForumThread, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    thread.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "by ${thread.authorName.ifBlank { "Reader" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Open")
        }
    }
}

package com.example.readingcorner.ui.clubs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.readingcorner.data.ClubMessage
import com.example.readingcorner.data.ShelfEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubDetailScreen(
    clubId: String,
    onBack: () -> Unit,
    viewModel: ClubDetailViewModel = viewModel()
) {
    LaunchedEffect(clubId) { viewModel.load(clubId) }

    val club = viewModel.club
    val messages = viewModel.messages
    val members = viewModel.members
    var draft by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(club?.name ?: "Club", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                if (club != null) {
                    item {
                        if (club.description.isNotBlank()) {
                            Text(club.description, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(8.dp))
                        }
                        if (club.currentBook.isNotBlank()) {
                            Text(
                                "Currently reading: ${club.currentBook}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${club.members.size} member${if (club.members.size == 1) "" else "s"}",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Button(onClick = viewModel::toggleMembership) {
                                Text(if (viewModel.isMember) "Leave" else "Join")
                            }
                        }
                    }
                }

                // Members and what they're reading.
                item {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Members & their shelves",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(members) { member -> MemberCard(member) }

                item {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Text(
                        "Club chat",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (messages.isEmpty()) {
                    item {
                        Text(
                            "No messages yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(messages) { msg ->
                        MessageBubble(msg, isMine = msg.authorUid == viewModel.currentUid)
                    }
                }
            }

            if (viewModel.isMember) {
                Row(
                    Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Message…") },
                        maxLines = 3
                    )
                    IconButton(
                        onClick = {
                            viewModel.sendMessage(draft)
                            draft = ""
                        },
                        enabled = draft.isNotBlank()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            } else {
                Text(
                    "Join this club to chat.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun MemberCard(member: MemberShelf) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(
                member.username,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            ShelfLine("To Read", member.toRead)
            ShelfLine("Reading", member.reading)
            ShelfLine("Read", member.read)
            if (member.toRead.isEmpty() && member.reading.isEmpty() && member.read.isEmpty()) {
                Text(
                    "No books shared yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ShelfLine(label: String, books: List<ShelfEntry>) {
    if (books.isEmpty()) return
    Row(Modifier.padding(vertical = 2.dp)) {
        Text(
            "$label: ",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            books.joinToString(", ") { it.title },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MessageBubble(message: ClubMessage, isMine: Boolean) {
    val bubbleColor =
        if (isMine) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant

    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Surface(color = bubbleColor, shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(10.dp)) {
                Text(
                    message.authorName.ifBlank { "Reader" },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(message.text, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

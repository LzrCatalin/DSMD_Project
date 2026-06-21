package com.example.readingcorner.ui.clubs

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.readingcorner.data.Book
import com.example.readingcorner.data.Club
import com.example.readingcorner.data.ClubMessage
import com.example.readingcorner.data.ShelfEntry
import com.example.readingcorner.ui.components.BookCover

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    var selectedTab by remember { mutableStateOf(0) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    val tabs = listOf("Chat", "Members", "About")
    val isOwner = club?.ownerUid == viewModel.currentUid

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Leave club?") },
            text = { Text("You will lose access to the club chat and member shelves.") },
            confirmButton = {
                TextButton(onClick = {
                    showLeaveDialog = false
                    viewModel.toggleMembership()
                }) { Text("Leave", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) { Text("Cancel") }
            }
        )
    }

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
            // Compact header — always visible
            if (club != null) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${club.members.size} member${if (club.members.size == 1) "" else "s"}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = {
                            if (viewModel.isMember) showLeaveDialog = true
                            else viewModel.toggleMembership()
                        },
                        enabled = !isOwner
                    ) {
                        Text(if (viewModel.isMember) "Leave" else "Join")
                    }
                }
                HorizontalDivider()
            }

            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                2 -> AboutTab(
                    club = club,
                    currentUid = viewModel.currentUid,
                    members = members,
                    searchResults = viewModel.bookSearchResults,
                    searchLoading = viewModel.bookSearchLoading,
                    onSearch = { q -> viewModel.searchBooks(q) },
                    onClearSearch = { viewModel.clearBookSearch() },
                    onSetCurrentBook = { book -> viewModel.setCurrentBook(book) }
                )
                1 -> MembersTab(members)
                0 -> ChatTab(
                    messages = messages,
                    currentUid = viewModel.currentUid,
                    isMember = viewModel.isMember,
                    draft = draft,
                    onDraftChange = { draft = it },
                    onSend = {
                        viewModel.sendMessage(draft)
                        draft = ""
                    }
                )
            }
        }
    }
}

@Composable
private fun AboutTab(
    club: Club?,
    currentUid: String?,
    members: List<MemberShelf>,
    searchResults: List<Book> = emptyList(),
    searchLoading: Boolean = false,
    onSearch: (String) -> Unit = {},
    onClearSearch: () -> Unit = {},
    onSetCurrentBook: (Book) -> Unit = {}
) {
    if (club == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val isOwner = club.ownerUid == currentUid
    var showPickerDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    if (showPickerDialog) {
        AlertDialog(
            onDismissRequest = {
                showPickerDialog = false
                searchQuery = ""
                onClearSearch()
            },
            title = { Text("Set currently reading") },
            text = {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search a book…") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { onSearch(searchQuery) }) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    if (searchLoading) {
                        Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(searchResults) { book ->
                                Surface(
                                    onClick = {
                                        onSetCurrentBook(book)
                                        showPickerDialog = false
                                        searchQuery = ""
                                        onClearSearch()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        BookCover(
                                            coverUrl = book.coverUrl,
                                            modifier = Modifier.size(40.dp, 58.dp),
                                            cornerRadius = 4.dp
                                        )
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                book.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                book.author,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            "Select",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = {
                    showPickerDialog = false
                    searchQuery = ""
                    onClearSearch()
                }) { Text("Cancel") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            SectionLabel("Description")
            Spacer(Modifier.height(4.dp))
            Text(
                club.description.ifBlank { "No description provided." },
                style = MaterialTheme.typography.bodyMedium
            )
        }
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                SectionLabel("Currently reading")
                if (isOwner) {
                    IconButton(
                        onClick = { showPickerDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit currently reading",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            if (club.currentBook.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BookCover(
                            coverUrl = club.currentBookCover,
                            modifier = Modifier.size(56.dp, 84.dp),
                            cornerRadius = 6.dp
                        )
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                club.currentBook,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (club.currentBookAuthor.isNotBlank()) {
                                Text(
                                    club.currentBookAuthor,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    "No book selected yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            SectionLabel("Owner")
            Spacer(Modifier.height(4.dp))
            val owner = members.find { it.uid == club.ownerUid }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(owner?.username ?: "Reader", style = MaterialTheme.typography.bodyMedium)
                SuggestionChip(onClick = {}, label = { Text("Owner") })
                if (club.ownerUid == currentUid) {
                    SuggestionChip(onClick = {}, label = { Text("You") })
                }
            }
        }
    }
}

@Composable
private fun MembersTab(members: List<MemberShelf>) {
    if (members.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "No members yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(members) { member -> MemberCard(member) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MemberCard(member: MemberShelf) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        member.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    member.username,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (member.toRead.isEmpty() && member.reading.isEmpty() && member.read.isEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        "No books shared yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Spacer(Modifier.height(8.dp))
                ShelfChips("To Read", member.toRead)
                ShelfChips("Reading", member.reading)
                ShelfChips("Read", member.read)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShelfChips(label: String, books: List<ShelfEntry>) {
    if (books.isEmpty()) return
    val maxVisible = 3
    val visible = books.take(maxVisible)
    val overflow = books.size - maxVisible

    Column(Modifier.padding(vertical = 4.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            visible.forEach { book ->
                AssistChip(onClick = {}, label = { Text(book.title, maxLines = 1) })
            }
            if (overflow > 0) {
                AssistChip(
                    onClick = {},
                    label = { Text("+$overflow more", style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
    }
}

@Composable
private fun ChatTab(
    messages: List<ClubMessage>,
    currentUid: String?,
    isMember: Boolean,
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(
                        Modifier
                            .fillParentMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No messages yet. Say hello!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(messages) { msg ->
                    MessageBubble(msg, isMine = msg.authorUid == currentUid)
                }
            }
        }

        HorizontalDivider()

        if (isMember) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message…") },
                    maxLines = 3
                )
                IconButton(onClick = onSend, enabled = draft.isNotBlank()) {
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

@Composable
private fun MessageBubble(message: ClubMessage, isMine: Boolean) {
    val bubbleColor =
        if (isMine) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant

    val timestamp = remember(message.createdAt) {
        if (message.createdAt == 0L) ""
        else DateUtils.getRelativeTimeSpanString(
            message.createdAt,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    }

    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Surface(color = bubbleColor, shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(10.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isMine) "You" else message.authorName.ifBlank { "Reader" },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (timestamp.isNotEmpty()) {
                        Text(
                            timestamp,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(message.text, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

package com.example.readingcorner.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = viewModel()) {
    val toRead by viewModel.toReadCount.collectAsState(initial = 0)
    val reading by viewModel.readingCount.collectAsState(initial = 0)
    val read by viewModel.readCount.collectAsState(initial = 0)
    val socialBookstars by viewModel.socialBookstars.collectAsState(initial = 0)
    val username by viewModel.username.collectAsState(initial = "Reader")

    val bookstars = read * 10 + reading * 3 + toRead + socialBookstars
    val tier = starTier(bookstars)
    val tierName = tierName(tier)
    val nextTierThreshold = nextTierThreshold(bookstars)
    val currentTierThreshold = currentTierThreshold(bookstars)
    val progress = if (nextTierThreshold == null) 1f else {
        val range = nextTierThreshold - currentTierThreshold
        if (range <= 0) 1f else (bookstars - currentTierThreshold).toFloat() / range
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(username, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            viewModel.email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        // Tier badge
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "★".repeat(tier) + "☆".repeat(5 - tier),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    tierName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "$bookstars bookstars",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )

                if (nextTierThreshold != null) {
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${nextTierThreshold - bookstars} bookstars until next tier",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                } else {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Maximum tier reached!",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Shelf stats
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "My Shelf",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Stat("To Read", toRead)
                    VerticalDivider(modifier = Modifier.height(48.dp))
                    Stat("Reading", reading)
                    VerticalDivider(modifier = Modifier.height(48.dp))
                    Stat("Read", read)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Stat("Total", toRead + reading + read)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // How bookstars are earned
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "How to earn bookstars",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                BookstarRow("Finish a book (Read)", "+10")
                BookstarRow("Currently reading a book", "+3")
                BookstarRow("Add a book to To Read", "+1")
                BookstarRow("Join a reading club", "+15")
                BookstarRow("Create a forum thread", "+5")
                BookstarRow("Post in a forum", "+3")
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BookstarRow(label: String, points: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            points,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private fun starTier(bookstars: Int): Int = when {
    bookstars >= 200 -> 5
    bookstars >= 120 -> 4
    bookstars >= 60  -> 3
    bookstars >= 20  -> 2
    else             -> 1
}

private fun tierName(tier: Int): String = when (tier) {
    1    -> "Beginner"
    2    -> "Reader"
    3    -> "Bookworm"
    4    -> "Avid Reader"
    5    -> "Scholar"
    else -> "Reader"
}

private fun currentTierThreshold(bookstars: Int): Int = when {
    bookstars >= 200 -> 200
    bookstars >= 120 -> 120
    bookstars >= 60  -> 60
    bookstars >= 20  -> 20
    else             -> 0
}

private fun nextTierThreshold(bookstars: Int): Int? = when {
    bookstars >= 200 -> null
    bookstars >= 120 -> 200
    bookstars >= 60  -> 120
    bookstars >= 20  -> 60
    else             -> 20
}

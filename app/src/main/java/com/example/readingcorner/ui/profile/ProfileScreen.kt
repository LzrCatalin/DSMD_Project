package com.example.readingcorner.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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

    // Bookstars = local reading activity + cloud social activity (forum posts, club joins).
    val bookstars = read * 10 + reading * 3 + toRead + socialBookstars
    val tier = starTier(bookstars)

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(viewModel.email, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(8.dp))
        Text(
            text = "★".repeat(tier) + "☆".repeat(5 - tier),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(4.dp))
        AssistChip(
            onClick = {},
            label = { Text("$bookstars bookstars") }
        )

        Spacer(Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Stat("To Read", toRead)
                Stat("Reading", reading)
                Stat("Read", read)
            }
        }
    }
}

/** Maps a raw bookstars score to a 1–5 star tier shown on the profile. */
private fun starTier(bookstars: Int): Int = when {
    bookstars >= 200 -> 5
    bookstars >= 120 -> 4
    bookstars >= 60 -> 3
    bookstars >= 20 -> 2
    else -> 1
}

@Composable
private fun Stat(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

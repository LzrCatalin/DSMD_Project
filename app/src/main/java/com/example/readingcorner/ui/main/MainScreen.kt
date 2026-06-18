package com.example.readingcorner.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.example.readingcorner.ui.clubs.ClubsScreen
import com.example.readingcorner.ui.home.HomeContent
import com.example.readingcorner.ui.mybooks.MyBooksScreen
import com.example.readingcorner.ui.profile.ProfileScreen
import com.example.readingcorner.ui.search.SearchScreen

private enum class MainTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    SEARCH("Search", Icons.Default.Search),
    MY_BOOKS("My Books", Icons.AutoMirrored.Filled.MenuBook),
    CLUBS("Clubs", Icons.Default.Groups),
    PROFILE("Profile", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onBookClick: (String) -> Unit,
    onClubClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reading Corner", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                MainTab.HOME -> HomeContent(onBookClick = onBookClick)
                MainTab.SEARCH -> SearchScreen(onBookClick = onBookClick)
                MainTab.MY_BOOKS -> MyBooksScreen(onBookClick = onBookClick)
                MainTab.CLUBS -> ClubsScreen(onClubClick = onClubClick)
                MainTab.PROFILE -> ProfileScreen()
            }
        }
    }
}

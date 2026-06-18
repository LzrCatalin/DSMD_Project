package com.example.readingcorner.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.readingcorner.ui.auth.LoginScreen
import com.example.readingcorner.ui.auth.SignUpScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.readingcorner.ui.auth.AuthViewModel
import com.example.readingcorner.ui.clubs.ClubDetailScreen
import com.example.readingcorner.ui.detail.BookDetailScreen
import com.example.readingcorner.ui.forum.ForumThreadScreen
import com.example.readingcorner.ui.main.MainScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val authViewModel: AuthViewModel = viewModel()
    val errorMessage by authViewModel.errorMessage
    val currentUser by authViewModel.currentUser

    NavHost(
        navController = navController,
        startDestination = if (currentUser != null) "home" else "login"
    ) {

        composable("login") {
            LoginScreen(
                errorMessage = errorMessage,
                onLoginClick = { email, password ->
                    authViewModel.logIn(email, password) {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
                onNavigateToSignUp = {
                    authViewModel.clearError()
                    navController.navigate("signup")
                }
            )
        }

        composable("signup") {
            SignUpScreen(
                errorMessage = errorMessage,
                onSignUpClick = { username, email, password ->
                    authViewModel.signUp(username, email, password) {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
                onNavigateToLogin = {
                    authViewModel.clearError()
                    navController.popBackStack()
                }
            )
        }

        composable("home") {
            MainScreen(
                onBookClick = { bookId -> navController.navigate("detail/$bookId") },
                onClubClick = { clubId -> navController.navigate("clubDetail/$clubId") },
                onLogout = {
                    authViewModel.logOut {
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(
            route = "detail/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId").orEmpty()
            BookDetailScreen(
                bookId = bookId,
                onBack = { navController.popBackStack() },
                onOpenForum = { bId, threadId -> navController.navigate("forum/$bId/$threadId") }
            )
        }

        composable(
            route = "forum/{bookId}/{threadId}",
            arguments = listOf(
                navArgument("bookId") { type = NavType.StringType },
                navArgument("threadId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val fBookId = backStackEntry.arguments?.getString("bookId").orEmpty()
            val threadId = backStackEntry.arguments?.getString("threadId").orEmpty()
            ForumThreadScreen(
                bookId = fBookId,
                threadId = threadId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "clubDetail/{clubId}",
            arguments = listOf(navArgument("clubId") { type = NavType.StringType })
        ) { backStackEntry ->
            val clubId = backStackEntry.arguments?.getString("clubId").orEmpty()
            ClubDetailScreen(
                clubId = clubId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
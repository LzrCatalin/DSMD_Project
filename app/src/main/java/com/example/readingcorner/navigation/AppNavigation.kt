package com.example.readingcorner.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.readingcorner.ui.auth.LoginScreen
import com.example.readingcorner.ui.auth.SignUpScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.readingcorner.ui.auth.AuthViewModel
import com.example.readingcorner.ui.home.HomeScreen

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
            HomeScreen(
                onLogoutClick = {
                    authViewModel.logOut {
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}
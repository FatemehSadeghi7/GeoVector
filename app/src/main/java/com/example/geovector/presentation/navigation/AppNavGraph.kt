package com.example.geovector.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.geovector.presentation.screens.login.LoginScreen
import com.example.geovector.presentation.screens.map.MapScreen
import com.example.geovector.presentation.screens.register.RegisterScreen
import com.example.geovector.presentation.screens.welcome.WelcomeScreen

@Composable
fun AppNavGraph(nav: NavHostController) {
    NavHost(navController = nav, startDestination = Routes.WELCOME) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onLogin = { nav.navigate(Routes.LOGIN) },
                onRegister = { nav.navigate(Routes.REGISTER) }
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    nav.navigate(Routes.MAP) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBack = {
                    nav.navigate(Routes.WELCOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisteredGoToLogin = {
                    nav.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
                onBack = {
                    nav.navigate(Routes.WELCOME) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.MAP) {
            MapScreen(
                onLogout = {
                    nav.navigate(Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
package com.example.geovector.presentation.navigation

import MapScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.geovector.presentation.screens.login.LoginScreen
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
            LoginScreen(onLoginSuccess = {
                // After successful login, navigate to map screen
                nav.navigate(Routes.MAP) {
                    popUpTo(Routes.LOGIN) { inclusive = true } // Pop back to avoid returning to login screen
                }
            })
        }
        composable(Routes.REGISTER) {
            RegisterScreen(onRegisteredGoToLogin = {
                nav.navigate(Routes.LOGIN) {
                    popUpTo(Routes.WELCOME) { inclusive = false }
                }
            })
        }
        composable(Routes.MAP) {
            MapScreen()  // The screen to display map
        }
    }
}

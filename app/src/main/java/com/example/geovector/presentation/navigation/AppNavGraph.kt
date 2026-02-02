package com.example.geovector.presentation.navigation

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
        composable(Routes.LOGIN) { LoginScreen() }
        composable(Routes.REGISTER) { RegisterScreen(onRegisteredGoToLogin = {
            nav.navigate(Routes.LOGIN) {
                popUpTo(Routes.WELCOME) { inclusive = false }
            }
        }) }
    }
}

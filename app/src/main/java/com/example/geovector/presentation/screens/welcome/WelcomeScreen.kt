package com.example.geovector.presentation.screens.welcome

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WelcomeScreen(
    onLogin: () -> Unit,
    onRegister: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("خوش آمدید", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onLogin,
                modifier = Modifier.fillMaxWidth()
            ) { Text("ورود") }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onRegister,
                modifier = Modifier.fillMaxWidth()
            ) { Text("ثبت نام") }
        }
    }
}

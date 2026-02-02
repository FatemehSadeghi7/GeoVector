package com.example.geovector.presentation.screens.register

import RegisterViewModelFactory
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lint.kotlin.metadata.Visibility
import com.example.geovector.di.AppModule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisteredGoToLogin: () -> Unit
) {
    // ✅ RTL فقط برای همین صفحه
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {

        val context = LocalContext.current.applicationContext
        val factory = remember {
            RegisterViewModelFactory(AppModule.provideRegisterUseCase(context))
        }
        val vm: RegisterViewModel = viewModel(factory = factory)
        val state by vm.state.collectAsState()

        val snackbarHostState = remember { SnackbarHostState() }
        var showPassword by remember { mutableStateOf(false) }

        // پیام‌ها به صورت Snackbar
        LaunchedEffect(state.message) {
            val msg = state.message ?: return@LaunchedEffect
            snackbarHostState.showSnackbar(message = msg)
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("ثبت نام") }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { padding ->

            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(18.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "حساب کاربری بسازید",
                            style = MaterialTheme.typography.titleLarge
                        )

                        OutlinedTextField(
                            value = state.fullName,
                            onValueChange = vm::onFullNameChange,
                            label = { Text("نام و نام خانوادگی") },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            )
                        )

                        OutlinedTextField(
                            value = state.email,
                            onValueChange = vm::onEmailChange,
                            label = { Text("ایمیل") },
                            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )
                        )

                        OutlinedTextField(
                            value = state.password,
                            onValueChange = vm::onPasswordChange,
                            label = { Text("پسورد") },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            )
                        )

                        Spacer(Modifier.height(4.dp))

                        Button(
                            onClick = { vm.submit(onRegisteredGoToLogin) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            enabled = !state.isLoading
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(strokeWidth = 2.dp)
                            } else {
                                Text("ثبت نام")
                            }
                        }

                        TextButton(
                            onClick = onRegisteredGoToLogin,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("قبلاً حساب ساخته‌اید؟ ورود")
                        }
                    }
                }
            }
        }
    }
}

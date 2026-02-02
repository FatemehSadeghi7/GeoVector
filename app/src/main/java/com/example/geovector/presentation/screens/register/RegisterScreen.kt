package com.example.geovector.presentation.screens.register

import RegisterViewModelFactory
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tag
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
import com.example.geovector.di.AppModule
import com.example.geovector.presentation.components.JalaliBirthDatePickerDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisteredGoToLogin: () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {


        val context = LocalContext.current.applicationContext
        val factory = remember {
            RegisterViewModelFactory(AppModule.provideRegisterUseCase(context))
        }
        val vm: RegisterViewModel = viewModel(factory = factory)
        var showJalaliPicker by remember { mutableStateOf(false) }

        if (showJalaliPicker) {
            JalaliBirthDatePickerDialog(
                initial = null,
                onDismiss = { showJalaliPicker = false },
                onConfirm = { j ->
                    vm.onBirthDateJalaliSelected(j)
                    showJalaliPicker = false
                }
            )
        }

        val state by vm.state.collectAsState()

        val snackbarHostState = remember { SnackbarHostState() }
        var showPassword by remember { mutableStateOf(false) }
        var showDatePicker by remember { mutableStateOf(false) }

        // Snackbar message
        LaunchedEffect(state.message) {
            state.message?.let { snackbarHostState.showSnackbar(it) }
        }

        // Date formatting
        val dateText = remember(state.birthDateMillis) {
            if (state.birthDateMillis <= 0L) "انتخاب تاریخ تولد"
            else {
                val df = SimpleDateFormat("yyyy/MM/dd", Locale("fa", "IR"))
                df.format(Date(state.birthDateMillis))
            }
        }

        // DatePicker dialog
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = state.birthDateMillis.takeIf { it > 0L }
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val selected = datePickerState.selectedDateMillis ?: 0L
                        vm.onBirthDateChange(selected)
                        showDatePicker = false
                    }) { Text("تأیید") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("لغو") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Scaffold(
            topBar = { CenterAlignedTopAppBar(title = { Text("ثبت نام") }) },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { padding ->

            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
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
                            text = "حساب جدید بسازید",
                            style = MaterialTheme.typography.titleLarge
                        )

                        // Full name
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

                        // Age
                        OutlinedTextField(
                            value = state.age,
                            onValueChange = vm::onAgeChange,
                            label = { Text("سن") },
                            leadingIcon = { Icon(Icons.Filled.Tag, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            )
                        )
                        OutlinedButton(
                            onClick = { showJalaliPicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // متن را از state.birthDateMillis نشان می‌دهیم (یا اگر خواستی Jalali string هم نگه می‌داریم)
                            Text(if (state.birthDateMillis > 0L) "تاریخ تولد ثبت شد" else "انتخاب تاریخ تولد (شمسی)")
                        }


                        // Birth date picker button
                       /* OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Cake, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(dateText)
                        }*/

                        // Username
                        OutlinedTextField(
                            value = state.username,
                            onValueChange = vm::onUsernameChange,
                            label = { Text("نام کاربری") },
                            leadingIcon = { Icon(Icons.Filled.Tag, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            )
                        )

                        // Password
                        OutlinedTextField(
                            value = state.password,
                            onValueChange = vm::onPasswordChange,
                            label = { Text("رمز عبور") },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation =
                                if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            )
                        )

                        Spacer(Modifier.height(4.dp))

                        // Register button
                        Button(
                            onClick = { vm.submit(onRegisteredGoToLogin) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
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
                            Text("قبلاً حساب دارید؟ ورود")
                        }
                    }
                }
            }
        }
    }
}

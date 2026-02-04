package com.example.geovector.presentation.screens.register

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AppRegistration
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.geovector.di.AppModule
import com.example.geovector.presentation.components.JalaliBirthDatePickerDialog

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun RegisterScreen(
    onRegisteredGoToLogin: () -> Unit,
    onBack: () -> Unit = {}
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
        val snackBarHostState = remember { SnackbarHostState() }
        var showPassword by remember { mutableStateOf(false) }

        val isDark = isSystemInDarkTheme()
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        val isTablet = screenWidth >= 600

        val primaryGradient = if (isDark) {
            Brush.linearGradient(listOf(Color(0xFF1B5E20), Color(0xFF004D40)))
        } else {
            Brush.linearGradient(listOf(Color(0xFF43A047), Color(0xFF00897B)))
        }

        val surfaceColor = if (isDark) Color(0xFF1A1A2E) else Color(0xFFF8FAF8)
        val cardColor = if (isDark) Color(0xFF16213E) else Color.White
        val accentColor = if (isDark) Color(0xFF66BB6A) else Color(0xFF2E7D32)
        val subtitleColor = if (isDark) Color(0xFFB0BEC5) else Color(0xFF607D8B)

        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }

        LaunchedEffect(state.message) {
            state.message?.let { snackBarHostState.showSnackbar(it) }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
            containerColor = surfaceColor
        ) { padding ->

            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(primaryGradient)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = if (isTablet) (screenWidth * 0.15f).dp else 20.dp)
                        .padding(top = 40.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn() + slideInVertically(
                            initialOffsetY = { -60 },
                            animationSpec = spring(stiffness = Spring.StiffnessLow)
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                modifier = Modifier.size(72.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                tonalElevation = 0.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Outlined.HowToReg,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp),
                                        tint = Color.White
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = "ثبت نام",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = "حساب جدید خود را بسازید",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn() + slideInVertically(
                            initialOffsetY = { 100 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    ) {
                        Card(
                            modifier = Modifier
                                .widthIn(max = 500.dp)
                                .fillMaxWidth()
                                .shadow(
                                    elevation = if (isDark) 8.dp else 16.dp,
                                    shape = RoundedCornerShape(28.dp),
                                    ambientColor = if (isDark) Color.Black else Color(0xFF2E7D32).copy(alpha = 0.15f),
                                    spotColor = if (isDark) Color.Black else Color(0xFF2E7D32).copy(alpha = 0.1f)
                                ),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 24.dp, vertical = 28.dp)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                StyledTextField(
                                    value = state.fullName,
                                    onValueChange = vm::onFullNameChange,
                                    label = "نام و نام خانوادگی",
                                    icon = Icons.Filled.Person,
                                    accentColor = accentColor,
                                    isDark = isDark,
                                    imeAction = ImeAction.Next
                                )

                                StyledTextField(
                                    value = state.age,
                                    onValueChange = vm::onAgeChange,
                                    label = "سن",
                                    icon = Icons.Filled.Tag,
                                    accentColor = accentColor,
                                    isDark = isDark,
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                )

                                OutlinedButton(
                                    onClick = { showJalaliPicker = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    border = ButtonDefaults.outlinedButtonBorder,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = if (state.birthDateMillis > 0L) accentColor else subtitleColor
                                    )
                                ) {
                                    Icon(
                                        Icons.Filled.Cake,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = if (state.birthDateMillis > 0L) "تاریخ تولد ثبت شد ✓" else "انتخاب تاریخ تولد (شمسی)",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }

                                StyledTextField(
                                    value = state.username,
                                    onValueChange = vm::onUsernameChange,
                                    label = "نام کاربری",
                                    icon = Icons.Filled.Tag,
                                    accentColor = accentColor,
                                    isDark = isDark,
                                    imeAction = ImeAction.Next
                                )

                                StyledPasswordField(
                                    value = state.password,
                                    onValueChange = vm::onPasswordChange,
                                    showPassword = showPassword,
                                    onTogglePassword = { showPassword = !showPassword },
                                    accentColor = accentColor,
                                    isDark = isDark
                                )

                                Spacer(Modifier.height(8.dp))

                                Button(
                                    onClick = { vm.submit(onRegisteredGoToLogin) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp),
                                    enabled = !state.isLoading,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accentColor,
                                        disabledContainerColor = accentColor.copy(alpha = 0.4f)
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 4.dp,
                                        pressedElevation = 8.dp
                                    )
                                ) {
                                    if (state.isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(22.dp),
                                            strokeWidth = 2.5.dp,
                                            color = Color.White
                                        )
                                    } else {
                                        Icon(
                                            Icons.Outlined.AppRegistration,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "ثبت نام",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    TextButton(onClick = onRegisteredGoToLogin) {
                                        Text(
                                            "قبلاً حساب دارید؟ ورود",
                                            color = accentColor,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}


@Composable
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    accentColor: Color,
    isDark: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done
) {
    var isFocused by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isFocused) accentColor else {
                    if (isDark) Color(0xFF78909C) else Color(0xFF90A4AE)
                },
                modifier = Modifier.size(20.dp)
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accentColor,
            unfocusedBorderColor = if (isDark) Color(0xFF37474F) else Color(0xFFE0E0E0),
            focusedLabelColor = accentColor,
            cursorColor = accentColor,
            focusedContainerColor = if (isDark) Color(0xFF1A2744) else Color(0xFFF1F8E9),
            unfocusedContainerColor = Color.Transparent
        )
    )
}

@Composable
private fun StyledPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    showPassword: Boolean,
    onTogglePassword: () -> Unit,
    accentColor: Color,
    isDark: Boolean
) {
    var isFocused by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("رمز عبور") },
        leadingIcon = {
            Icon(
                Icons.Filled.Lock,
                contentDescription = null,
                tint = if (isFocused) accentColor else {
                    if (isDark) Color(0xFF78909C) else Color(0xFF90A4AE)
                },
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            IconButton(onClick = onTogglePassword) {
                Icon(
                    imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF78909C) else Color(0xFF90A4AE),
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accentColor,
            unfocusedBorderColor = if (isDark) Color(0xFF37474F) else Color(0xFFE0E0E0),
            focusedLabelColor = accentColor,
            cursorColor = accentColor,
            focusedContainerColor = if (isDark) Color(0xFF1A2744) else Color(0xFFF1F8E9),
            unfocusedContainerColor = Color.Transparent
        )
    )
}

package com.example.geovector.presentation.screens.login

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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Login
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

@Composable
fun LoginScreen(
    onLoginSuccess: (() -> Unit)? = null,
    onBack: () -> Unit = {}
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {

        val context = LocalContext.current.applicationContext
        val factory = remember {
            LoginViewModelFactory(
                AppModule.provideLoginUseCase(context),
                AppModule.provideDatabase(context).userDao()
            )
        }
        val vm: LoginViewModel = viewModel(factory = factory)
        val state by vm.state.collectAsState()

        val snackbarHostState = remember { SnackbarHostState() }
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

        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }

        LaunchedEffect(state.message) {
            state.message?.let { snackbarHostState.showSnackbar(it) }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                        .height(260.dp)
                        .background(primaryGradient)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = if (isTablet) (screenWidth * 0.15f).dp else 20.dp)
                        .padding(top = 60.dp, bottom = 24.dp),
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
                                modifier = Modifier.size(80.dp),
                                shape = RoundedCornerShape(24.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                tonalElevation = 0.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Outlined.Login,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp),
                                        tint = Color.White
                                    )
                                }
                            }

                            Spacer(Modifier.height(18.dp))

                            Text(
                                text = "خوش آمدید",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = "به حساب خود وارد شوید",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(36.dp))

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
                                    .padding(horizontal = 24.dp, vertical = 32.dp)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(18.dp)
                            ) {
                                LoginTextField(
                                    value = state.username,
                                    onValueChange = vm::onUsernameChange,
                                    label = "نام کاربری",
                                    icon = Icons.Filled.Person,
                                    accentColor = accentColor,
                                    isDark = isDark,
                                    imeAction = ImeAction.Next
                                )

                                LoginPasswordField(
                                    value = state.password,
                                    onValueChange = vm::onPasswordChange,
                                    showPassword = showPassword,
                                    onTogglePassword = { showPassword = !showPassword },
                                    accentColor = accentColor,
                                    isDark = isDark
                                )

                                Spacer(Modifier.height(8.dp))

                                Button(
                                    onClick = { vm.submit(onLoginSuccess) },
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
                                            Icons.Outlined.Login,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "ورود",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
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
private fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    accentColor: Color,
    isDark: Boolean,
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
private fun LoginPasswordField(
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

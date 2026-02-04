package com.example.geovector.presentation.screens.welcome

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun WelcomeScreen(
    onLogin: () -> Unit,
    onRegister: () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {

        val isDark = isSystemInDarkTheme()
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        val isTablet = screenWidth >= 600

        // رنگ‌ها
        val bgGradient = if (isDark) {
            Brush.verticalGradient(
                listOf(Color(0xFF0D1B2A), Color(0xFF1A1A2E), Color(0xFF16213E))
            )
        } else {
            Brush.verticalGradient(
                listOf(Color(0xFFE8F5E9), Color(0xFFF1F8E9), Color(0xFFF8FAF8))
            )
        }

        val accentColor = if (isDark) Color(0xFF66BB6A) else Color(0xFF2E7D32)
        val accentLight = if (isDark) Color(0xFF81C784) else Color(0xFF43A047)
        val cardColor = if (isDark) Color(0xFF16213E) else Color.White
        val subtitleColor = if (isDark) Color(0xFF90A4AE) else Color(0xFF607D8B)

        var showLogo by remember { mutableStateOf(false) }
        var showTitle by remember { mutableStateOf(false) }
        var showSubtitle by remember { mutableStateOf(false) }
        var showCard by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            showLogo = true
            delay(200)
            showTitle = true
            delay(150)
            showSubtitle = true
            delay(200)
            showCard = true
        }

        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )

        val wavePhase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2f * PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "wave"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (isDark) 0.08f else 0.12f)
            ) {
                val w = size.width
                val h = size.height

                val path1 = Path().apply {
                    moveTo(0f, h * 0.35f)
                    for (x in 0..w.toInt() step 4) {
                        val y = h * 0.35f + sin(x * 0.008f + wavePhase) * 40f
                        lineTo(x.toFloat(), y)
                    }
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(path1, color = Color(0xFF43A047), style = Fill)

                val path2 = Path().apply {
                    moveTo(0f, h * 0.55f)
                    for (x in 0..w.toInt() step 4) {
                        val y = h * 0.55f + sin(x * 0.006f + wavePhase + 1.5f) * 30f
                        lineTo(x.toFloat(), y)
                    }
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(path2, color = Color(0xFF2E7D32), style = Fill)

                drawCircle(
                    color = Color(0xFF66BB6A),
                    radius = 120f,
                    center = Offset(w * 0.85f, h * 0.15f),
                    style = Stroke(width = 2f)
                )
                drawCircle(
                    color = Color(0xFF43A047),
                    radius = 80f,
                    center = Offset(w * 0.1f, h * 0.75f),
                    style = Stroke(width = 1.5f)
                )
                drawCircle(
                    color = Color(0xFF81C784),
                    radius = 50f,
                    center = Offset(w * 0.7f, h * 0.65f),
                    style = Stroke(width = 1f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = if (isTablet) (screenWidth * 0.2f).dp else 28.dp)
                    .padding(top = if (isTablet) 80.dp else 100.dp, bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(Modifier.weight(0.8f))

                AnimatedVisibility(
                    visible = showLogo,
                    enter = fadeIn(tween(600)) + slideInVertically(
                        initialOffsetY = { -80 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                ) {
                    Surface(
                        modifier = Modifier.size((80 * pulseScale).dp),
                        shape = RoundedCornerShape(24.dp),
                        color = accentColor,
                        shadowElevation = 12.dp,
                        tonalElevation = 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(42.dp),
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                AnimatedVisibility(
                    visible = showTitle,
                    enter = fadeIn(tween(500)) + slideInVertically(
                        initialOffsetY = { 40 },
                        animationSpec = spring(stiffness = Spring.StiffnessLow)
                    )
                ) {
                    Text(
                        text = "Geo Vector",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) Color.White else Color(0xFF1B5E20),
                            letterSpacing = 1.sp
                        )
                    )
                }

                Spacer(Modifier.height(10.dp))

                AnimatedVisibility(
                    visible = showSubtitle,
                    enter = fadeIn(tween(500)) + slideInVertically(
                        initialOffsetY = { 30 },
                        animationSpec = spring(stiffness = Spring.StiffnessLow)
                    )
                ) {
                    Text(
                        text = "ردیابی هوشمند مسیر شما",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = subtitleColor,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.3.sp
                        )
                    )
                }

                Spacer(Modifier.weight(1f))

                AnimatedVisibility(
                    visible = showCard,
                    enter = fadeIn(tween(400)) + slideInVertically(
                        initialOffsetY = { 120 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                ) {
                    Card(
                        modifier = Modifier
                            .widthIn(max = 440.dp)
                            .fillMaxWidth()
                            .shadow(
                                elevation = if (isDark) 8.dp else 20.dp,
                                shape = RoundedCornerShape(28.dp),
                                ambientColor = if (isDark) Color.Black else accentColor.copy(alpha = 0.12f),
                                spotColor = if (isDark) Color.Black else accentColor.copy(alpha = 0.08f)
                            ),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 24.dp, vertical = 28.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "شروع کنید",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color(0xFF1B5E20)
                                )
                            )

                            Spacer(Modifier.height(4.dp))

                            // دکمه ورود
                            Button(
                                onClick = onLogin,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 8.dp
                                )
                            ) {
                                Icon(
                                    Icons.Outlined.Login,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "ورود به حساب",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                )
                            }

                            OutlinedButton(
                                onClick = onRegister,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.linearGradient(
                                        listOf(accentColor, accentLight)
                                    )
                                )
                            ) {
                                Icon(
                                    Icons.Outlined.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = accentColor
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "ساخت حساب جدید",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = accentColor
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                AnimatedVisibility(
                    visible = showCard,
                    enter = fadeIn(tween(600, delayMillis = 200))
                ) {
                    Text(
                        text = "نسخه ۱.۰.۰ GeoVector ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = subtitleColor.copy(alpha = 0.6f),
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
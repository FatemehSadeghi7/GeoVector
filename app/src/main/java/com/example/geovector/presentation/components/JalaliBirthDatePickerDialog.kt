package com.example.geovector.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.geovector.core.date.JalaliDate

@Composable
fun JalaliBirthDatePickerDialog(
    initial: JalaliDate? = null,
    onDismiss: () -> Unit,
    onConfirm: (JalaliDate) -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {

        val isDark = isSystemInDarkTheme()
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        val isTablet = screenWidth >= 600

        val accentColor = if (isDark) Color(0xFF66BB6A) else Color(0xFF2E7D32)
        val surfaceColor = if (isDark) Color(0xFF16213E) else Color.White
        val headerGradient = if (isDark) {
            Brush.linearGradient(listOf(Color(0xFF1B5E20), Color(0xFF004D40)))
        } else {
            Brush.linearGradient(listOf(Color(0xFF43A047), Color(0xFF00897B)))
        }
        val columnBg = if (isDark) Color(0xFF1A2744) else Color(0xFFF5F9F5)
        val selectedBg = if (isDark) Color(0xFF1B5E20).copy(alpha = 0.5f) else Color(0xFFE8F5E9)
        val selectedBorder = accentColor
        val unselectedText = if (isDark) Color(0xFFB0BEC5) else Color(0xFF607D8B)
        val dividerColor = if (isDark) Color(0xFF37474F) else Color(0xFFE8E8E8)

        val currentJalaliYear = remember { 1404 }
        val years = remember { (1300..currentJalaliYear).toList().reversed() }
        val months = remember { (1..12).toList() }

        val jalaliMonthNames = remember {
            listOf(
                "فروردین", "اردیبهشت", "خرداد",
                "تیر", "مرداد", "شهریور",
                "مهر", "آبان", "آذر",
                "دی", "بهمن", "اسفند"
            )
        }

        var selectedYear by remember { mutableStateOf(initial?.jy ?: 1380) }
        var selectedMonth by remember { mutableStateOf(initial?.jm ?: 1) }
        var selectedDay by remember { mutableStateOf(initial?.jd ?: 1) }

        val daysInMonth = remember(selectedYear, selectedMonth) {
            when (selectedMonth) {
                in 1..6 -> 31
                in 7..11 -> 30
                12 -> if (isJalaliLeap(selectedYear)) 30 else 29
                else -> 30
            }
        }
        val days = remember(daysInMonth) { (1..daysInMonth).toList() }

        if (selectedDay > daysInMonth) selectedDay = daysInMonth

        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + scaleIn(
                    initialScale = 0.9f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            ) {
                Card(
                    modifier = Modifier
                        .widthIn(max = if (isTablet) 520.dp else 400.dp)
                        .fillMaxWidth(if (isTablet) 0.6f else 0.92f)
                        .shadow(
                            elevation = if (isDark) 12.dp else 24.dp,
                            shape = RoundedCornerShape(28.dp),
                            ambientColor = if (isDark) Color.Black else Color(0xFF2E7D32).copy(alpha = 0.15f)
                        ),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(headerGradient)
                                .padding(vertical = 20.dp, horizontal = 24.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Outlined.CalendarToday,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "انتخاب تاریخ تولد",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Spacer(Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "%04d / %s / %02d".format(
                                            selectedYear,
                                            jalaliMonthNames[selectedMonth - 1],
                                            selectedDay
                                        ),
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                }
                            }
                        }

                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ScrollPickerColumn(
                                    title = "سال",
                                    items = years,
                                    selected = selectedYear,
                                    onSelect = { selectedYear = it },
                                    displayMapper = { it.toString() },
                                    modifier = Modifier.weight(1f),
                                    columnBg = columnBg,
                                    selectedBg = selectedBg,
                                    selectedBorder = selectedBorder,
                                    accentColor = accentColor,
                                    unselectedText = unselectedText,
                                    isDark = isDark
                                )

                                ScrollPickerColumn(
                                    title = "ماه",
                                    items = months,
                                    selected = selectedMonth,
                                    onSelect = { selectedMonth = it },
                                    displayMapper = { jalaliMonthNames[it - 1] },
                                    modifier = Modifier.weight(1f),
                                    columnBg = columnBg,
                                    selectedBg = selectedBg,
                                    selectedBorder = selectedBorder,
                                    accentColor = accentColor,
                                    unselectedText = unselectedText,
                                    isDark = isDark
                                )

                                ScrollPickerColumn(
                                    title = "روز",
                                    items = days,
                                    selected = selectedDay,
                                    onSelect = { selectedDay = it },
                                    displayMapper = { "%02d".format(it) },
                                    modifier = Modifier.weight(1f),
                                    columnBg = columnBg,
                                    selectedBg = selectedBg,
                                    selectedBorder = selectedBorder,
                                    accentColor = accentColor,
                                    unselectedText = unselectedText,
                                    isDark = isDark
                                )
                            }
                        }

                        HorizontalDivider(
                            color = dividerColor,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = ButtonDefaults.outlinedButtonBorder
                            ) {
                                Text(
                                    "انصراف",
                                    color = unselectedText,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Button(
                                onClick = {
                                    onConfirm(JalaliDate(selectedYear, selectedMonth, selectedDay))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 3.dp,
                                    pressedElevation = 6.dp
                                )
                            ) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "تأیید",
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> ScrollPickerColumn(
    title: String,
    items: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    displayMapper: (T) -> String,
    modifier: Modifier = Modifier,
    columnBg: Color,
    selectedBg: Color,
    selectedBorder: Color,
    accentColor: Color,
    unselectedText: Color,
    isDark: Boolean
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(selected) {
        val index = items.indexOf(selected)
        if (index >= 0) {
            listState.animateScrollToItem(
                index = maxOf(0, index - 2)
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(columnBg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(top = 10.dp, bottom = 6.dp),
            style = MaterialTheme.typography.labelMedium.copy(
                color = accentColor,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(items) { item ->
                val isSelected = item == selected
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .then(
                            if (isSelected) {
                                Modifier
                                    .background(selectedBg)
                                    .border(
                                        width = 1.5.dp,
                                        color = selectedBorder.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                            } else Modifier
                        )
                        .clickable {
                            onSelect(item)
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayMapper(item),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) accentColor else unselectedText,
                            fontSize = if (isSelected) 15.sp else 13.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun isJalaliLeap(jy: Int): Boolean {
    val mod = (jy + 38) % 2820
    val leap = ((mod + 474) + 38) * 682 % 2816 < 682
    return leap
}
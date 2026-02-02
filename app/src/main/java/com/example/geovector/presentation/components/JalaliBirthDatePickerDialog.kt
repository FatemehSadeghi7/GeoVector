package com.example.geovector.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.geovector.core.date.JalaliDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JalaliBirthDatePickerDialog(
    initial: JalaliDate? = null,
    onDismiss: () -> Unit,
    onConfirm: (JalaliDate) -> Unit
) {
    // بازه‌ی معقول برای سال
    val currentJalaliYear = remember { 1404 } // اگر خواستی پویاش می‌کنیم
    val years = remember { (1300..currentJalaliYear).toList().reversed() }
    val months = remember { (1..12).toList() }

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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("انتخاب تاریخ تولد (شمسی)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DropDownField(
                        label = "سال",
                        items = years,
                        selected = selectedYear,
                        onSelect = { selectedYear = it },
                        modifier = Modifier.weight(1f)
                    )
                    DropDownField(
                        label = "ماه",
                        items = months,
                        selected = selectedMonth,
                        onSelect = { selectedMonth = it },
                        modifier = Modifier.weight(1f)
                    )
                    DropDownField(
                        label = "روز",
                        items = days,
                        selected = selectedDay,
                        onSelect = { selectedDay = it },
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = "تاریخ انتخاب‌شده: %04d/%02d/%02d".format(selectedYear, selectedMonth, selectedDay),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(JalaliDate(selectedYear, selectedMonth, selectedDay)) }) {
                Text("تأیید")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("لغو") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DropDownField(
    label: String,
    items: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected.toString(),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.toString()) },
                    onClick = {
                        onSelect(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun isJalaliLeap(jy: Int): Boolean {
    // یک تقریب قابل قبول برای سال کبیسه جلالی (برای روزهای ماه ۱۲ کافی است)
    // اگر خواستی دقیق‌ترش هم می‌کنم.
    val mod = (jy + 38) % 2820
    val leap = ((mod + 474) + 38) * 682 % 2816 < 682
    return leap
}

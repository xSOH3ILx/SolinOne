package com.solinone.features.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solinone.core.calendar.IslamicDate
import com.solinone.core.calendar.Jdn
import com.solinone.core.calendar.PersianDate
import com.solinone.core.calendar.toPersianDigits

/**
 * Universal Date Converter & Calculator (مبدل جامع تاریخ خورشیدی، میلادی، قمری)
 * Ported from upstream Persian Calendar ConverterScreen architecture.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateConverterDialog(
    initialDate: PersianDate,
    onDismissRequest: () -> Unit,
    onSelectDate: (PersianDate) -> Unit
) {
    var selectedCalendarIndex by remember { mutableIntStateOf(0) } // 0: Solar, 1: Gregorian, 2: Islamic

    var solarYear by remember { mutableIntStateOf(initialDate.year) }
    var solarMonth by remember { mutableIntStateOf(initialDate.month) }
    var solarDay by remember { mutableIntStateOf(initialDate.day) }

    val initialGregorian = remember { initialDate.toJdn().toGregorian() }
    var gregYear by remember { mutableIntStateOf(initialGregorian.first) }
    var gregMonth by remember { mutableIntStateOf(initialGregorian.second) }
    var gregDay by remember { mutableIntStateOf(initialGregorian.third) }

    val initialIslamic = remember { IslamicDate.fromJdn(initialDate.toJdn()) }
    var islYear by remember { mutableIntStateOf(initialIslamic.year) }
    var islMonth by remember { mutableIntStateOf(initialIslamic.month) }
    var islDay by remember { mutableIntStateOf(initialIslamic.day) }

    // Derive current active JDN based on selected calendar mode
    val currentJdn = when (selectedCalendarIndex) {
        0 -> Jdn.fromPersian(solarYear, solarMonth, solarDay)
        1 -> Jdn.fromGregorian(gregYear, gregMonth, gregDay)
        else -> IslamicDate(islYear, islMonth, islDay).toJdn()
    }

    val derivedPersian = currentJdn.toPersian().let { PersianDate(it.first, it.second, it.third) }
    val derivedGregorian = currentJdn.toGregorian()
    val derivedIslamic = IslamicDate.fromJdn(currentJdn)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = "مبدل تاریخ جامع SolinOne",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Calendar selector tabs
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = selectedCalendarIndex == 0,
                        onClick = { selectedCalendarIndex = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                    ) {
                        Text("خورشیدی")
                    }
                    SegmentedButton(
                        selected = selectedCalendarIndex == 1,
                        onClick = { selectedCalendarIndex = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                    ) {
                        Text("میلادی")
                    }
                    SegmentedButton(
                        selected = selectedCalendarIndex == 2,
                        onClick = { selectedCalendarIndex = 2 },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                    ) {
                        Text("قمری")
                    }
                }

                // Input fields based on selected calendar
                when (selectedCalendarIndex) {
                    0 -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = solarDay.toString(),
                                onValueChange = { solarDay = it.toIntOrNull()?.coerceIn(1, 31) ?: 1 },
                                label = { Text("روز") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = solarMonth.toString(),
                                onValueChange = { solarMonth = it.toIntOrNull()?.coerceIn(1, 12) ?: 1 },
                                label = { Text("ماه") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = solarYear.toString(),
                                onValueChange = { solarYear = it.toIntOrNull()?.coerceIn(1200, 1500) ?: 1405 },
                                label = { Text("سال") },
                                modifier = Modifier.weight(1.5f),
                                singleLine = true
                            )
                        }
                    }
                    1 -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = gregDay.toString(),
                                onValueChange = { gregDay = it.toIntOrNull()?.coerceIn(1, 31) ?: 1 },
                                label = { Text("روز") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = gregMonth.toString(),
                                onValueChange = { gregMonth = it.toIntOrNull()?.coerceIn(1, 12) ?: 1 },
                                label = { Text("ماه") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = gregYear.toString(),
                                onValueChange = { gregYear = it.toIntOrNull()?.coerceIn(1800, 2100) ?: 2026 },
                                label = { Text("سال") },
                                modifier = Modifier.weight(1.5f),
                                singleLine = true
                            )
                        }
                    }
                    2 -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = islDay.toString(),
                                onValueChange = { islDay = it.toIntOrNull()?.coerceIn(1, 30) ?: 1 },
                                label = { Text("روز") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = islMonth.toString(),
                                onValueChange = { islMonth = it.toIntOrNull()?.coerceIn(1, 12) ?: 1 },
                                label = { Text("ماه") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = islYear.toString(),
                                onValueChange = { islYear = it.toIntOrNull()?.coerceIn(1250, 1500) ?: 1447 },
                                label = { Text("سال") },
                                modifier = Modifier.weight(1.5f),
                                singleLine = true
                            )
                        }
                    }
                }

                HorizontalDivider()

                // Result card showing equivalent dates across all 3 calendars
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "روز هفته: ${derivedPersian.dayOfWeekName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "خورشیدی: ${derivedPersian.day} ${derivedPersian.monthName} ${derivedPersian.year}".toPersianDigits(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "میلادی: ${derivedGregorian.third}/${derivedGregorian.second.toString().padStart(2, '0')}/${derivedGregorian.first}".toPersianDigits(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "قمری: ${derivedIslamic.day} ${derivedIslamic.monthName} ${derivedIslamic.year}".toPersianDigits(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSelectDate(derivedPersian)
                onDismissRequest()
            }) {
                Text("مشاهده در تقویم")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("بستن")
            }
        }
    )
}

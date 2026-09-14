package com.solinone.features.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solinone.core.calendar.Jdn
import com.solinone.core.calendar.PersianDate
import com.solinone.core.calendar.toPersianDigits
import com.solinone.core.database.entity.CalendarEventEntity

/**
 * 12-month Overview Grid (نمای سالانه)
 * Ported from upstream Persian Calendar yearview architecture.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearView(
    year: Int,
    onYearChange: (Int) -> Unit,
    onSelectDay: (PersianDate) -> Unit,
    allYearEvents: List<CalendarEventEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Year Navigation Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onYearChange(year + 1) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "سال بعد")
                }
                Text(
                    text = "سال $year".toPersianDigits(),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = { onYearChange(year - 1) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "سال قبل")
                }
            }
        }

        // 12 Months Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(PersianDate.MONTH_NAMES) { index, monthName ->
                val monthNumber = index + 1
                MiniMonthCard(
                    year = year,
                    month = monthNumber,
                    monthName = monthName,
                    events = allYearEvents.filter { it.persianMonth == monthNumber },
                    onSelectDay = onSelectDay
                )
            }
        }
    }
}

@Composable
private fun MiniMonthCard(
    year: Int,
    month: Int,
    monthName: String,
    events: List<CalendarEventEntity>,
    onSelectDay: (PersianDate) -> Unit
) {
    val daysInMonth = when {
        month in 1..6 -> 31
        month in 7..11 -> 30
        else -> {
            val jdnEnd = Jdn.fromPersian(year, 12, 30)
            if (jdnEnd.toPersian().first == year) 30 else 29
        }
    }
    val firstDayOfWeek = Jdn.fromPersian(year, month, 1).getDayOfWeek()
    val today = PersianDate.today()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectDay(PersianDate(year, month, 1)) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = monthName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Mini weekday headers
            Row(modifier = Modifier.fillMaxWidth()) {
                val shortHeaders = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
                shortHeaders.forEach { h ->
                    Text(
                        text = h,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (h == "ج") Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Mini days grid
            val totalCells = firstDayOfWeek + daysInMonth
            val totalRows = (totalCells + 6) / 7

            for (row in 0 until totalRows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - firstDayOfWeek + 1
                        if (dayNumber in 1..daysInMonth) {
                            val isHoliday = col == 6 || events.any { it.persianDay == dayNumber && it.isHoliday }
                            val isToday = today.year == year && today.month == month && today.day == dayNumber

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .background(if (isToday) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                    .clickable { onSelectDay(PersianDate(year, month, dayNumber)) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayNumber.toString().toPersianDigits(),
                                    fontSize = 10.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isHoliday -> Color(0xFFE53935)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

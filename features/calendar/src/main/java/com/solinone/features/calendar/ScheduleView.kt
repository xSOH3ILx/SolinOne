package com.solinone.features.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solinone.core.calendar.Jdn
import com.solinone.core.calendar.PersianDate
import com.solinone.core.calendar.toPersianDigits
import com.solinone.core.database.entity.CalendarEventEntity

/**
 * Schedule/Agenda Screen (برنامه زمانی و تقویم پیوسته)
 * Ported from upstream Persian Calendar ScheduleScreen architecture.
 */
@Composable
fun ScheduleView(
    baseDate: PersianDate,
    onSelectDay: (PersianDate) -> Unit,
    events: List<CalendarEventEntity>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val today = remember { PersianDate.today() }

    // Generate a range of +/- 45 days around the base date for smooth continuous schedule scanning
    val daysList = remember(baseDate) {
        val baseJdn = baseDate.toJdn()
        (-30..60).map { offset ->
            val curJdn = baseJdn + offset.toLong()
            val (y, m, d) = curJdn.toPersian()
            PersianDate(y, m, d)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(daysList, key = { "${it.year}-${it.month}-${it.day}" }) { date ->
            val isToday = date.year == today.year && date.month == today.month && date.day == today.day
            val dayEvents = events.filter { it.persianMonth == date.month && it.persianDay == date.day }
            val isFriday = date.toJdn().getDayOfWeek() == 6
            val isHoliday = isFriday || dayEvents.any { it.isHoliday }

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectDay(date) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = when {
                        isToday -> MaterialTheme.colorScheme.primaryContainer
                        isHoliday -> Color(0xFFFFF1F0)
                        else -> MaterialTheme.colorScheme.surface
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isToday) MaterialTheme.colorScheme.primary
                                        else if (isHoliday) Color(0xFFFFCDD2)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = date.day.toString().toPersianDigits(),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isToday) Color.White else if (isHoliday) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column {
                                Text(
                                    text = "${date.dayOfWeekName}، ${date.day} ${date.monthName} ${date.year}".toPersianDigits(),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isHoliday) Color(0xFFB71C1C) else MaterialTheme.colorScheme.onSurface
                                )
                                val (gy, gm, gd) = date.toJdn().toGregorian()
                                Text(
                                    text = "$gy/${gm.toString().padStart(2, '0')}/${gd.toString().padStart(2, '0')}".toPersianDigits(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (isHoliday) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFFCDD2)
                            ) {
                                Text(
                                    text = "تعطیل",
                                    color = Color(0xFFB71C1C),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Events preview for this day
                    if (dayEvents.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        dayEvents.forEach { ev ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    if (ev.isHoliday) Icons.Default.Star else Icons.Default.EventNote,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (ev.isHoliday) Color(0xFFC62828) else MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = ev.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (ev.isHoliday) Color(0xFFB71C1C) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

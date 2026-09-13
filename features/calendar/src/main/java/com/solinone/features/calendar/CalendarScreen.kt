package com.solinone.features.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solinone.core.calendar.PersianDate
import com.solinone.core.calendar.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
    var selectedDate by remember { mutableStateOf(PersianDate.today()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تقویم هوشمند شمسی") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* Previous month */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "ماه قبل")
                    }
                    Text(
                        text = "${selectedDate.monthName} ${selectedDate.year}".toPersianDigits(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = { /* Next month */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ماه بعد")
                    }
                }
            }

            // Weekday Headers
            Row(modifier = Modifier.fillMaxWidth()) {
                val weekDays = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
                weekDays.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (day == "ج") Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Month Days Grid (30/31 days)
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(31) { index ->
                    val dayNum = index + 1
                    val isToday = dayNum == selectedDate.day
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayNum.toString().toPersianDigits(),
                            color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Divider()

            // Selected Day Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "اطلاعات روز انتخاب شده",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${selectedDate.dayOfWeekName}، ${selectedDate.day} ${selectedDate.monthName} ${selectedDate.year}".toPersianDigits(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "رویداد رسمی: رویداد خاصی ثبت نشده است.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

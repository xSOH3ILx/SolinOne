package com.solinone.features.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solinone.core.calendar.Jdn
import com.solinone.core.calendar.PersianDate
import com.solinone.core.calendar.toPersianDigits
import com.solinone.core.database.SolinOneDatabase
import com.solinone.core.database.entity.CalendarEventEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { SolinOneDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    var selectedDate by remember { mutableStateOf(PersianDate.today()) }
    var currentYear by remember { mutableIntStateOf(selectedDate.year) }
    var currentMonth by remember { mutableIntStateOf(selectedDate.month) }

    // Observe events for selected month from Room
    val monthEvents by db.calendarEventDao().getEventsForMonth(currentYear, currentMonth).collectAsState(initial = emptyList())
    // Observe events for the selected day
    val dayEvents by db.calendarEventDao().getEventsForDay(selectedDate.year, selectedDate.month, selectedDate.day).collectAsState(initial = emptyList())

    var showAddEventDialog by remember { mutableStateOf(false) }
    var newEventTitle by remember { mutableStateOf("") }
    var newEventDesc by remember { mutableStateOf("") }

    val daysInMonth = when {
        currentMonth in 1..6 -> 31
        currentMonth in 7..11 -> 30
        else -> {
            // Month 12: 30 days if leap, else 29
            val jdnEnd = Jdn.fromPersian(currentYear, 12, 30)
            if (jdnEnd.toPersian().first == currentYear) 30 else 29
        }
    }

    // First day of this month in Persian week: Saturday=0 .. Friday=6
    val firstDayOfWeek = Jdn.fromPersian(currentYear, currentMonth, 1).getDayOfWeek()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تقویم هوشمند SolinOne") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    newEventTitle = ""
                    newEventDesc = ""
                    showAddEventDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "افزودن رویداد", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month Header with Navigation
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (currentMonth == 12) {
                                currentMonth = 1
                                currentYear++
                            } else {
                                currentMonth++
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "ماه بعد")
                        }
                        Text(
                            text = "${PersianDate.MONTH_NAMES.getOrElse(currentMonth - 1) { "" }} $currentYear".toPersianDigits(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(onClick = {
                            if (currentMonth == 1) {
                                currentMonth = 12
                                currentYear--
                            } else {
                                currentMonth--
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ماه قبل")
                        }
                    }
                }
            }

            // Weekday Headers (Saturday to Friday in Persian order)
            item {
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
            }

            // Month Grid
            item {
                val totalCells = firstDayOfWeek + daysInMonth
                val totalRows = (totalCells + 6) / 7

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (row in 0 until totalRows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (col in 0 until 7) {
                                val cellIndex = row * 7 + col
                                val dayNumber = cellIndex - firstDayOfWeek + 1
                                if (dayNumber in 1..daysInMonth) {
                                    val isSelected = selectedDate.year == currentYear &&
                                            selectedDate.month == currentMonth &&
                                            selectedDate.day == dayNumber
                                    val isToday = PersianDate.today().let {
                                        it.year == currentYear && it.month == currentMonth && it.day == dayNumber
                                    }
                                    val hasEvent = monthEvents.any { it.persianDay == dayNumber }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isSelected -> MaterialTheme.colorScheme.primary
                                                    isToday -> MaterialTheme.colorScheme.primaryContainer
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .clickable {
                                                selectedDate = PersianDate(currentYear, currentMonth, dayNumber)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = dayNumber.toString().toPersianDigits(),
                                                color = when {
                                                    isSelected -> Color.White
                                                    col == 6 -> Color.Red // Friday
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                },
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            if (hasEvent) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) Color.White else MaterialTheme.colorScheme.secondary)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            item {
                HorizontalDivider()
            }

            // Selected Day Details Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                    }
                }
            }

            // Events for this Selected Day
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "رویدادها و یادداشت‌های این روز (${dayEvents.size})".toPersianDigits(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (dayEvents.isEmpty()) {
                item {
                    Text(
                        text = "هیچ رویداد یا یادداشتی برای این تاریخ ثبت نشده است. با زدن دکمه + می‌توانید رویداد جدید اضافه کنید.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(dayEvents, key = { it.id }) { event ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.EventNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(text = event.title, style = MaterialTheme.typography.titleSmall)
                                    if (event.description.isNotEmpty()) {
                                        Text(
                                            text = event.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            IconButton(onClick = {
                                scope.launch(Dispatchers.IO) {
                                    db.calendarEventDao().deleteEventById(event.id)
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Event Dialog
    if (showAddEventDialog) {
        AlertDialog(
            onDismissRequest = { showAddEventDialog = false },
            title = { Text("ثبت رویداد یا یادداشت جدید") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "تاریخ: ${selectedDate.day} ${selectedDate.monthName} ${selectedDate.year}".toPersianDigits(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = newEventTitle,
                        onValueChange = { newEventTitle = it },
                        label = { Text("عنوان رویداد") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newEventDesc,
                        onValueChange = { newEventDesc = it },
                        label = { Text("توضیحات (اختیاری)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newEventTitle.isNotBlank()) {
                            scope.launch(Dispatchers.IO) {
                                db.calendarEventDao().insertEvent(
                                    CalendarEventEntity(
                                        title = newEventTitle.trim(),
                                        description = newEventDesc.trim(),
                                        persianYear = selectedDate.year,
                                        persianMonth = selectedDate.month,
                                        persianDay = selectedDate.day
                                    )
                                )
                            }
                            showAddEventDialog = false
                        }
                    }
                ) {
                    Text("ذخیره")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEventDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

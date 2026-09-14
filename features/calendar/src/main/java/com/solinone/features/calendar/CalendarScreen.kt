package com.solinone.features.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solinone.core.calendar.Jdn
import com.solinone.core.calendar.PersianDate
import com.solinone.core.calendar.toPersianDigits
import com.solinone.core.database.CalendarEventsInitializer
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

    // Populate official Iran events from assets into Room on first launch
    LaunchedEffect(Unit) {
        CalendarEventsInitializer.populateEventsIfNeeded(context, db)
    }

    var selectedDate by remember { mutableStateOf(PersianDate.today()) }
    var currentYear by remember { mutableIntStateOf(selectedDate.year) }
    var currentMonth by remember { mutableIntStateOf(selectedDate.month) }
    var isSlidingForward by remember { mutableStateOf(true) }

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
            val jdnEnd = Jdn.fromPersian(currentYear, 12, 30)
            if (jdnEnd.toPersian().first == currentYear) 30 else 29
        }
    }

    val firstDayOfWeek = Jdn.fromPersian(currentYear, currentMonth, 1).getDayOfWeek()

    fun navigateToNextMonth() {
        isSlidingForward = true
        if (currentMonth == 12) {
            currentMonth = 1
            currentYear++
        } else {
            currentMonth++
        }
    }

    fun navigateToPreviousMonth() {
        isSlidingForward = false
        if (currentMonth == 1) {
            currentMonth = 12
            currentYear--
        } else {
            currentMonth--
        }
    }

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
                actions = {
                    TextButton(onClick = {
                        val today = PersianDate.today()
                        selectedDate = today
                        currentYear = today.year
                        currentMonth = today.month
                    }) {
                        Text("امروز", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
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
            // Month Header with Navigation Buttons
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
                        IconButton(onClick = { navigateToNextMonth() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "ماه بعد")
                        }
                        Text(
                            text = "${PersianDate.MONTH_NAMES.getOrElse(currentMonth - 1) { "" }} $currentYear".toPersianDigits(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(onClick = { navigateToPreviousMonth() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ماه قبل")
                        }
                    }
                }
            }

            // Weekday Headers (ش تا ج در ترتیب تقویم فارسی)
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    val weekDays = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
                    weekDays.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (day == "ج") Color(0xFFE53935) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Month Days Grid with Animated Swipe Gesture (RTL Native Direction)
            item {
                var totalDrag by remember { mutableFloatStateOf(0f) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(currentYear, currentMonth) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    // In RTL: Drag to right (totalDrag > 60) moves forward to next month
                                    // Drag to left (totalDrag < -60) moves backward to previous month
                                    if (totalDrag > 60) {
                                        navigateToNextMonth()
                                    } else if (totalDrag < -60) {
                                        navigateToPreviousMonth()
                                    }
                                    totalDrag = 0f
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    totalDrag += dragAmount
                                }
                            )
                        }
                ) {
                    AnimatedContent(
                        targetState = Pair(currentYear, currentMonth),
                        transitionSpec = {
                            if (isSlidingForward) {
                                slideInHorizontally { width -> width } togetherWith slideOutHorizontally { width -> -width }
                            } else {
                                slideInHorizontally { width -> -width } togetherWith slideOutHorizontally { width -> width }
                            }
                        },
                        label = "MonthGridAnimation"
                    ) { (year, month) ->
                        val currentDaysInMonth = when {
                            month in 1..6 -> 31
                            month in 7..11 -> 30
                            else -> {
                                val jdnEnd = Jdn.fromPersian(year, 12, 30)
                                if (jdnEnd.toPersian().first == year) 30 else 29
                            }
                        }
                        val currentFirstDay = Jdn.fromPersian(year, month, 1).getDayOfWeek()
                        val totalCells = currentFirstDay + currentDaysInMonth
                        val totalRows = (totalCells + 6) / 7

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (row in 0 until totalRows) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    for (col in 0 until 7) {
                                        val cellIndex = row * 7 + col
                                        val dayNumber = cellIndex - currentFirstDay + 1
                                        if (dayNumber in 1..currentDaysInMonth) {
                                            val isSelected = selectedDate.year == year &&
                                                    selectedDate.month == month &&
                                                    selectedDate.day == dayNumber
                                            val isToday = PersianDate.today().let {
                                                it.year == year && it.month == month && it.day == dayNumber
                                            }
                                            val eventsForCell = monthEvents.filter { it.persianDay == dayNumber }
                                            val isHoliday = col == 6 || eventsForCell.any { it.isHoliday }
                                            val hasCustomEvent = eventsForCell.any { !it.isHoliday && it.persianYear != 0 }
                                            val hasOfficialEvent = eventsForCell.any { it.persianYear == 0 }

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
                                                        selectedDate = PersianDate(year, month, dayNumber)
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
                                                            isHoliday -> Color(0xFFE53935)
                                                            else -> MaterialTheme.colorScheme.onSurface
                                                        },
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                    if (eventsForCell.isNotEmpty()) {
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            if (isHoliday) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(4.dp)
                                                                        .clip(CircleShape)
                                                                        .background(if (isSelected) Color.White else Color(0xFFE53935))
                                                                )
                                                            }
                                                            if (hasCustomEvent || hasOfficialEvent) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(4.dp)
                                                                        .clip(CircleShape)
                                                                        .background(if (isSelected) Color.White else MaterialTheme.colorScheme.secondary)
                                                                )
                                                            }
                                                        }
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
                }
            }

            item {
                HorizontalDivider()
            }

            // Selected Day Details Card
            item {
                val jdnSelected = selectedDate.toJdn()
                val (gy, gm, gd) = jdnSelected.toGregorian()
                val isHolidayDay = selectedDate.toJdn().getDayOfWeek() == 6 || dayEvents.any { it.isHoliday }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isHolidayDay) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "اطلاعات روز انتخاب شده",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isHolidayDay) Color(0xFFC62828) else MaterialTheme.colorScheme.primary
                            )
                            if (isHolidayDay) {
                                Surface(
                                    color = Color(0xFFFFCDD2),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "تعطیل رسمی",
                                        color = Color(0xFFB71C1C),
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${selectedDate.dayOfWeekName}، ${selectedDate.day} ${selectedDate.monthName} ${selectedDate.year}".toPersianDigits(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (isHolidayDay) Color(0xFFB71C1C) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "معادل میلادی: $gy/${gm.toString().padStart(2, '0')}/${gd.toString().padStart(2, '0')}".toPersianDigits(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Events for this Selected Day (Official & Custom)
            item {
                Text(
                    text = "مناسبت‌ها و رویدادهای این روز (${dayEvents.size})".toPersianDigits(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (dayEvents.isEmpty()) {
                item {
                    Text(
                        text = "هیچ مناسبت رسمی یا یادداشت شخصی برای این روز ثبت نشده است. با زدن دکمه + می‌توانید رویداد جدید اضافه کنید.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(dayEvents, key = { it.id }) { event ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (event.isHoliday) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surface
                        )
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
                                    if (event.isHoliday) Icons.Default.Star else Icons.Default.EventNote,
                                    contentDescription = null,
                                    tint = if (event.isHoliday) Color(0xFFC62828) else MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = event.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (event.isHoliday) Color(0xFFB71C1C) else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (event.isHoliday) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (event.description.isNotEmpty()) {
                                        Text(
                                            text = event.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            if (event.persianYear != 0) { // Only allow deleting user-created custom events
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

package com.solinone.features.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solinone.core.calendar.IslamicDate
import com.solinone.core.calendar.Jdn
import com.solinone.core.calendar.PersianDate
import com.solinone.core.calendar.toPersianDigits
import com.solinone.core.database.CalendarEventsInitializer
import com.solinone.core.database.SolinOneDatabase
import com.solinone.core.database.entity.CalendarEventEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class CalendarViewMode {
    MONTH,
    YEAR,
    SCHEDULE
}

enum class CalendarDetailTab {
    EVENTS,
    CALENDARS_INFO
}

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

    var viewMode by remember { mutableStateOf(CalendarViewMode.MONTH) }
    var selectedDetailTab by remember { mutableStateOf(CalendarDetailTab.EVENTS) }

    // Dialog states
    var showAddEventDialog by remember { mutableStateOf(false) }
    var showConverterDialog by remember { mutableStateOf(false) }
    var newEventTitle by remember { mutableStateOf("") }
    var newEventDesc by remember { mutableStateOf("") }

    // Observe events for selected month from Room
    val monthEvents by db.calendarEventDao().getEventsForMonth(currentYear, currentMonth).collectAsState(initial = emptyList())
    // Observe events for the selected day
    val dayEvents by db.calendarEventDao().getEventsForDay(selectedDate.year, selectedDate.month, selectedDate.day).collectAsState(initial = emptyList())
    // Observe all events for year/schedule views
    val allYearEvents by db.calendarEventDao().getAllEvents().collectAsState(initial = emptyList())

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
            currentYear--
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
                    // Converter Icon
                    IconButton(onClick = { showConverterDialog = true }) {
                        Icon(Icons.Default.Calculate, contentDescription = "تبدیل تاریخ", tint = MaterialTheme.colorScheme.primary)
                    }
                    // Today jump button
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
            if (viewMode == CalendarViewMode.MONTH) {
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // View Mode Selector Segmented Bar (ماهانه، سالانه، زمان‌بندی)
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SegmentedButton(
                    selected = viewMode == CalendarViewMode.MONTH,
                    onClick = { viewMode = CalendarViewMode.MONTH },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                ) {
                    Text("ماهانه")
                }
                SegmentedButton(
                    selected = viewMode == CalendarViewMode.YEAR,
                    onClick = { viewMode = CalendarViewMode.YEAR },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                ) {
                    Text("سالانه")
                }
                SegmentedButton(
                    selected = viewMode == CalendarViewMode.SCHEDULE,
                    onClick = { viewMode = CalendarViewMode.SCHEDULE },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                ) {
                    Text("برنامه زمانی")
                }
            }

            when (viewMode) {
                CalendarViewMode.YEAR -> {
                    YearView(
                        year = currentYear,
                        onYearChange = { currentYear = it },
                        allYearEvents = allYearEvents,
                        onSelectDay = { date ->
                            selectedDate = date
                            currentYear = date.year
                            currentMonth = date.month
                            viewMode = CalendarViewMode.MONTH
                        }
                    )
                }
                CalendarViewMode.SCHEDULE -> {
                    ScheduleView(
                        baseDate = selectedDate,
                        events = allYearEvents,
                        onSelectDay = { date ->
                            selectedDate = date
                            currentYear = date.year
                            currentMonth = date.month
                            viewMode = CalendarViewMode.MONTH
                        }
                    )
                }
                CalendarViewMode.MONTH -> {
                    // Month View with Upstream Swipe Engine (Horizontal for months, Vertical for YearView/Schedule)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .detectHorizontalSwipe(key1 = "$currentYear-$currentMonth") {
                                { isLeft: Boolean ->
                                    if (isLeft) navigateToNextMonth() else navigateToPreviousMonth()
                                }
                            }
                            .detectSwipe {
                                { isUp: Boolean ->
                                    if (isUp) {
                                        viewMode = CalendarViewMode.SCHEDULE
                                    } else {
                                        viewMode = CalendarViewMode.YEAR
                                    }
                                }
                            },
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Month Header Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { navigateToNextMonth() }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "ماه بعد")
                                    }
                                    Text(
                                        text = "${PersianDate.MONTH_NAMES.getOrElse(currentMonth - 1) { "" }} $currentYear".toPersianDigits(),
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
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

                        // Month Grid with Animated Slide
                        item {
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

                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }

                        // Detail Tabs Switcher (مناسبت‌ها و رویدادها | اطلاعات تقویم‌ها)
                        item {
                            PrimaryTabRow(
                                selectedTabIndex = selectedDetailTab.ordinal,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Tab(
                                    selected = selectedDetailTab == CalendarDetailTab.EVENTS,
                                    onClick = { selectedDetailTab = CalendarDetailTab.EVENTS },
                                    text = { Text("مناسبت‌ها (${dayEvents.size})".toPersianDigits()) }
                                )
                                Tab(
                                    selected = selectedDetailTab == CalendarDetailTab.CALENDARS_INFO,
                                    onClick = { selectedDetailTab = CalendarDetailTab.CALENDARS_INFO },
                                    text = { Text("تقویم‌های همزمان") }
                                )
                            }
                        }

                        // Tab Content
                        if (selectedDetailTab == CalendarDetailTab.EVENTS) {
                            // Selected Day Header
                            item {
                                val isHolidayDay = selectedDate.toJdn().getDayOfWeek() == 6 || dayEvents.any { it.isHoliday }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isHolidayDay) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant
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
                                            Text(
                                                text = "${selectedDate.dayOfWeekName}، ${selectedDate.day} ${selectedDate.monthName} ${selectedDate.year}".toPersianDigits(),
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (isHolidayDay) Color(0xFFB71C1C) else MaterialTheme.colorScheme.onSurfaceVariant
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
                                    }
                                }
                            }

                            if (dayEvents.isEmpty()) {
                                item {
                                    Text(
                                        text = "هیچ مناسبت رسمی یا یادداشت شخصی برای این روز ثبت نشده است. با زدن دکمه + می‌توانید رویداد جدید اضافه کنید.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 8.dp)
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
                        } else {
                            // Calendars Overview Tab (خورشیدی، میلادی، قمری)
                            item {
                                val jdnSelected = selectedDate.toJdn()
                                val (gy, gm, gd) = jdnSelected.toGregorian()
                                val islamic = IslamicDate.fromJdn(jdnSelected)

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = "گاه‌شماری‌های سه‌گانه و تطبیق روز",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        ListItem(
                                            headlineContent = { Text("تقویم هجری خورشیدی (جلالی)") },
                                            supportingContent = { Text("${selectedDate.day} ${selectedDate.monthName} ${selectedDate.year}".toPersianDigits()) },
                                            leadingContent = { Icon(Icons.Default.WbSunny, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                        )

                                        ListItem(
                                            headlineContent = { Text("تقویم میلادی (Gregorian)") },
                                            supportingContent = { Text("$gy/${gm.toString().padStart(2, '0')}/${gd.toString().padStart(2, '0')}".toPersianDigits()) },
                                            leadingContent = { Icon(Icons.Default.Public, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) }
                                        )

                                        ListItem(
                                            headlineContent = { Text("تقویم هجری قمری (Islamic Hijri)") },
                                            supportingContent = { Text("${islamic.day} ${islamic.monthName} ${islamic.year}".toPersianDigits()) },
                                            leadingContent = { Icon(Icons.Default.NightlightRound, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) }
                                        )

                                        HorizontalDivider()

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "روز هفته: ${selectedDate.dayOfWeekName}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            val dayOfYear = when {
                                                selectedDate.month <= 6 -> (selectedDate.month - 1) * 31 + selectedDate.day
                                                else -> 186 + (selectedDate.month - 7) * 30 + selectedDate.day
                                            }
                                            Text(
                                                text = "روز شماره $dayOfYear از سال".toPersianDigits(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(30.dp))
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

    // Date Converter Dialog
    if (showConverterDialog) {
        DateConverterDialog(
            initialDate = selectedDate,
            onDismissRequest = { showConverterDialog = false },
            onSelectDate = { date ->
                selectedDate = date
                currentYear = date.year
                currentMonth = date.month
                viewMode = CalendarViewMode.MONTH
            }
        )
    }
}

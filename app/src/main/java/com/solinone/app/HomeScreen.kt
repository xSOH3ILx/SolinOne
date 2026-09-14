package com.solinone.app

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
import androidx.compose.ui.unit.dp
import com.solinone.core.calendar.PersianDate
import com.solinone.core.calendar.toPersianDigits
import com.solinone.core.calendar.toPersianTomanFormatted
import com.solinone.core.database.SolinOneDatabase
import com.solinone.core.database.entity.ObligationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToFinance: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val today = PersianDate.today()
    val context = LocalContext.current
    val db = remember { SolinOneDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    val totalIncome by db.transactionDao().getTotalIncome().collectAsState(initial = 0L)
    val totalExpense by db.transactionDao().getTotalExpense().collectAsState(initial = 0L)
    val pendingObligations by db.obligationDao().getPendingObligations().collectAsState(initial = emptyList())
    val pendingTransactions by db.transactionDao().getPendingReviewTransactions().collectAsState(initial = emptyList())
    val todayEvents by db.calendarEventDao().getEventsForDay(today.year, today.month, today.day).collectAsState(initial = emptyList())

    val currentBalance = (totalIncome ?: 0L) - (totalExpense ?: 0L)

    var showAddObligationDialog by remember { mutableStateOf(false) }
    var newObligationTitle by remember { mutableStateOf("") }
    var newObligationAmount by remember { mutableStateOf("") }
    var newObligationDate by remember { mutableStateOf(today.formatPersian(false)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SolinOne",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "تنظیمات",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // "My Today" Card (امروز من)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "امروز من",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "${today.dayOfWeekName}، ${today.day} ${today.monthName} ${today.year}".toPersianDigits(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        val statusText = when {
                            todayEvents.isNotEmpty() && pendingTransactions.isNotEmpty() ->
                                "${todayEvents.size} رویداد تقویم و ${pendingTransactions.size} پیامک بانکی برای بررسی دارید.".toPersianDigits()
                            todayEvents.isNotEmpty() ->
                                "${todayEvents.size} رویداد یا یادداشت برای امروز ثبت شده است: ${todayEvents.first().title}".toPersianDigits()
                            pendingTransactions.isNotEmpty() ->
                                "${pendingTransactions.size} تراکنش جدید بانکی منتظر تأیید شماست.".toPersianDigits()
                            pendingObligations.isNotEmpty() ->
                                "${pendingObligations.size} تعهد مالی سررسید نشده دارید.".toPersianDigits()
                            else ->
                                "امروز هیچ یادآوری، رویداد یا سررسید مالی ثبت‌نشده‌ای ندارید."
                        }

                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Main Feature Cards (انتخاب ورود به بخش‌ها)
            item {
                Text(
                    text = "بخش‌های برنامه",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Finance Entry Card
            item {
                ElevatedCard(
                    onClick = onNavigateToFinance,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "مدیریت مالی",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = "کل موجودی: ${currentBalance.toPersianTomanFormatted()}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (pendingTransactions.isNotEmpty()) {
                                    Text(
                                        text = "⚡ ${pendingTransactions.size} پیامک بانکی جدید".toPersianDigits(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ورود",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Calendar Entry Card
            item {
                ElevatedCard(
                    onClick = onNavigateToCalendar,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "تقویم هوشمند شمسی",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = "مشاهده روزها، رویدادها و یادداشت‌ها",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (todayEvents.isNotEmpty()) {
                                    Text(
                                        text = "📌 ${todayEvents.size} رویداد ثبت‌شده برای امروز".toPersianDigits(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ورود",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Financial Obligations Section (تعهدات مالی و اقساط)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "اقساط و موعدهای مالی (${pendingObligations.size})".toPersianDigits(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = { showAddObligationDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("افزودن قسط")
                    }
                }
            }

            if (pendingObligations.isEmpty()) {
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(text = "قسط یا تعهد فعالی ثبت نشده است", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = "با زدن دکمه «افزودن قسط»، موعد چک یا وام‌های خود را ثبت کنید.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                items(pendingObligations, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(text = item.title, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = "موعد: ${item.dueDatePersian.toPersianDigits()} | مبلغ: ${item.amountRial.toPersianTomanFormatted()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = {
                                scope.launch(Dispatchers.IO) {
                                    db.obligationDao().deleteObligation(item)
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Add Obligation Dialog
    if (showAddObligationDialog) {
        AlertDialog(
            onDismissRequest = { showAddObligationDialog = false },
            title = { Text("افزودن قسط یا تعهد مالی جدید") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newObligationTitle,
                        onValueChange = { newObligationTitle = it },
                        label = { Text("عنوان قسط یا تعهد (مثلاً وام مسکن)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newObligationAmount,
                        onValueChange = { newObligationAmount = it },
                        label = { Text("مبلغ به تومان") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newObligationDate,
                        onValueChange = { newObligationDate = it },
                        label = { Text("تاریخ سررسید (مثلاً 1403/07/01)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountToman = newObligationAmount.toLongOrNull() ?: 0L
                        if (newObligationTitle.isNotBlank() && amountToman > 0) {
                            scope.launch(Dispatchers.IO) {
                                db.obligationDao().insertObligation(
                                    ObligationEntity(
                                        title = newObligationTitle.trim(),
                                        amountRial = amountToman * 10,
                                        dueDatePersian = newObligationDate.trim(),
                                        isPaid = false
                                    )
                                )
                            }
                            showAddObligationDialog = false
                            newObligationTitle = ""
                            newObligationAmount = ""
                        }
                    }
                ) {
                    Text("ذخیره")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddObligationDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

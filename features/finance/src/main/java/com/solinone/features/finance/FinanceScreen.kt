package com.solinone.features.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.solinone.core.calendar.toPersianTomanFormatted
import com.solinone.core.database.SolinOneDatabase
import com.solinone.core.database.entity.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    onNavigateBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val db = remember { SolinOneDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    val confirmedTransactions by db.transactionDao().getAllConfirmedTransactions().collectAsState(initial = emptyList())
    val pendingTransactions by db.transactionDao().getPendingReviewTransactions().collectAsState(initial = emptyList())
    val totalIncome by db.transactionDao().getTotalIncome().collectAsState(initial = 0L)
    val totalExpense by db.transactionDao().getTotalExpense().collectAsState(initial = 0L)

    val currentBalance = (totalIncome ?: 0L) - (totalExpense ?: 0L)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مدیریت مالی آرا (Ara)") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Seed sample test manual transaction
                    scope.launch(Dispatchers.IO) {
                        db.transactionDao().insertTransaction(
                            TransactionEntity(
                                amountRial = 50000000L,
                                type = "INCOME",
                                category = "واریز حقوق",
                                note = "ثبت دستی",
                                timestamp = System.currentTimeMillis(),
                                persianDate = "1403/06/23",
                                isPendingReview = false
                            )
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "تراکنش جدید", tint = Color.White)
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
            // Balance Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "کل موجودی محاسبه‌شده",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = currentBalance.toPersianTomanFormatted(),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color(0xFF4CAF50))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "درآمد: " + (totalIncome ?: 0L).toPersianTomanFormatted(),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFFE53935))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "هزینه: " + (totalExpense ?: 0L).toPersianTomanFormatted(),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            // Pending Review Queue
            if (pendingTransactions.isNotEmpty()) {
                item {
                    Text(
                        text = "صف بازبینی پیامک‌های بانکی (${pendingTransactions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(pendingTransactions, key = { it.id }) { item ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (item.sender.isNotEmpty()) item.sender else "بانک",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Surface(
                                    color = Color(0xFFE8F5E9),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (item.type == "INCOME") "واریز" else "برداشت",
                                        color = if (item.type == "INCOME") Color(0xFF2E7D32) else Color(0xFFC62828),
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Text(
                                text = "مبلغ: " + item.amountRial.toPersianTomanFormatted(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        db.transactionDao().deleteTransaction(item)
                                    }
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("رد پیام")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        db.transactionDao().confirmTransaction(item.id)
                                    }
                                }) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تأیید و افزودن")
                                }
                            }
                        }
                    }
                }
            }

            // Confirmed Transactions History
            item {
                Text(
                    text = "آخرین تراکنش‌های ثبت‌شده",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (confirmedTransactions.isEmpty()) {
                item {
                    Text(
                        text = "هنوز هیچ تراکنشی ثبت نشده است. پیامک‌های بانکی به محض دریافت در صف بازبینی ظاهر می‌شوند.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(confirmedTransactions, key = { it.id }) { tx ->
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = tx.category, style = MaterialTheme.typography.titleSmall)
                                Text(text = tx.persianDate, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = (if (tx.type == "INCOME") "+ " else "- ") + tx.amountRial.toPersianTomanFormatted(),
                                style = MaterialTheme.typography.titleMedium,
                                color = if (tx.type == "INCOME") Color(0xFF4CAF50) else Color(0xFFE53935)
                            )
                        }
                    }
                }
            }
        }
    }
}

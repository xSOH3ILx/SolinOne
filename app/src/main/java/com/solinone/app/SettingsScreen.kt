package com.solinone.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.solinone.core.calendar.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {}
) {
    var biometricEnabled by remember { mutableStateOf(true) }
    var amoledTheme by remember { mutableStateOf(false) }
    var blurRecentApps by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات SolinOne") },
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
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Security Settings
            item {
                Text(
                    text = "امنیت و حریم خصوصی",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Fingerprint, contentDescription = null)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("قفل بیومتریک", style = MaterialTheme.typography.titleSmall)
                                    Text("ورود با اثر انگشت یا تشخیص چهره", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Switch(checked = biometricEnabled, onCheckedChange = { biometricEnabled = it })
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("مات کردن صفحه در برنامه‌های اخیر", style = MaterialTheme.typography.titleSmall)
                                    Text("جلوگیری از دیده شدن موجودی و تعهدات", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Switch(checked = blurRecentApps, onCheckedChange = { blurRecentApps = it })
                        }
                    }
                }
            }

            // Appearance Settings
            item {
                Text(
                    text = "ظاهر و شخصی‌سازی",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Palette, contentDescription = null)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("تم مشکی عمیق (Amoled)", style = MaterialTheme.typography.titleSmall)
                                    Text("صرفه‌جویی در مصرف باتری برای صفحات اولد", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Switch(checked = amoledTheme, onCheckedChange = { amoledTheme = it })
                        }
                    }
                }
            }

            // App Version Info
            item {
                Text(
                    text = "درباره برنامه",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text("نسخه SolinOne", style = MaterialTheme.typography.titleSmall)
                            Text("نسخه 0.1.0-alpha.5 (کد بیلد: 5)".toPersianDigits(), style = MaterialTheme.typography.bodySmall)
                            Text("تحت لایسنس عمومی GPL-3.0", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

package com.solinone.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.solinone.core.ui.SolinOneTheme
import com.solinone.features.calendar.CalendarScreen
import com.solinone.features.finance.FinanceScreen

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SolinOneTheme {
                MainApp()
            }
        }
    }
}

object Destinations {
    const val HOME = "home"
    const val FINANCE = "finance"
    const val CALENDAR = "calendar"
    const val SETTINGS = "settings"
}

@Composable
fun MainApp() {
    val navController = rememberNavController()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        NavHost(
            navController = navController,
            startDestination = Destinations.HOME,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Destinations.HOME) {
                HomeScreen(
                    onNavigateToFinance = { navController.navigate(Destinations.FINANCE) },
                    onNavigateToCalendar = { navController.navigate(Destinations.CALENDAR) },
                    onNavigateToSettings = { navController.navigate(Destinations.SETTINGS) }
                )
            }
            composable(Destinations.FINANCE) {
                FinanceScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Destinations.CALENDAR) {
                CalendarScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Destinations.SETTINGS) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

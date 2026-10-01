package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ads.AdBannerView
import com.example.ads.ConsentManager
import com.example.ui.screens.AlarmEditDialog
import com.example.ui.screens.AlarmsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MapScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.NearAlarmTheme
import com.example.ui.viewmodel.MainViewModel

enum class MainTab {
    MAP,
    ALARMS,
    HISTORY,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        val app = application as NearAlarmApplication
        MainViewModel.Factory(app.repository, app.geofenceManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Gather consent using Google UMP (EU/UK/US regulations) before ad loading
        ConsentManager.gatherConsent(this) {
            // Consent updated
        }

        // 2. Check if launched from a notification tap with OPEN_ALARM_ID
        val openAlarmId = intent.getLongExtra("OPEN_ALARM_ID", -1L)

        setContent {
            NearAlarmTheme {
                MainAppContent(
                    viewModel = viewModel,
                    initialAlarmId = openAlarmId
                )
            }
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: MainViewModel,
    initialAlarmId: Long = -1L
) {
    var currentTab by remember { mutableStateOf(MainTab.MAP) }

    val isAlarmDialogOpen by viewModel.isAlarmDialogOpen.collectAsStateWithLifecycle()
    val alarmBeingEdited by viewModel.alarmBeingEdited.collectAsStateWithLifecycle()
    val distanceUnit by viewModel.distanceUnit.collectAsStateWithLifecycle()

    // Handle deep-link from notification
    LaunchedEffect(initialAlarmId) {
        if (initialAlarmId != -1L) {
            currentTab = MainTab.MAP
        }
    }

    // BackHandler: navigate to Map tab before exiting if on a sub-screen
    BackHandler(enabled = currentTab != MainTab.MAP) {
        currentTab = MainTab.MAP
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // AdMob Banner safely placed above navigation bar with distinct layout boundary
                AdBannerView(modifier = Modifier.testTag("admob_banner"))

                NavigationBar {
                    NavigationBarItem(
                        selected = currentTab == MainTab.MAP,
                        onClick = { currentTab = MainTab.MAP },
                        icon = { Icon(Icons.Default.Map, contentDescription = stringResource(R.string.nav_map)) },
                        label = { Text(stringResource(R.string.nav_map)) },
                        modifier = Modifier.testTag("nav_tab_map")
                    )
                    NavigationBarItem(
                        selected = currentTab == MainTab.ALARMS,
                        onClick = { currentTab = MainTab.ALARMS },
                        icon = { Icon(Icons.Default.Alarm, contentDescription = stringResource(R.string.nav_alarms)) },
                        label = { Text(stringResource(R.string.nav_alarms)) },
                        modifier = Modifier.testTag("nav_tab_alarms")
                    )
                    NavigationBarItem(
                        selected = currentTab == MainTab.HISTORY,
                        onClick = { currentTab = MainTab.HISTORY },
                        icon = { Icon(Icons.Default.History, contentDescription = stringResource(R.string.nav_history)) },
                        label = { Text(stringResource(R.string.nav_history)) },
                        modifier = Modifier.testTag("nav_tab_history")
                    )
                    NavigationBarItem(
                        selected = currentTab == MainTab.SETTINGS,
                        onClick = { currentTab = MainTab.SETTINGS },
                        icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.nav_settings)) },
                        label = { Text(stringResource(R.string.nav_settings)) },
                        modifier = Modifier.testTag("nav_tab_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.MAP -> MapScreen(viewModel = viewModel)
                MainTab.ALARMS -> AlarmsScreen(viewModel = viewModel)
                MainTab.HISTORY -> HistoryScreen(viewModel = viewModel)
                MainTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }
    }

    // Modal Add / Edit Alarm Dialog
    if (isAlarmDialogOpen && alarmBeingEdited != null) {
        AlarmEditDialog(
            initialAlarm = alarmBeingEdited!!,
            distanceUnit = distanceUnit,
            onSave = { updatedAlarm -> viewModel.saveAlarm(updatedAlarm) },
            onDismiss = { viewModel.closeAlarmDialog() }
        )
    }
}

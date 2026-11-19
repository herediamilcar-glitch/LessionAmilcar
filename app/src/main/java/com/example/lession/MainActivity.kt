package com.example.lession

import android.Manifest
import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lession.ui.screens.ChartScreen
import com.example.lession.ui.screens.DashboardScreen
import com.example.lession.ui.screens.DeviceScanScreen
import com.example.lession.ui.screens.HistoryScreen
import com.example.lession.ui.screens.HomeScreen
import com.example.lession.ui.screens.SettingsScreen
import com.example.lession.ui.theme.LessionTheme
import com.example.lession.ui.viewmodels.BleViewModel
import com.example.lession.ui.viewmodels.HistoryViewModel
import com.example.lession.ui.viewmodels.SettingsViewModel
import com.example.lession.ui.viewmodels.WorkoutViewModel
import com.example.lession.ui.viewmodels.WorkoutViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LessionTheme {
                LessionApp()
            }
        }
    }
}

@Composable
fun LessionApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as Application

    val bleViewModel: BleViewModel = viewModel()
    val workoutViewModel: WorkoutViewModel = viewModel(
        factory = WorkoutViewModelFactory(
            application = app,
            bleManager = bleViewModel.bleManager,
        )
    )
    val historyViewModel: HistoryViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    val connectionState by bleViewModel.connectionState.collectAsState()
    val discoveredDevices by bleViewModel.discoveredDevices.collectAsState()

    val workoutStats by workoutViewModel.workoutStats.collectAsState()
    val chartPoints by workoutViewModel.chartPoints.collectAsState()
    val alertThreshold by workoutViewModel.alertThreshold.collectAsState()

    val sessions by historyViewModel.sessions.collectAsState()
    val autoReconnect by settingsViewModel.autoReconnectSetting.collectAsState()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("home") {
                HomeScreen(
                    onStartClick = { navController.navigate("dashboard") }
                )
            }

            composable("dashboard") {
                DashboardScreen(
                    connectionState = connectionState,
                    workoutStats = workoutStats,
                    onNavigate = { route -> navController.navigate(route) },
                    onStartRecording = { workoutViewModel.startRecording() },
                    onStopRecording = { workoutViewModel.stopRecording() },
                )
            }

            composable("device_scan") {
                DeviceScanScreen(
                    connectionState = connectionState,
                    discoveredDevices = discoveredDevices,
                    onStartScan = { bleViewModel.startScan() },
                    onStopScan = { bleViewModel.stopScan() },
                    onConnectDevice = { device -> bleViewModel.connectToDevice(device) },
                    onDisconnectDevice = { bleViewModel.disconnect() },
                    onBackClick = { navController.popBackStack() },
                )
            }

            composable("charts") {
                ChartScreen(
                    readings = chartPoints,
                    alertThreshold = alertThreshold,
                    onBackClick = { navController.popBackStack() },
                )
            }

            composable("history") {
                HistoryScreen(
                    sessions = sessions,
                    onDeleteSession = { session -> historyViewModel.deleteSession(session) },
                    onExportSession = { session -> historyViewModel.exportSession(session) },
                    onBackClick = { navController.popBackStack() },
                )
            }

            composable("settings") {
                SettingsScreen(
                    autoReconnect = autoReconnect,
                    onAutoReconnectChanged = { settingsViewModel.setAutoReconnect(it) },
                    onBackClick = { navController.popBackStack() },
                )
            }
        }
    }
}

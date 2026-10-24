package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.MarioCoinIcon
import com.example.ui.components.StarRushCompletionDialog
import com.example.ui.components.StarRushOverlay
import com.example.ui.components.SuperStarIcon
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.PowerUpShopScreen
import com.example.ui.screens.WorldMapScreen
import com.example.ui.theme.MarioRed
import com.example.ui.theme.StarGold
import com.example.ui.theme.StarStepTheme
import com.example.ui.viewmodel.StarStepViewModel

enum class NavigationScreen(val title: String) {
    DASHBOARD("Run"),
    POWER_UPS("Items"),
    WORLD_MAP("Worlds"),
    HISTORY("Stats")
}

class MainActivity : ComponentActivity() {

    private val viewModel: StarStepViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            StarStepTheme {
                val context = LocalContext.current
                var currentScreen by remember { mutableStateOf(NavigationScreen.DASHBOARD) }
                var isSoundMuted by remember { mutableStateOf(false) }

                // Check Activity Recognition permission on Android 10+
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        viewModel.sensorTracker.startListening()
                    }
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACTIVITY_RECOGNITION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                        }
                    }
                }

                // Collect state from ViewModel
                val coins by viewModel.coins.collectAsState()
                val isStarRushActive by viewModel.isStarRushActive.collectAsState()
                val starRushSecondsLeft by viewModel.starRushSecondsLeft.collectAsState()
                val starRushTargetSpm by viewModel.starRushTargetSpm.collectAsState()
                val currentSpm by viewModel.currentCadence.collectAsState()
                val starRushSteps by viewModel.starRushSteps.collectAsState()
                val starRushCoins by viewModel.starRushCoins.collectAsState()
                val completionNotice by viewModel.starRushCompletionNotice.collectAsState()

                BackHandler(enabled = currentScreen != NavigationScreen.DASHBOARD) {
                    currentScreen = NavigationScreen.DASHBOARD
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            TopAppBar(
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        SuperStarIcon(size = 28.dp, isPulsing = false)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "StarStep",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 20.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color.White
                                        )
                                    }
                                },
                                actions = {
                                    // Coin counter pill
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color.Black.copy(alpha = 0.3f),
                                        border = BorderStroke(1.dp, StarGold)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            MarioCoinIcon(size = 16.dp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "$coins",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = StarGold
                                            )
                                        }
                                    }

                                    // Sound Mute Toggle
                                    IconButton(
                                        onClick = {
                                            val isPlaying = viewModel.toggleSound()
                                            isSoundMuted = !isPlaying
                                        },
                                        modifier = Modifier.testTag("sound_toggle_button")
                                    ) {
                                        Icon(
                                            imageVector = if (isSoundMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                            contentDescription = if (isSoundMuted) "Unmute Audio" else "Mute Audio",
                                            tint = Color.White
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MarioRed,
                                    titleContentColor = Color.White,
                                    actionIconContentColor = Color.White
                                )
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                windowInsets = WindowInsets(0, 0, 0, 0),
                                containerColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.testTag("bottom_nav_bar")
                            ) {
                                NavigationBarItem(
                                    selected = currentScreen == NavigationScreen.DASHBOARD,
                                    onClick = { currentScreen = NavigationScreen.DASHBOARD },
                                    icon = { Icon(Icons.Default.DirectionsRun, contentDescription = "Run") },
                                    label = { Text("Run", fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MarioRed,
                                        indicatorColor = MarioRed.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_run")
                                )
                                NavigationBarItem(
                                    selected = currentScreen == NavigationScreen.POWER_UPS,
                                    onClick = { currentScreen = NavigationScreen.POWER_UPS },
                                    icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Items") },
                                    label = { Text("Items", fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = StarGold,
                                        indicatorColor = StarGold.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_items")
                                )
                                NavigationBarItem(
                                    selected = currentScreen == NavigationScreen.WORLD_MAP,
                                    onClick = { currentScreen = NavigationScreen.WORLD_MAP },
                                    icon = { Icon(Icons.Default.Map, contentDescription = "Worlds") },
                                    label = { Text("Worlds", fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MarioRed,
                                        indicatorColor = MarioRed.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_worlds")
                                )
                                NavigationBarItem(
                                    selected = currentScreen == NavigationScreen.HISTORY,
                                    onClick = { currentScreen = NavigationScreen.HISTORY },
                                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Stats") },
                                    label = { Text("Stats", fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MarioRed,
                                        indicatorColor = MarioRed.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_stats")
                                )
                            }
                        }
                    ) { innerPadding ->
                        val contentModifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)

                        when (currentScreen) {
                            NavigationScreen.DASHBOARD -> DashboardScreen(
                                viewModel = viewModel,
                                modifier = contentModifier
                            )
                            NavigationScreen.POWER_UPS -> PowerUpShopScreen(
                                viewModel = viewModel,
                                modifier = contentModifier
                            )
                            NavigationScreen.WORLD_MAP -> WorldMapScreen(
                                viewModel = viewModel,
                                modifier = contentModifier
                            )
                            NavigationScreen.HISTORY -> HistoryScreen(
                                viewModel = viewModel,
                                modifier = contentModifier
                            )
                        }
                    }

                    // ⭐ Star Rush High-Intensity Sprint Workout Fullscreen Overlay ⭐
                    // ⭐ Star Rush High-Intensity Sprint Workout Fullscreen Overlay ⭐
                    StarRushOverlay(
                        isActive = isStarRushActive,
                        secondsLeft = starRushSecondsLeft,
                        targetSpm = starRushTargetSpm,
                        currentSpm = currentSpm,
                        stepsInRush = starRushSteps,
                        coinsEarned = starRushCoins,
                        onCancel = {
                            viewModel.soundManager.stopStarRushTheme()
                        }
                    )

                    // Star Rush Finish Autonomous Notice (Non-blocking banner)
                    androidx.compose.animation.AnimatedVisibility(
                        visible = completionNotice != null,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 70.dp, start = 16.dp, end = 16.dp)
                    ) {
                        completionNotice?.let { res ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF0F172A),
                                border = BorderStroke(2.dp, StarGold),
                                shadowElevation = 8.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.dismissCompletionNotice() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SuperStarIcon(modifier = Modifier, size = 32.dp, isPulsing = false)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "STAR SPRINT COMPLETE!",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = StarGold
                                        )
                                        Text(
                                            text = "${res.stepsTaken} steps @ ${res.averageSpm} SPM (+${res.bonusCoins} coins). Cool down now!",
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

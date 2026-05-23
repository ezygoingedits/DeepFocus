package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun TimerScreen(
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(0) } // 0 = Pomodoro, 1 = Stopwatch
    val haptic = LocalHapticFeedback.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Section Headers
            Text(
                text = "DEPTH METRICS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "FOCUS CHRONOMETER",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Modern Tab Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Pomodoro Timer Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (activeTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            activeTab = 0
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "POMODORO",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (activeTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Stopwatch Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (activeTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            activeTab = 1
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "STOPWATCH",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (activeTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Body Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                this@Column.AnimatedVisibility(
                    visible = activeTab == 0,
                    enter = fadeIn() + slideInVertically { 40 },
                    exit = fadeOut() + slideOutVertically { -40 }
                ) {
                    PomodoroView()
                }

                this@Column.AnimatedVisibility(
                    visible = activeTab == 1,
                    enter = fadeIn() + slideInVertically { 40 },
                    exit = fadeOut() + slideOutVertically { -40 }
                ) {
                    StopwatchView()
                }
            }
        }
    }
}

@Composable
fun PomodoroView() {
    val haptic = LocalHapticFeedback.current

    // Pomodoro states
    var durationMinutes by remember { mutableIntStateOf(25) }
    var secondsRemaining by remember { mutableIntStateOf(1500) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var sessionType by remember { mutableStateOf("Focus") } // "Focus", "Short Break", "Long Break"

    // Sync initial timers whenever defaults change
    fun resetTimer(minutes: Int, type: String) {
        isTimerRunning = false
        durationMinutes = minutes
        secondsRemaining = minutes * 60
        sessionType = type
    }

    // Countdown logic
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (secondsRemaining > 0 && isTimerRunning) {
                delay(1000L)
                secondsRemaining -= 1
            }
            if (secondsRemaining == 0) {
                isTimerRunning = false
                // Alert haptic feedback
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                // Complete session automatically swaps
                if (sessionType == "Focus") {
                    resetTimer(5, "Short Break")
                } else {
                    resetTimer(25, "Focus")
                }
            }
        }
    }

    val totalDurationSeconds = durationMinutes * 60
    val progressFraction = if (totalDurationSeconds > 0) {
        secondsRemaining.toFloat() / totalDurationSeconds
    } else {
        1f
    }

    val mins = secondsRemaining / 60
    val secs = secondsRemaining % 60
    val timeDisplay = String.format(Locale.US, "%02d:%02d", mins, secs)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Preset Pill Selectors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Focus presets
            arrayOf("25 min Focus" to Pair(25, "Focus"), "15 min Focus" to Pair(15, "Focus"), "5 min Break" to Pair(5, "Short Break"), "15 min Break" to Pair(15, "Long Break")).forEach { (label, data) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (durationMinutes == data.first && sessionType == data.second) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (durationMinutes == data.first && sessionType == data.second) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            resetTimer(data.first, data.second)
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        lineHeight = 12.sp,
                        color = if (durationMinutes == data.first && sessionType == data.second) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Large Premium Circular Chronometer HUD
        Box(
            modifier = Modifier
                .size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background subtle circular track
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 8.dp,
                strokeCap = StrokeCap.Round,
            )

            // Animated progress line
            CircularProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier.fillMaxSize(),
                color = if (sessionType == "Focus") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                strokeWidth = 8.dp,
                strokeCap = StrokeCap.Round,
            )

            // Dynamic typography statistics
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = sessionType.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (sessionType == "Focus") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = timeDisplay,
                    style = MaterialTheme.typography.displayMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isTimerRunning) "TICKING..." else "PAUSED",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset Button
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    resetTimer(durationMinutes, sessionType)
                },
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.surface, shape = CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Timer",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(32.dp))

            // Core Play / Pause Button with responsive ripple feedback and modern primary aesthetics
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isTimerRunning = !isTimerRunning
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = if (isTimerRunning) Icons.Default.Close else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.width(32.dp))

            // Skip Button to next stage
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (sessionType == "Focus") {
                        resetTimer(5, "Short Break")
                    } else {
                        resetTimer(25, "Focus")
                    }
                },
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.surface, shape = CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Skip Session",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun StopwatchView() {
    val haptic = LocalHapticFeedback.current

    // Stopwatch states
    var isRunning by remember { mutableStateOf(false) }
    var elapsedTimeMs by remember { mutableLongStateOf(0L) }
    val laps = remember { mutableStateListOf<Long>() }

    // Core high precision Stopwatch ticker loop
    LaunchedEffect(isRunning) {
        if (isRunning) {
            val startTime = System.currentTimeMillis() - elapsedTimeMs
            while (isRunning) {
                elapsedTimeMs = System.currentTimeMillis() - startTime
                delay(40L) // Refresh every 40ms (~25 FPS) for smooth digits
            }
        }
    }

    // Format helper e.g. "01:23.4"
    val totalSeconds = elapsedTimeMs / 1000
    val displayMins = totalSeconds / 60
    val displaySecs = totalSeconds % 60
    val tenthOfSecond = (elapsedTimeMs % 1000) / 100
    val stopwatchStr = String.format(Locale.US, "%02d:%02d.%d", displayMins, displaySecs, tenthOfSecond)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Large high-precision digital layout
        Text(
            text = stopwatchStr,
            style = MaterialTheme.typography.displayLarge,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Stopwatch controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lap / Reset Button
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (isRunning) {
                        laps.add(0, elapsedTimeMs)
                    } else {
                        // Reset everything
                        elapsedTimeMs = 0L
                        laps.clear()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = if (isRunning) "LAP" else "RESET",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // Start / Stop Button
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isRunning = !isRunning
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    contentColor = if (isRunning) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = if (isRunning) "STOP" else "START",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Record list of laps
        if (laps.isNotEmpty()) {
            Text(
                text = "LAP RECORDS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(laps) { index, lapTime ->
                    val lapSecs = lapTime / 1000
                    val lapMin = lapSecs / 60
                    val lapSec = lapSecs % 60
                    val lapTenth = (lapTime % 1000) / 100
                    val formattedLap = String.format(Locale.US, "%02d:%02d.%d", lapMin, lapSec, lapTenth)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LAP ${laps.size - index}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formattedLap,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        } else {
            // Empty spacer to occupy area when no laps are recorded
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

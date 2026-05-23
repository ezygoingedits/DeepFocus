package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.DisciplineViewModel
import com.example.util.DateUtils

@Composable
fun StatsScreen(
    viewModel: DisciplineViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.habits.collectAsState()
    val completions by viewModel.habitCompletions.collectAsState()

    val totalHabitsCount = habits.size
    val currentDate = DateUtils.getCurrentDateString()
    val yesterdayDate = DateUtils.getYesterdayDateString()

    // Calculate Weekly Info (Last 7 Days)
    val past7Days = remember {
        (0..6).map { offset ->
            DateUtils.getDateStringWithOffset(offset)
        }
    }

    // Calculate Monthly Progress (Last 30 Days)
    val past30Days = remember {
        (0..29).map { offset ->
            DateUtils.getDateStringWithOffset(offset)
        }.reversed() // chronological order
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Info Header
        Text(
            text = "RIGOROUS STATISTICAL TRUTH",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "ANALYSIS",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section 1: Streaks summary
            item {
                Column {
                    Text(
                        text = "ACTIVE STREAKS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (habits.isEmpty()) {
                        Text(
                            text = "Initiate habits to record consistency metrics.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            habits.forEach { habit ->
                                val isStreakActive = habit.lastCompleted == currentDate || habit.lastCompleted == yesterdayDate
                                val currentStreak = if (isStreakActive) habit.streak else 0

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = habit.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Text(
                                            text = "$currentStreak Days",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (currentStreak > 0) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                com.example.ui.theme.AccentFailure
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Weekly Consistency Layout
            item {
                Column {
                    Text(
                        text = "WEEKLY CONGRUENCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Past 7 days completions relative to active habits",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            past7Days.forEachIndexed { idx, date ->
                                val countOnDate = completions.count { it.date == date }
                                val dayLetter = DateUtils.getDayOfWeekLetter(idx)
                                val shortDate = DateUtils.getShortMonthAndDay(date)

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = dayLetter,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    // Completion status bullet representation
                                    Box(
                                        modifier = Modifier.size(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        when {
                                            totalHabitsCount > 0 && countOnDate == totalHabitsCount -> {
                                                // 100% Perfect completed day
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.fillMaxSize()
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(6.dp)
                                                                .background(
                                                                    color = MaterialTheme.colorScheme.background,
                                                                    shape = RoundedCornerShape(3.dp)
                                                                )
                                                        )
                                                    }
                                                }
                                            }
                                            countOnDate > 0 -> {
                                                // Partially completed day
                                                Surface(
                                                    color = MaterialTheme.colorScheme.surface,
                                                    shape = RoundedCornerShape(12.dp),
                                                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                                                    modifier = Modifier.fillMaxSize()
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(6.dp)
                                                                .background(
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                                    shape = RoundedCornerShape(3.dp)
                                                                )
                                                        )
                                                    }
                                                }
                                            }
                                            else -> {
                                                // Zero habits completed
                                                Surface(
                                                    color = MaterialTheme.colorScheme.surface,
                                                    shape = RoundedCornerShape(10.dp),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                                                    modifier = Modifier.size(20.dp)
                                                ) {}
                                            }
                                        }
                                    }

                                    Text(
                                        text = "$countOnDate/$totalHabitsCount",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Monthly Progress Matrix (Grid)
            item {
                Column {
                    Text(
                        text = "LAST 30 DAYS MATRIX",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Horizontal progression mapping. Bright represents perfect alignment.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Lay out 30 days into blocks, e.g. 5 columns by 6 rows
                            val colsCount = 6
                            val rowsCount = 5

                            for (r in 0 until rowsCount) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    for (c in 0 until colsCount) {
                                        val dayIndex = r * colsCount + c
                                        if (dayIndex < past30Days.size) {
                                            val date = past30Days[dayIndex]
                                            val countOnDate = completions.count { it.date == date }

                                            val color = when {
                                                totalHabitsCount > 0 && countOnDate == totalHabitsCount -> {
                                                    MaterialTheme.colorScheme.primary // Brilliant silver white
                                                }
                                                countOnDate > 0 -> {
                                                    MaterialTheme.colorScheme.outline // Muted Gray
                                                }
                                                else -> {
                                                    MaterialTheme.colorScheme.background // Pitch black
                                                }
                                            }

                                            Surface(
                                                color = color,
                                                shape = RoundedCornerShape(3.dp),
                                                border = if (color == MaterialTheme.colorScheme.background) {
                                                    BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                                } else null,
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .padding(2.dp)
                                            ) {
                                                // Optional tiny tooltip or meta count could go here if hovered, but minimalist is best!
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Legend Indicators
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(2.dp),
                                        modifier = Modifier.size(12.dp)
                                    ) {}
                                    Text("Perfect (All Completed)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(2.dp),
                                        modifier = Modifier.size(12.dp)
                                    ) {}
                                    Text("Partial Alignment", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

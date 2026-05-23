package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.DisciplineViewModel

@Composable
fun MainScaffoldScreen(
    viewModel: DisciplineViewModel
) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                modifier = Modifier
                    .testTag("bottom_nav_bar")
            ) {
                // Home (Daily Focus)
                NavigationBarItem(
                    selected = currentScreen == DisciplineViewModel.Screen.Home,
                    onClick = { viewModel.selectScreen(DisciplineViewModel.Screen.Home) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home Focus") },
                    label = { Text("Daily Focus", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_home")
                )

                // Weekly Planner
                NavigationBarItem(
                    selected = currentScreen == DisciplineViewModel.Screen.Planner,
                    onClick = { viewModel.selectScreen(DisciplineViewModel.Screen.Planner) },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Weekly Planner") },
                    label = { Text("Planner", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_planner")
                )

                // Focus Timer & Stopwatch
                NavigationBarItem(
                    selected = currentScreen == DisciplineViewModel.Screen.Timer,
                    onClick = { viewModel.selectScreen(DisciplineViewModel.Screen.Timer) },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Focus Timer") },
                    label = { Text("Timer", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_timer")
                )

                // Habits list with integrated Stats
                NavigationBarItem(
                    selected = currentScreen == DisciplineViewModel.Screen.Habits,
                    onClick = { viewModel.selectScreen(DisciplineViewModel.Screen.Habits) },
                    icon = { Icon(Icons.Default.List, contentDescription = "Habit Constants") },
                    label = { Text("Habits", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_habits")
                )

                // Reflection Journal
                NavigationBarItem(
                    selected = currentScreen == DisciplineViewModel.Screen.Journal,
                    onClick = { viewModel.selectScreen(DisciplineViewModel.Screen.Journal) },
                    icon = { Icon(Icons.Default.Edit, contentDescription = "Write Journal") },
                    label = { Text("Journal", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_journal")
                )
            }
        }
    ) { innerPadding ->
        // To prevent bottom element clipping, we pass modifier padding to inner pages
        val paddingModifier = Modifier.padding(innerPadding)
        
        when (currentScreen) {
            DisciplineViewModel.Screen.Home -> HomeScreen(viewModel, modifier = paddingModifier)
            DisciplineViewModel.Screen.Planner -> PlannerScreen(viewModel, modifier = paddingModifier)
            DisciplineViewModel.Screen.Timer -> TimerScreen(modifier = paddingModifier)
            DisciplineViewModel.Screen.Habits -> HabitsScreen(viewModel, modifier = paddingModifier)
            DisciplineViewModel.Screen.Journal -> JournalScreen(viewModel, modifier = paddingModifier)
            DisciplineViewModel.Screen.Stats -> StatsScreen(viewModel, modifier = paddingModifier)
        }
    }
}

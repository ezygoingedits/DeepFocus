package com.example

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.AppDatabase
import com.example.data.repository.DisciplineRepository
import com.example.ui.screens.MainScaffoldScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DisciplineViewModel

class MainActivity : ComponentActivity() {
  private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { _: Boolean -> }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Request Notification permission for Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(
          this,
          Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }

    // Initialize Database & Repository
    val database = AppDatabase.getDatabase(applicationContext)
    val repository = DisciplineRepository(
      database.habitDao(),
      database.taskDao(),
      database.journalDao(),
      database.habitCompletionDao()
    )
    val viewModelFactory = DisciplineViewModel.Factory(repository)
    val viewModel = ViewModelProvider(this, viewModelFactory)[DisciplineViewModel::class.java]

    setContent {
      MyApplicationTheme {
        MainScaffoldScreen(viewModel = viewModel)
      }
    }
  }
}


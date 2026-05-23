package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // e.g., "Mind", "Work", "Body", "Routine"
    val reminderTime: String? = null, // e.g., "08:30"
    val hasAlarm: Boolean = false,
    val isCompleted: Boolean = false,
    val isPinned: Boolean = false,
    val dateString: String? = null, // e.g., "2026-05-23" or null for general / today
    val createdAt: Long = System.currentTimeMillis()
)

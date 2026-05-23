package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val streak: Int = 0,
    val lastCompleted: String = "", // Format: "YYYY-MM-DD"
    val reminderTime: String? = null,
    val hasAlarm: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

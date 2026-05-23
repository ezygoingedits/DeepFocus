package com.example.data.model

import androidx.room.Entity

@Entity(tableName = "habit_completions", primaryKeys = ["habitId", "date"])
data class HabitCompletion(
    val habitId: Int,
    val date: String // Format: "YYYY-MM-DD"
)

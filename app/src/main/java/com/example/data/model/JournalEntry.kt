package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // Format: "YYYY-MM-DD"
    val content: String,
    val imageUri: String? = null, // e.g., Uri or local path
    val createdAt: Long = System.currentTimeMillis()
)

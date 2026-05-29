package com.example.liteloop.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val startTime: Long, // Milliseconds since start of day
    val endTime: Long,   // Milliseconds since start of day
    val frequencyMinutes: Int,
    val daysOfWeek: String, // Comma-separated indices (1=Mon, ..., 7=Sun)
    val isActive: Boolean = true,
    val isTtsEnabled: Boolean = true
)

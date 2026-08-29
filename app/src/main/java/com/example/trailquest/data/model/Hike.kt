package com.example.trailquest.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hikes")
data class Hike(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trailId: String,
    val userId: String,
    val startTime: Long,
    val endTime: Long? = null,
    val distanceKm: Double = 0.0,
    val durationMinutes: Int = 0
)
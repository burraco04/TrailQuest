package com.example.trailquest.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trails")
data class Trail(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val difficulty: String,
    val lengthKm: Double,
    val durationMinutes: Int,
    val points: Int,
    val imageUrl: String? = null
)

package com.example.trailquest.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hike_locations",
    indices = [Index(value = ["hikeId"])]
)
data class HikeLocation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hikeId: Long,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)
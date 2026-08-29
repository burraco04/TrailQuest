package com.example.trailquest.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hike_photos",
    indices = [Index(value = ["hikeId"])]
)
data class HikePhoto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hikeId: Long,
    val userId: String,
    val filePath: String,
    val timestamp: Long = System.currentTimeMillis()
)
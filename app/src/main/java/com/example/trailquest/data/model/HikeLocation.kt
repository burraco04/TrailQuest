package com.example.trailquest.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "hike_locations",
    foreignKeys = [
        ForeignKey(
            entity = Hike::class,
            parentColumns = ["id"],
            childColumns = ["hikeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class HikeLocation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hikeId: Long,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)

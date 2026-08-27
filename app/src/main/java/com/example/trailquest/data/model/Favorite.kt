package com.example.trailquest.data.model

import androidx.room.Entity

@Entity(tableName = "favorites", primaryKeys = ["userId", "trailId"])
data class Favorite(
    val userId: String,
    val trailId: String
)

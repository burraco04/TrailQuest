package com.example.trailquest.data.auth

data class UserProfile(
    val username: String = "",
    val profileImageUrl: String? = null,
    val points: Int = 0,
    val level: Int = 1,
    val completedHikes: Int = 0,
    val totalDistance: Double = 0.0,
    val badges: List<String> = emptyList()
)

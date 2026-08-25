package com.example.trailquest.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.trailquest.data.auth.User
import com.example.trailquest.data.auth.UserDao
import com.example.trailquest.data.model.Hike
import com.example.trailquest.data.model.Trail

@Database(entities = [Trail::class, Hike::class, User::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trailDao(): TrailDao

    abstract fun userDao(): UserDao
}

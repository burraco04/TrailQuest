package com.example.trailquest.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.trailquest.data.model.Hike
import com.example.trailquest.data.model.Trail

@Database(entities = [Trail::class, Hike::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trailDao(): TrailDao
}

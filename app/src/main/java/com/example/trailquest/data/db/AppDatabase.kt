package com.example.trailquest.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.trailquest.data.model.Favorite
import com.example.trailquest.data.model.Hike
import com.example.trailquest.data.model.HikeLocation
import com.example.trailquest.data.model.HikePhoto
import com.example.trailquest.data.model.Trail

@Database(
    entities = [Trail::class, Hike::class, HikeLocation::class, Favorite::class, HikePhoto::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trailDao(): TrailDao
}

package com.example.trailquest.data.db

import androidx.room.*
import com.example.trailquest.data.model.Hike
import com.example.trailquest.data.model.Trail
import kotlinx.coroutines.flow.Flow

@Dao
interface TrailDao {
    @Query("SELECT * FROM trails")
    fun getAllTrails(): Flow<List<Trail>>

    @Query("SELECT * FROM trails WHERE id = :trailId")
    suspend fun getTrailById(trailId: String): Trail?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrail(trail: Trail)

    @Update
    suspend fun updateTrail(trail: Trail)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHike(hike: Hike)

    @Query("SELECT * FROM hikes ORDER BY startTime DESC")
    fun getAllHikes(): Flow<List<Hike>>

    @Query("SELECT SUM(pointsEarned) FROM hikes")
    fun getTotalPoints(): Flow<Int?>
}

package com.example.trailquest.data.db

import androidx.room.*
import com.example.trailquest.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrailDao {
    @Query("SELECT * FROM trails")
    fun getAllTrails(): Flow<List<Trail>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrail(trail: Trail)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)

    @Delete
    suspend fun deleteFavorite(favorite: Favorite)

    @Query("SELECT trailId FROM favorites WHERE userId = :userId")
    fun getFavoriteIds(userId: String): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE trailId = :trailId AND userId = :userId)")
    fun isFavorite(trailId: String, userId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHike(hike: Hike)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHikeLocation(location: HikeLocation)

    @Query("SELECT * FROM hike_locations WHERE hikeId = :hikeId ORDER BY timestamp ASC")
    fun getHikeLocations(hikeId: Long): Flow<List<HikeLocation>>
}
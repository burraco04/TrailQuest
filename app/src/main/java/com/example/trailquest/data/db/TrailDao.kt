package com.example.trailquest.data.db

import androidx.room.*
import com.example.trailquest.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrailDao {
    // Trails (Local cache for performance)
    @Query("SELECT * FROM trails")
    fun getAllTrails(): Flow<List<Trail>>

    @Query("SELECT * FROM trails WHERE id = :trailId")
    suspend fun getTrailById(trailId: String): Trail?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrail(trail: Trail)

    // Favorites (Filtered by userId)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)

    @Delete
    suspend fun deleteFavorite(favorite: Favorite)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE trailId = :trailId AND userId = :userId)")
    fun isFavorite(trailId: String, userId: String): Flow<Boolean>

    @Query("SELECT trailId FROM favorites WHERE userId = :userId")
    fun getFavoriteIds(userId: String): Flow<List<String>>

    // Hikes (Local operational data)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHike(hike: Hike): Long

    @Update
    suspend fun updateHike(hike: Hike)

    @Query("SELECT * FROM hikes ORDER BY startTime DESC")
    fun getAllHikes(): Flow<List<Hike>>

    @Query("SELECT * FROM hikes WHERE id = :hikeId")
    suspend fun getHikeById(hikeId: Long): Hike?

    // GPS Tracking (High frequency local writes)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: HikeLocation)

    @Query("SELECT * FROM hike_locations WHERE hikeId = :hikeId ORDER BY timestamp ASC")
    fun getLocationsForHike(hikeId: Long): Flow<List<HikeLocation>>

    // Photos
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: HikePhoto)

    @Query("SELECT * FROM hike_photos WHERE hikeId = :hikeId")
    fun getPhotosForHike(hikeId: Long): Flow<List<HikePhoto>>
}

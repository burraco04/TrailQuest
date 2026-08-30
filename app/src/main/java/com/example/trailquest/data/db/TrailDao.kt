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

    // --- Gestione Foto Escursioni (Room) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHikePhoto(photo: HikePhoto)

    @Query("SELECT * FROM hike_photos WHERE userId = :userId AND EXISTS (SELECT 1 FROM hikes WHERE hikes.id = hike_photos.hikeId AND hikes.trailId = :trailId) ORDER BY timestamp DESC")
    fun getPhotosByTrailAndUser(trailId: String, userId: String): Flow<List<HikePhoto>>

    // Recupera TUTTE le foto di un utente specifico
    @Query("SELECT * FROM hike_photos WHERE userId = :userId ORDER BY timestamp DESC")
    fun getPhotosByUser(userId: String): Flow<List<HikePhoto>>

    @Query("""
        SELECT T.* FROM trails T
        INNER JOIN hikes H ON T.id = H.trailId
        WHERE H.userId = :userId AND H.endTime IS NOT NULL
        ORDER BY H.endTime DESC
        LIMIT 1
    """)
    fun getLastCompletedTrail(userId: String): Flow<Trail?>

    // Percorso più difficile concluso dall'utente
    // (Ordina la difficoltà in base alla scala: 'Difficile' > 'Medio' > 'Facile')
    @Query("""
        SELECT T.* FROM trails T
        INNER JOIN hikes H ON T.id = H.trailId
        WHERE H.userId = :userId AND H.endTime IS NOT NULL
        ORDER BY 
            CASE T.difficulty
                WHEN 'Difficile' THEN 3
                WHEN 'Medio' THEN 2
                WHEN 'Facile' THEN 1
                ELSE 0
            END DESC,
            H.endTime DESC
        LIMIT 1
    """)
    fun getMostDifficultCompletedTrail(userId: String): Flow<Trail?>

    // 3. Percorso completato più velocemente dall'utente (calcolando la differenza di tempo)
    @Query("""
        SELECT T.* FROM trails T
        INNER JOIN hikes H ON T.id = H.trailId
        WHERE H.userId = :userId AND H.endTime IS NOT NULL
        ORDER BY (H.endTime - H.startTime) ASC
        LIMIT 1
    """)
    fun getFastestCompletedTrail(userId: String): Flow<Trail?>

    // Conta il numero di escursioni completate
    @Query("SELECT COUNT(*) FROM hikes WHERE userId = :userId AND endTime IS NOT NULL")
    fun getCompletedHikesCount(userId: String): Flow<Int>

    // Calcola i km totali
    @Query("SELECT SUM(distanceKm) FROM hikes WHERE userId = :userId AND endTime IS NOT NULL")
    fun getTotalDistance(userId: String): Flow<Double?>

    // Calcola i minuti totali
    @Query("SELECT SUM(durationMinutes) FROM hikes WHERE userId = :userId AND endTime IS NOT NULL")
    fun getTotalDuration(userId: String): Flow<Int?>
}
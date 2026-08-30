package com.example.trailquest.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trailquest.data.auth.AuthRepository
import com.example.trailquest.data.auth.ProfileRepository
import com.example.trailquest.data.auth.UserProfile
import com.example.trailquest.data.db.TrailDao
import com.example.trailquest.data.model.*
import com.example.trailquest.data.pref.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class Badge(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val isUnlocked: Boolean
)

class MainViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val trailDao: TrailDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val currentUser = authRepository.currentUser

    val currentUserId: StateFlow<String?> = currentUser
        .map { it?.uid }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val userProfile: StateFlow<UserProfile?> = currentUser.flatMapLatest { user ->
        if (user != null) profileRepository.getProfile(user.uid)
        else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isDarkMode = settingsRepository.isDarkMode.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val trails: StateFlow<List<Trail>> = trailDao.getAllTrails()
        .combine(_searchQuery) { trails, query ->
            if (query.isBlank()) trails
            else trails.filter { it.name.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val favoriteIds: StateFlow<Set<String>> = currentUser.flatMapLatest { user ->
        val uid = user?.uid
        if (!uid.isNullOrBlank()) {
            trailDao.getFavoriteIds(uid).map { it.toSet() }
        } else {
            flowOf(emptySet())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val lastTrail: StateFlow<Trail?> = currentUserId.flatMapLatest { userId ->
        if (userId?.isEmpty() ?: false) flowOf(null)
        else trailDao.getLastCompletedTrail(userId ?: "")
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val hardestTrail: StateFlow<Trail?> = currentUserId.flatMapLatest { userId ->
        if (userId?.isEmpty() ?: false) flowOf(null)
        else trailDao.getMostDifficultCompletedTrail(userId ?: "")
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val fastestTrail: StateFlow<Trail?> = currentUserId.flatMapLatest { userId ->
        if (userId?.isEmpty() ?: false) flowOf(null)
        else trailDao.getFastestCompletedTrail(userId ?: "")
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val userBadges: StateFlow<List<Badge>> = currentUserId.flatMapLatest { userId ->
        if (userId.isNullOrEmpty()) {
            flowOf(emptyList())
        } else {
            combine(
                trailDao.getCompletedHikesCount(userId),
                trailDao.getTotalDistance(userId),
                trailDao.getTotalDuration(userId)
            ) { count, distance, duration ->
                val dist = distance ?: 0.0
                val dur = duration ?: 0

                listOf(
                    Badge(
                        name = "Primo Passo",
                        description = "Completa la tua prima escursione",
                        icon = Icons.Default.DirectionsWalk,
                        isUnlocked = count >= 1
                    ),
                    Badge(
                        name = "Camminatore Instancabile",
                        description = "Percorri almeno 50 km totali",
                        icon = Icons.Default.Star,
                        isUnlocked = dist >= 50.0
                    ),
                    Badge(
                        name = "Veterano dei Sentieri",
                        description = "Cammina per oltre 10 ore (600 min)",
                        icon = Icons.Default.EmojiEvents,
                        isUnlocked = dur >= 600
                    )
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    @OptIn(ExperimentalCoroutinesApi::class)
    fun isFavorite(trailId: String): Flow<Boolean> = currentUser.flatMapLatest { user ->
        val uid = user?.uid
        if (!uid.isNullOrBlank()) {
            trailDao.isFavorite(trailId, uid)
        } else {
            flowOf(false)
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            onResult(result.isSuccess)
        }
    }

    fun register(name: String, email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.signUp(email, password, name)
            if (result.isSuccess) {
                authRepository.currentUser.value?.let { user ->
                    profileRepository.createProfile(user.uid, email, name)
                }
            }
            onResult(result.isSuccess)
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun toggleDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkMode(isDark)
        }
    }

    fun toggleFavorite(trail: Trail) {
        val uid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            val isFav = trailDao.isFavorite(trail.id, uid).first()
            if (isFav) {
                trailDao.deleteFavorite(Favorite(uid, trail.id))
            } else {
                trailDao.insertFavorite(Favorite(uid, trail.id))
            }
        }
    }

    fun startHike(hikeId: Long, trailId: String) {
        val uid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            trailDao.insertHike(
                Hike(
                    id = hikeId,
                    trailId = trailId,
                    userId = uid,
                    startTime = System.currentTimeMillis()
                )
            )
        }
    }

    fun saveLocationPoint(hikeId: Long, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            trailDao.insertHikeLocation(
                HikeLocation(
                    hikeId = hikeId,
                    latitude = latitude,
                    longitude = longitude,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun saveHikePhoto(hikeId: Long, filePath: String) {
        val uid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            trailDao.insertHikePhoto(
                HikePhoto(
                    hikeId = hikeId,
                    userId = uid,
                    filePath = filePath,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // Flusso: Tutte le foto dell'utente loggato
    @OptIn(ExperimentalCoroutinesApi::class)
    val allUserPhotos: StateFlow<List<HikePhoto>> = currentUser.flatMapLatest { user ->
        if (user != null) trailDao.getPhotosByUser(user.uid)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Flusso: Foto dell'utente loggato per un sentiero specifico
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getUserPhotosForTrail(trailId: String): Flow<List<HikePhoto>> = currentUser.flatMapLatest { user ->
        if (user != null) trailDao.getPhotosByTrailAndUser(trailId, user.uid)
        else flowOf(emptyList())
    }
    // Aggiorna la foto profilo su Cloud Firestore
    fun updateProfilePicture(photoUrl: String) {
        val uid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            profileRepository.updateProfilePicture(uid, photoUrl)
        }
    }

    fun completeHike(hikeId: Long, trail: Trail, startTime: Long, distanceKm: Double) {
        val uid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            trailDao.insertHike(
                Hike(
                    id = hikeId,
                    trailId = trail.id,
                    userId = uid,
                    startTime = startTime,
                    endTime = System.currentTimeMillis(),
                    distanceKm = distanceKm,
                    durationMinutes = ((System.currentTimeMillis() - startTime) / 60000).toInt()
                )
            )

            profileRepository.updateStats(uid, trail.points, distanceKm)
        }
    }

    init {
        viewModelScope.launch {
            trailDao.insertTrail(Trail("1", "Sentiero Azzurro", "Un bellissimo sentiero lungo la costa delle Cinque Terre.", "Facile", 12.0, 180, 50))
            trailDao.insertTrail(Trail("2", "Alta Via dei Monti Liguri", "Percorso impegnativo con panorami mozzafiato sul mare.", "Difficile", 25.0, 480, 150))
            trailDao.insertTrail(Trail("3", "Sentiero del Pellegrino", "Un percorso storico tra Noli e Varigotti.", "Medio", 8.5, 120, 75))
        }
    }
}
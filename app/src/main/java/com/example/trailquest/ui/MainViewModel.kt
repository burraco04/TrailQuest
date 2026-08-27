package com.example.trailquest.ui

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

class MainViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val trailDao: TrailDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val currentUser = authRepository.currentUser

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

    // For display in TrailList - combined flow to check favorite status
    fun isFavorite(trailId: String): Flow<Boolean> = trailDao.isFavorite(trailId)

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            onResult(result.isSuccess)
        }
    }

    fun register(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.signUp(email, password)
            if (result.isSuccess) {
                // Create Firestore profile for new user
                currentUser.value?.let { user ->
                    profileRepository.createProfile(user.uid, user.email, email.substringBefore("@"))
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
        viewModelScope.launch {
            val isFav = trailDao.isFavorite(trail.id).first()
            if (isFav) {
                trailDao.deleteFavorite(Favorite(trail.id))
            } else {
                trailDao.insertFavorite(Favorite(trail.id))
            }
        }
    }

    fun completeHike(trail: Trail, distanceKm: Double) {
        viewModelScope.launch {
            val hikeId = trailDao.insertHike(Hike(
                trailId = trail.id,
                startTime = System.currentTimeMillis() - 3600000,
                endTime = System.currentTimeMillis(),
                distanceKm = distanceKm,
                pointsEarned = trail.points,
                isCompleted = true
            ))
            
            // Sync stats to Firestore
            currentUser.value?.let { user ->
                profileRepository.updateStats(user.uid, trail.points, distanceKm)
            }
        }
    }

    init {
        viewModelScope.launch {
            // Initial data prep
            trailDao.insertTrail(Trail("1", "Sentiero Azzurro", "Un bellissimo sentiero lungo la costa delle Cinque Terre.", "Facile", 12.0, 180, 50))
            trailDao.insertTrail(Trail("2", "Alta Via dei Monti Liguri", "Percorso impegnativo con panorami mozzafiato sul mare.", "Difficile", 25.0, 480, 150))
            trailDao.insertTrail(Trail("3", "Sentiero del Pellegrino", "Un percorso storico tra Noli e Varigotti.", "Medio", 8.5, 120, 75))
        }
    }
}

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

    @OptIn(ExperimentalCoroutinesApi::class)
    val favoriteIds: StateFlow<Set<String>> = currentUser.flatMapLatest { user ->
        if (user != null) trailDao.getFavoriteIds(user.uid).map { it.toSet() }
        else flowOf(emptySet())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun isFavorite(trailId: String): Flow<Boolean> {
        val userId = currentUser.value?.uid ?: return flowOf(false)
        return trailDao.isFavorite(trailId, userId)
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
                // The currentUser will be updated by AuthStateListener in AuthRepository
                // We might need to wait for it or use the result. But AuthRepository already updates _currentUser.
                val user = authRepository.currentUser.value
                if (user != null) {
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
        val userId = currentUser.value?.uid ?: return
        viewModelScope.launch {
            val isFav = trailDao.isFavorite(trail.id, userId).first()
            if (isFav) {
                trailDao.deleteFavorite(Favorite(userId, trail.id))
            } else {
                trailDao.insertFavorite(Favorite(userId, trail.id))
            }
        }
    }

    fun completeHike(trail: Trail, distanceKm: Double) {
        viewModelScope.launch {
            trailDao.insertHike(Hike(
                trailId = trail.id,
                startTime = System.currentTimeMillis() - 3600000,
                endTime = System.currentTimeMillis(),
                distanceKm = distanceKm,
                pointsEarned = trail.points,
                isCompleted = true
            ))
            
            currentUser.value?.let { user ->
                profileRepository.updateStats(user.uid, trail.points, distanceKm)
            }
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

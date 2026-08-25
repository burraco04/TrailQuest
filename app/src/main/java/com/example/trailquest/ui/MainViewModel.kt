package com.example.trailquest.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trailquest.data.auth.AuthRepository
import com.example.trailquest.data.db.TrailDao
import com.example.trailquest.data.model.Hike
import com.example.trailquest.data.model.Trail
import com.example.trailquest.data.pref.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val authRepository: AuthRepository,
    private val trailDao: TrailDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val currentUser = authRepository.currentUser
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

    val totalPoints = trailDao.getTotalPoints().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun login(
    username: String,
    password: String,
    onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val success = authRepository.login(
                username,
                password
            )

            onResult(success)
        }
    }

    fun register(
        username: String,
        email: String,
        password: String,
        onResult: (Boolean) -> Unit

    ) {
        viewModelScope.launch {
            val success = authRepository.createUser(
                username = username,
                email = email,
                password = password
            )

            onResult(success)
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
            trailDao.updateTrail(trail.copy(isFavorite = !trail.isFavorite))
        }
    }

    fun completeHike(trail: Trail, distanceKm: Double) {
        viewModelScope.launch {
            val hike = Hike(
                trailId = trail.id,
                startTime = System.currentTimeMillis() - 3600000,
                endTime = System.currentTimeMillis(),
                distanceKm = distanceKm,
                pointsEarned = trail.points,
                isCompleted = true
            )
            trailDao.insertHike(hike)
        }
    }

    init {
        viewModelScope.launch {
            authRepository.createTestUsers()
            trailDao.insertTrail(Trail("1", "Sentiero Azzurro", "Un bellissimo sentiero lungo la costa delle Cinque Terre.", "Facile", 12.0, 180, 50))
            trailDao.insertTrail(Trail("2", "Alta Via dei Monti Liguri", "Percorso impegnativo con panorami mozzafiato sul mare.", "Difficile", 25.0, 480, 150))
            trailDao.insertTrail(Trail("3", "Sentiero del Pellegrino", "Un percorso storico tra Noli e Varigotti.", "Medio", 8.5, 120, 75))
        }
    }
}

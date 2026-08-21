package com.example.trailquest.data.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class User(val username: String, val email: String)

class AuthRepository {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    fun login(username: String): Boolean {
        if (username.isNotBlank()) {
            _currentUser.value = User(username, "$username@example.com")
            return true
        }
        return false
    }

    fun logout() {
        _currentUser.value = null
    }
}

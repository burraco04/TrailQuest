package com.example.trailquest.data.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.security.MessageDigest

class AuthRepository(
    private val userDao: UserDao
) {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    suspend fun login(
        username: String,
        password: String
    ): Boolean {

        if (username.isBlank() || password.isBlank()) {
            return false
        }

        val user = userDao.getUser(username.trim())
            ?: return false

        val passwordHash = hashPassword(password)

        if (user.passwordHash != passwordHash) {
            return false
        }

        _currentUser.value = user
        return true
    }

    fun logout() {
        _currentUser.value = null
    }

    suspend fun createUser(
        username: String,
        email: String,
        password: String
    ): Boolean {

        if (
            username.isBlank() ||
            email.isBlank() ||
            password.isBlank()
        ) {
            return false
        }

        if (userDao.getUser(username.trim()) != null) {
            return false
        }

        val user = User(
            username = username.trim(),
            email = email.trim(),
            passwordHash = hashPassword(password)
        )

        userDao.insertUser(user)

        return true
    }

    suspend fun createTestUsers() {
        if (userDao.getUserCount() > 0) {
            return
        }

        createUser(
            username = "simone",
            email = "simone@trailquest.it",
            password = "password123"
        )

        createUser(
            username = "test",
            email = "test@trailquest.it",
            password = "test123"
        )
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())

        return hash.joinToString("") {
            "%02x".format(it)
        }
    }
}

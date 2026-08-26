package com.example.trailquest.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

data class AuthUser(val uid: String, val email: String, val displayName: String?)

class AuthRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    
    private val _currentUser = MutableStateFlow<AuthUser?>(mapFirebaseUser(firebaseAuth.currentUser))
    val currentUser: StateFlow<AuthUser?> = _currentUser

    init {
        firebaseAuth.addAuthStateListener { auth ->
            _currentUser.value = mapFirebaseUser(auth.currentUser)
        }
    }

    private fun mapFirebaseUser(firebaseUser: FirebaseUser?): AuthUser? {
        return firebaseUser?.let {
            AuthUser(
                uid = it.uid,
                email = it.email ?: "",
                displayName = it.displayName
            )
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(
        name: String,
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            val result = firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .await()

            result.user?.updateProfile(
                com.google.firebase.auth.userProfileChangeRequest {
                    displayName = name
                }
            )?.await()

            _currentUser.value = mapFirebaseUser(firebaseAuth.currentUser)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}

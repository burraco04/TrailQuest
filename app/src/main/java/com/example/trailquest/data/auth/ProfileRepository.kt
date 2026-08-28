package com.example.trailquest.data.auth

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProfileRepository {
    private val firestore = FirebaseFirestore.getInstance()

    fun getProfile(uid: String): Flow<UserProfile?> = callbackFlow {
        val listener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val profile = snapshot.toObject(UserProfile::class.java)
                    trySend(profile)
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun createProfile(uid: String, email: String, username: String) {
        val profile = UserProfile(
            username = username,
            points = 0,
            badges = emptyList(),
            profileImageUrl = ""
        )
        firestore.collection("users").document(uid).set(profile).await()
    }

    suspend fun updateStats(uid: String, addedPoints: Int, addedDistance: Double) {
        val docRef = firestore.collection("users").document(uid)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val currentPoints = snapshot.getLong("points") ?: 0
            transaction.update(docRef, "points", currentPoints + addedPoints)
        }.await()
    }

    // Aggiorna l'URL/path della foto profilo su Cloud Firestore
    suspend fun updateProfilePicture(uid: String, photoUrl: String) {
        firestore.collection("users").document(uid)
            .update("profilePictureUrl", photoUrl)
            .await()
    }
}
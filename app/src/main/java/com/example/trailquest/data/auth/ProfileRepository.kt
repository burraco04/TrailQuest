package com.example.trailquest.data.auth

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProfileRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    fun getProfile(uid: String): Flow<UserProfile?> = callbackFlow {
        val subscription = usersCollection.document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                trySend(snapshot.toObject(UserProfile::class.java))
            } else {
                trySend(null)
            }
        }
        awaitClose { subscription.remove() }
    }

    suspend fun createProfile(uid: String, email: String, username: String) {
        val profile = UserProfile(
            username = username,
            points = 0,
            level = 1,
            completedHikes = 0,
            totalDistance = 0.0,
            badges = emptyList()
        )
        usersCollection.document(uid).set(profile).await()
    }

    suspend fun updateStats(uid: String, pointsEarned: Int, distanceKm: Double) {
        val docRef = usersCollection.document(uid)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val currentPoints = snapshot.getLong("points") ?: 0
            val currentHikes = snapshot.getLong("completedHikes") ?: 0
            val currentDistance = snapshot.getDouble("totalDistance") ?: 0.0
            
            transaction.update(docRef, "points", currentPoints + pointsEarned)
            transaction.update(docRef, "completedHikes", currentHikes + 1)
            transaction.update(docRef, "totalDistance", currentDistance + distanceKm)
            
            // Logic for level up or badges can be added here
        }.await()
    }
}

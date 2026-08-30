package com.example.trailquest.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.trailquest.data.model.HikePhoto
import com.example.trailquest.data.model.Trail
import com.example.trailquest.ui.components.PhotoItem


@Composable
fun TrailDetailScreen(
    trail: Trail?,
    isFavorite: Boolean,
    userPhotos: List<HikePhoto>,
    onStartHike: () -> Unit,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit
) {
    if (trail == null) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = trail.name, style = MaterialTheme.typography.headlineMedium)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = trail.difficulty, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) {
                        Color.Red
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Dettagli", style = MaterialTheme.typography.titleMedium)
                Text(text = "Lunghezza: ${trail.lengthKm} km")
                Text(text = "Durata stimata: ${trail.durationMinutes} min")
                Text(text = "Punti: ${trail.points}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (userPhotos.isNotEmpty()) {
            Text(
                text = "Le tue foto del sentiero",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                items(userPhotos.size) { index ->
                    PhotoItem(photoPath = userPhotos[index].filePath)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Descrizione", style = MaterialTheme.typography.titleMedium)
        Text(text = trail.description, style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onStartHike,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🥾 Inizia Escursione")
        }
    }
}
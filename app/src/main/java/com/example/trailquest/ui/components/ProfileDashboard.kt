package com.example.trailquest.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trailquest.data.model.Trail
import com.example.trailquest.ui.MainViewModel

@Composable
fun ProfileDashboardSection(viewModel: MainViewModel) {
    val lastTrail by viewModel.lastTrail.collectAsState()
    val hardestTrail by viewModel.hardestTrail.collectAsState()
    val fastestTrail by viewModel.fastestTrail.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Statistiche Percorsi",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        DashboardStatCard(
            title = "Ultimo percorso svolto",
            trail = lastTrail,
            icon = Icons.Default.History
        )

        DashboardStatCard(
            title = "Percorso più difficile",
            trail = hardestTrail,
            icon = Icons.Default.FitnessCenter
        )

        DashboardStatCard(
            title = "Percorso più veloce",
            trail = fastestTrail,
            icon = Icons.Default.Speed
        )
    }
}

@Composable
fun DashboardStatCard(
    title: String,
    trail: Trail?,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = trail?.name ?: "Nessun percorso registrato",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (trail != null) {
                    Text(
                        text = "Difficoltà: ${trail.difficulty} • Lunghezza: ${trail.lengthKm} km",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
package com.example.trailquest.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.trailquest.data.model.Trail
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay

@Composable
fun HikeScreen(trail: Trail, onEndHike: (Double) -> Unit) {
    var showCamera by remember { mutableStateOf(false) }
    val trailStart = LatLng(44.4141, 8.9421)
    
    // Simulazione del percorso
    val pathPoints = remember { mutableStateListOf(trailStart) }
    var distance by remember { mutableStateOf(0.0) }
    var secondsElapsed by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            secondsElapsed += 5
            val lastPoint = pathPoints.last()
            val newPoint = LatLng(lastPoint.latitude + 0.0001, lastPoint.longitude + 0.0001)
            pathPoints.add(newPoint)
            distance += 0.015 // km simulati
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(trailStart, 15f)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) showCamera = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true)
        ) {
            Marker(
                state = MarkerState(position = trailStart),
                title = trail.name,
                snippet = "Punto di partenza"
            )
            Polyline(
                points = pathPoints.toList(),
                color = Color.Blue,
                width = 10f
            )
        }

        if (showCamera) {
            Box(modifier = Modifier.fillMaxSize()) {
                CameraPreview()
                IconButton(
                    onClick = { showCamera = false },
                    modifier = Modifier.align(Alignment.TopEnd).padding(32.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Chiudi Camera", tint = Color.White)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FloatingActionButton(onClick = { 
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Scatta Foto")
                    }
                    
                    ExtendedFloatingActionButton(
                        onClick = { onEndHike(distance) },
                        icon = { Icon(Icons.Default.Stop, contentDescription = null) },
                        text = { Text("Termina") },
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Distanza", style = MaterialTheme.typography.labelMedium)
                            Text("%.2f km".format(distance), style = MaterialTheme.typography.titleLarge)
                        }
                        Column {
                            val mins = secondsElapsed / 60
                            val secs = secondsElapsed % 60
                            Text("Tempo", style = MaterialTheme.typography.labelMedium)
                            Text("%02d:%02d".format(mins, secs), style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
        }
    }
}

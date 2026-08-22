package com.example.trailquest.ui

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.trailquest.data.model.Trail
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File

@Composable
fun HikeScreen(trail: Trail, onEndHike: (Double) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showCamera by remember { mutableStateOf(false) }
    val trailStart = GeoPoint(44.4141, 8.9421)

    // Configurazione OSM conforme alle linee guida
    Configuration.getInstance().apply {
        // 1. User-Agent identificativo dell'applicazione
        userAgentValue = "TrailQuest/1.0 (com.example.trailquest)"
        
        // 2. Configurazione della cache locale nella directory interna dell'app
        val osmCache = File(context.cacheDir, "osmdroid_tiles")
        if (!osmCache.exists()) osmCache.mkdirs()
        osmdroidTileCache = osmCache
    }

    var locationPermissionsGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionsGranted = permissions.values.any { it }
    }

    LaunchedEffect(Unit) {
        if (!locationPermissionsGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Logica di simulazione del percorso
    val pathPoints = remember { mutableStateListOf(trailStart) }
    var distance by remember { mutableStateOf(0.0) }
    var secondsElapsed by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            secondsElapsed += 5
            val lastPoint = pathPoints.last()
            val newPoint = GeoPoint(lastPoint.latitude + 0.0001, lastPoint.longitude + 0.0001)
            pathPoints.add(newPoint)
            distance += 0.015
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) showCamera = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val mapView = remember { MapView(context) }

        // Gestione Ciclo di Vita per osmdroid
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> mapView.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        AndroidView(
            factory = { 
                mapView.apply {
                    // Utilizzo di sorgente HTTPS (default in MAPNIK recente)
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(17.0)
                    controller.setCenter(trailStart)

                    // 3. Attribuzione Licenza Visibile (Obbligatoria)
                    val copyrightOverlay = CopyrightOverlay(context).apply {
                        setAlignRight(true)
                    }
                    overlays.add(copyrightOverlay)

                    // Marker di partenza
                    val startMarker = Marker(this)
                    startMarker.position = trailStart
                    startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    startMarker.title = trail.name
                    overlays.add(startMarker)

                    // Polyline per il tracciato simulato
                    val line = Polyline(this)
                    line.outlinePaint.color = android.graphics.Color.BLUE
                    line.outlinePaint.strokeWidth = 8f
                    line.setPoints(pathPoints)
                    overlays.add(line)

                    // Layer posizione utente reale
                    if (locationPermissionsGranted) {
                        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                        locationOverlay.enableMyLocation()
                        overlays.add(locationOverlay)
                    }
                }
            },
            update = { view ->
                // Aggiorna la polyline durante la simulazione
                val line = view.overlays.filterIsInstance<Polyline>().firstOrNull()
                line?.setPoints(pathPoints.toList())
                view.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // UI sovrapposta (Camera e Termina)
        if (showCamera) {
            Box(modifier = Modifier.fillMaxSize()) {
                CameraPreview()
                IconButton(
                    onClick = { showCamera = false },
                    modifier = Modifier.align(Alignment.TopEnd).padding(32.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Chiudi", tint = Color.White)
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
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FloatingActionButton(onClick = { 
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Foto")
                    }
                    ExtendedFloatingActionButton(
                        onClick = { onEndHike(distance) },
                        icon = { Icon(Icons.Default.Stop, contentDescription = null) },
                        text = { Text("Termina") },
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
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

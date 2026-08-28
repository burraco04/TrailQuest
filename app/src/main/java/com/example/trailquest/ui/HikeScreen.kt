package com.example.trailquest.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.trailquest.data.model.Trail
import com.google.android.gms.location.*
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File

@Composable
fun HikeScreen(
    trail: Trail,
    onStartHike: (hikeId: Long, trailId: String) -> Unit,
    onSaveLocation: (hikeId: Long, lat: Double, lng: Double) -> Unit,
    onPhotoTaken: (hikeId: Long, filePath: String) -> Unit,
    onEndHike: (hikeId: Long, startTime: Long, distance: Double) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showCamera by remember { mutableStateOf(false) }

    val hikeId = remember { System.currentTimeMillis() }
    val startTime = remember { System.currentTimeMillis() }

    LaunchedEffect(hikeId) {
        onStartHike(hikeId, trail.id)
    }

    val trailStart = GeoPoint(44.4141, 8.9421)

    Configuration.getInstance().apply {
        userAgentValue = "TrailQuest/1.0 (com.example.trailquest)"
        val osmCache = File(context.cacheDir, "osmdroid_tiles")
        if (!osmCache.exists()) osmCache.mkdirs()
        osmdroidTileCache = osmCache
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
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
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    val pathPoints = remember { mutableStateListOf<GeoPoint>() }
    var distance by remember { mutableStateOf(0.0) }
    var secondsElapsed by remember { mutableStateOf(0) }
    var lastLocation by remember { mutableStateOf<Location?>(null) }
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapViewInstance?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapViewInstance?.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            secondsElapsed++
        }
    }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    val newPoint = GeoPoint(location.latitude, location.longitude)

                    onSaveLocation(hikeId, location.latitude, location.longitude)

                    lastLocation?.let { last ->
                        val distanceMeters = last.distanceTo(location)
                        if (distanceMeters >= 3.0) {
                            distance += distanceMeters / 1000.0
                            pathPoints.add(newPoint)
                        }
                    } ?: run {
                        pathPoints.add(newPoint)
                    }
                    lastLocation = location
                }
            }
        }
    }

    DisposableEffect(locationPermissionsGranted) {
        if (locationPermissionsGranted) {
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
                .setMinUpdateIntervalMillis(3000L)
                .build()
            try {
                fusedLocationClient.requestLocationUpdates(request, locationCallback, context.mainLooper)
            } catch (e: SecurityException) { e.printStackTrace() }
        }
        onDispose { fusedLocationClient.removeLocationUpdates(locationCallback) }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) showCamera = true }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    mapViewInstance = this
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(17.0)
                    controller.setCenter(trailStart)
                    overlays.add(CopyrightOverlay(ctx).apply { setAlignRight(true) })

                    val line = Polyline(this)
                    line.outlinePaint.color = android.graphics.Color.BLUE
                    line.outlinePaint.strokeWidth = 8f
                    overlays.add(line)

                    if (locationPermissionsGranted) {
                        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                        locationOverlay.enableMyLocation()
                        overlays.add(locationOverlay)
                    }
                }
            },
            update = { view ->
                val line = view.overlays.filterIsInstance<Polyline>().firstOrNull()
                line?.setPoints(pathPoints.toList())

                pathPoints.lastOrNull()?.let { lastPoint ->
                    view.controller.animateTo(lastPoint)
                }
                view.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        if (showCamera) {
            val photoFile = remember {
                File(context.filesDir, "hike_${hikeId}_${System.currentTimeMillis()}.jpg").apply {
                    if (exists()) delete()
                    createNewFile()
                }
            }

            CameraPreview(
                outputFile = photoFile,
                onPhotoSaved = { savedFile ->
                    onPhotoTaken(hikeId, savedFile.absolutePath)
                    showCamera = false
                },
                onClose = {
                    showCamera = false
                }
            )
        } else {
            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FloatingActionButton(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Foto")
                    }
                    ExtendedFloatingActionButton(
                        onClick = { onEndHike(hikeId, startTime, distance) },
                        icon = { Icon(Icons.Default.Stop, contentDescription = null) },
                        text = { Text("Termina") },
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Distanza", style = MaterialTheme.typography.labelMedium)
                            Text("%.2f km".format(distance), style = MaterialTheme.typography.titleLarge)
                        }
                        Column {
                            Text("Tempo", style = MaterialTheme.typography.labelMedium)
                            Text("%02d:%02d".format(secondsElapsed / 60, secondsElapsed % 60), style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
        }
    }
}
package com.example.trailquest.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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
fun HikeScreen(
    trail: Trail,
    onEndHike: (Double) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var showCamera by remember { mutableStateOf(false) }

    // Punto di partenza del sentiero
    val trailStart = GeoPoint(44.4141, 8.9421)

    // Configurazione osmdroid
    Configuration.getInstance().apply {
        userAgentValue = "TrailQuest/1.0 (com.example.trailquest)"

        val osmCache = File(context.cacheDir, "osmdroid_tiles")

        if (!osmCache.exists()) {
            osmCache.mkdirs()
        }

        osmdroidTileCache = osmCache
    }

    // Client GPS
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Controllo permessi GPS
    var locationPermissionsGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Launcher per richiesta permessi GPS
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        locationPermissionsGranted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Richiediamo il permesso quando si apre l'escursione
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

    // Punti realmente percorsi
    val pathPoints = remember {
        mutableStateListOf<GeoPoint>()
    }

    // Distanza totale in km
    var distance by remember {
        mutableStateOf(0.0)
    }

    // Tempo dell'escursione
    var secondsElapsed by remember {
        mutableStateOf(0)
    }

    // Timer
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            secondsElapsed++
        }
    }

    /*
     * Callback chiamato dal GPS ogni volta che
     * viene ricevuta una nuova posizione.
     */
    val locationCallback = remember {

        object : LocationCallback() {

            override fun onLocationResult(
                result: LocationResult
            ) {

                for (location in result.locations) {

                    val newPoint = GeoPoint(
                        location.latitude,
                        location.longitude
                    )

                    /*
                     * Se abbiamo già una posizione,
                     * calcoliamo la distanza percorsa.
                     */
                    if (pathPoints.isNotEmpty()) {

                        val previousPoint =
                            pathPoints.last()

                        val previousLocation =
                            Location("").apply {
                                latitude =
                                    previousPoint.latitude

                                longitude =
                                    previousPoint.longitude
                            }

                        val distanceMeters =
                            previousLocation.distanceTo(location)

                        /*
                         * Ignoriamo piccoli movimenti
                         * inferiori a 3 metri.
                         */
                        if (distanceMeters >= 3) {

                            distance +=
                                distanceMeters / 1000.0

                            pathPoints.add(newPoint)
                        }

                    } else {

                        // Prima posizione GPS
                        pathPoints.add(newPoint)
                    }
                }
            }
        }
    }

    /*
     * Configurazione aggiornamenti GPS.
     */
    val locationRequest = remember {

        LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000L
        )
            .setMinUpdateIntervalMillis(2000L)
            .setWaitForAccurateLocation(false)
            .build()
    }

    /*
     * Avvio tracking GPS.
     */
    DisposableEffect(locationPermissionsGranted) {

        if (locationPermissionsGranted) {

            try {

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    context.mainLooper
                )

            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }

        onDispose {

            fusedLocationClient.removeLocationUpdates(
                locationCallback
            )
        }
    }

    // Permesso fotocamera
    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->

            if (isGranted) {
                showCamera = true
            }
        }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        /*
         * MAPPA
         *
         * Non creiamo più il MapView con remember.
         * Viene creato direttamente dentro AndroidView.
         */
        AndroidView(
            factory = { ctx ->

                MapView(ctx).apply {

                    setTileSource(
                        TileSourceFactory.MAPNIK
                    )

                    setMultiTouchControls(true)

                    controller.setZoom(17.0)

                    controller.setCenter(
                        trailStart
                    )

                    // Copyright OpenStreetMap
                    val copyrightOverlay =
                        CopyrightOverlay(ctx).apply {
                            setAlignRight(true)
                        }

                    overlays.add(
                        copyrightOverlay
                    )

                    // Marker del punto di partenza
                    val startMarker =
                        Marker(this)

                    startMarker.position =
                        trailStart

                    startMarker.setAnchor(
                        Marker.ANCHOR_CENTER,
                        Marker.ANCHOR_BOTTOM
                    )

                    startMarker.title =
                        trail.name

                    overlays.add(
                        startMarker
                    )

                    // Linea del percorso
                    val line =
                        Polyline(this)

                    line.outlinePaint.color =
                        android.graphics.Color.BLUE

                    line.outlinePaint.strokeWidth =
                        8f

                    line.setPoints(
                        pathPoints
                    )

                    overlays.add(
                        line
                    )

                    // Posizione GPS dell'utente
                    if (locationPermissionsGranted) {

                        val locationOverlay =
                            MyLocationNewOverlay(
                                GpsMyLocationProvider(ctx),
                                this
                            )

                        locationOverlay.enableMyLocation()

                        overlays.add(
                            locationOverlay
                        )
                    }
                }
            },

            update = { view: MapView ->

                // Aggiorna la linea del percorso
                val line =
                    view.overlays
                        .filterIsInstance<Polyline>()
                        .firstOrNull()

                line?.setPoints(
                    pathPoints.toList()
                )

                view.invalidate()
            },

            modifier = Modifier.fillMaxSize()
        )

        /*
         * FOTOCAMERA
         */
        if (showCamera) {

            Box(
                modifier = Modifier.fillMaxSize()
            ) {

                CameraPreview()

                IconButton(
                    onClick = {
                        showCamera = false
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(32.dp)
                ) {

                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Chiudi",
                        tint = Color.White
                    )
                }
            }

        } else {

            /*
             * CONTROLLI INFERIORI
             */
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {

                    // Fotocamera
                    FloatingActionButton(
                        onClick = {

                            cameraPermissionLauncher.launch(
                                Manifest.permission.CAMERA
                            )
                        }
                    ) {

                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Foto"
                        )
                    }

                    // Termina escursione
                    ExtendedFloatingActionButton(

                        onClick = {
                            onEndHike(distance)
                        },

                        icon = {
                            Icon(
                                Icons.Default.Stop,
                                contentDescription = null
                            )
                        },

                        text = {
                            Text("Termina")
                        },

                        containerColor =
                            MaterialTheme.colorScheme.errorContainer
                    )
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                // Informazioni escursione
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Row(
                        modifier = Modifier.padding(16.dp),

                        horizontalArrangement =
                            Arrangement.SpaceBetween,

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Column {

                            Text(
                                "Distanza",
                                style =
                                    MaterialTheme
                                        .typography
                                        .labelMedium
                            )

                            Text(
                                "%.2f km".format(distance),
                                style =
                                    MaterialTheme
                                        .typography
                                        .titleLarge
                            )
                        }

                        Column {

                            val mins =
                                secondsElapsed / 60

                            val secs =
                                secondsElapsed % 60

                            Text(
                                "Tempo",
                                style =
                                    MaterialTheme
                                        .typography
                                        .labelMedium
                            )

                            Text(
                                "%02d:%02d".format(
                                    mins,
                                    secs
                                ),
                                style =
                                    MaterialTheme
                                        .typography
                                        .titleLarge
                            )
                        }
                    }
                }
            }
        }
    }
}
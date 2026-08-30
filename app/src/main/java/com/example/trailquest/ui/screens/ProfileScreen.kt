package com.example.trailquest.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.example.trailquest.data.auth.UserProfile
import com.example.trailquest.data.model.HikePhoto
import com.example.trailquest.ui.components.BadgesDashboardSection
import com.example.trailquest.ui.CameraPreview
import com.example.trailquest.ui.MainViewModel
import com.example.trailquest.ui.components.PhotoItem
import com.example.trailquest.ui.components.ProfileDashboardSection
import com.example.trailquest.ui.utils.decodeSampledBitmapFromFile
import java.io.File


@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    profile: UserProfile,
    email: String,
    allUserPhotos: List<HikePhoto>,
    onUpdateProfilePicture: (String) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var showOptionsDialog by remember { mutableStateOf(false) }
    var showCameraPreview by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showCameraPreview = true
        }
    }

    val level = when {
        profile.points >= 2000 -> 6
        profile.points >= 1000 -> 5
        profile.points >= 500 -> 4
        profile.points >= 250 -> 3
        profile.points >= 100 -> 2
        else -> 1
    }

    val levelName = when (level) {
        6 -> "Maestro dei sentieri"
        5 -> "Esperto"
        4 -> "Avventuriero"
        3 -> "Escursionista"
        2 -> "Camminatore"
        else -> "Esploratore"
    }

    val nextLevelPoints = when {
        profile.points < 100 -> 100
        profile.points < 250 -> 250
        profile.points < 500 -> 500
        profile.points < 1000 -> 1000
        profile.points < 2000 -> 2000
        else -> 2000
    }

    val previousLevelPoints = when {
        profile.points < 100 -> 0
        profile.points < 250 -> 100
        profile.points < 500 -> 250
        profile.points < 1000 -> 500
        profile.points < 2000 -> 1000
        else -> 2000
    }

    val progress = if (level == 6) {
        1f
    } else {
        ((profile.points - previousLevelPoints).toFloat() /
                (nextLevelPoints - previousLevelPoints))
            .coerceIn(0f, 1f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Profilo", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(24.dp))

            // Avatar / Foto Profilo dell'utente
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .clickable { showOptionsDialog = true },
                contentAlignment = Alignment.Center
            ) {
                val photoPath = profile.profileImageUrl

                // Decodifica l'immagine riducendone la risoluzione a 500x500px in memoria
                val profileBitmap = remember(photoPath) {
                    if (photoPath?.isNotBlank() ?: false) {
                        val file = File(photoPath)
                        if (file.exists()) {
                            decodeSampledBitmapFromFile(file.absolutePath, 500, 500)
                        } else null
                    } else null
                }

                if (profileBitmap != null) {
                    Image(
                        bitmap = profileBitmap.asImageBitmap(),
                        contentDescription = "Foto Profilo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(text = "👤", style = MaterialTheme.typography.displayLarge)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = profile.username,
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Livello $level", style = MaterialTheme.typography.titleLarge)
                    Text(text = levelName, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (level == 6) {
                            "${profile.points} punti"
                        } else {
                            "${profile.points} / $nextLevelPoints punti"
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SEZIONE TUTTE LE TUE FOTO (NUOVA)
            if (allUserPhotos.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Le tue foto", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(allUserPhotos.size) { index ->
                            PhotoItem(photoPath = allUserPhotos[index].filePath)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // AGGIUNGI QUESTA RIGA PER MOSTRARE LA DASHBOARD:
            ProfileDashboardSection(viewModel = viewModel)

            Spacer(modifier = Modifier.height(16.dp))

            BadgesDashboardSection(viewModel = viewModel)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Logout")
            }
        }
    }

    // Pop-up di selezione
    if (showOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showOptionsDialog = false },
            title = { Text("Foto Profilo") },
            text = { Text("Vuoi scattare una nuova foto da impostare come foto profilo?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showOptionsDialog = false
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                ) {
                    Text("Modifica foto")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOptionsDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }

    // Preview Fotocamera
    if (showCameraPreview) {
        val photoFile = remember {
            // Eliminiamo l'eventuale foto profilo precedente per risparmiare memoria
            if (profile.profileImageUrl?.isNotBlank() ?: false) {
                val oldFile = File(profile.profileImageUrl)
                if (oldFile.exists()) oldFile.delete()
            }

            // Generiamo un file con timestamp unico per forzare la ricarica dello stato di Compose
            File(context.filesDir, "profile_${System.currentTimeMillis()}.jpg").apply {
                createNewFile()
            }
        }

        CameraPreview(
            outputFile = photoFile,
            onPhotoSaved = { savedFile ->
                onUpdateProfilePicture(savedFile.absolutePath)
                showCameraPreview = false
            },
            onClose = {
                showCameraPreview = false
            }
        )
    }
}
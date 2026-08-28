package com.example.trailquest.ui

import android.Manifest
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.trailquest.data.auth.UserProfile
import com.example.trailquest.data.model.Trail
import java.io.File

@Composable
fun LoginScreen(
    onLogin: (String, String, (Boolean) -> Unit) -> Unit,
    onRegister: (String, String, String, (Boolean) -> Unit) -> Unit
) {
    var isRegistering by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isRegistering) "Crea Account" else "TrailQuest Login",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isRegistering) {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    message = ""
                },
                label = { Text("Nome") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                message = ""
            },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                message = ""
            },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (isRegistering) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    message = ""
                },
                label = { Text("Conferma Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (message.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (isRegistering) {
                    when {
                        name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                            message = "Compila tutti i campi"
                            isError = true
                        }
                        !email.contains("@") -> {
                            message = "Inserisci un'email valida"
                            isError = true
                        }
                        password.length < 6 -> {
                            message = "La password deve avere almeno 6 caratteri"
                            isError = true
                        }
                        password != confirmPassword -> {
                            message = "Le password non coincidono"
                            isError = true
                        }
                        else -> {
                            onRegister(name, email, password) { success ->
                                if (success) {
                                    message = "Registrazione completata!"
                                    isError = false
                                    isRegistering = false
                                } else {
                                    message = "Errore durante la registrazione"
                                    isError = true
                                }
                            }
                        }
                    }
                } else {
                    if (email.isBlank() || password.isBlank()) {
                        message = "Inserisci email e password"
                        isError = true
                    } else {
                        onLogin(email, password) { success ->
                            if (!success) {
                                message = "Email o password non corretti"
                                isError = true
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isRegistering) "Registrati" else "Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                isRegistering = !isRegistering
                message = ""
                name = ""
                email = ""
                password = ""
                confirmPassword = ""
            }
        ) {
            Text(if (isRegistering) "Hai già un account? Accedi" else "Non hai un account? Registrati")
        }
    }
}

@Composable
fun TrailListScreen(
    trails: List<Trail>,
    favoriteIds: Set<String>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onTrailClick: (Trail) -> Unit,
    onToggleFavorite: (Trail) -> Unit
) {
    var selectedDifficulty by remember {
        mutableStateOf("Tutti")
    }

    val filteredTrails = trails.filter { trail ->
        selectedDifficulty == "Tutti" ||
                trail.difficulty.equals(
                    selectedDifficulty,
                    ignoreCase = true
                )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Sentieri",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Cerca sentieri...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cerca") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Tutti", "Facile", "Medio", "Difficile").forEach { difficulty ->
                FilterChip(
                    selected = selectedDifficulty == difficulty,
                    onClick = { selectedDifficulty = difficulty },
                    label = { Text(difficulty) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Sentieri Disponibili",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredTrails.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nessun sentiero trovato",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filteredTrails) { trail ->
                    val isFav = favoriteIds.contains(trail.id)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { onTrailClick(trail) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(trail.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "${trail.difficulty} • ${trail.lengthKm} km",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onToggleFavorite(trail) }) {
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrailDetailScreen(
    trail: Trail?,
    isFavorite: Boolean,
    onStartHike: () -> Unit,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit
) {
    if (trail == null) return
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
                    tint = if (isFavorite) Color.Red else LocalContentColor.current
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

@Composable
fun ProfileScreen(
    profile: UserProfile,
    email: String,
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

                // Caricamento nativo sicuro con gestione eccezioni
                val profileBitmap = remember(photoPath) {
                    if (photoPath?.isNotBlank() ?: false) {
                        val file = File(photoPath)
                        if (file.exists()) {
                            try {
                                BitmapFactory.decodeFile(file.absolutePath)
                            } catch (e: Exception) {
                                null
                            }
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

            Spacer(modifier = Modifier.height(16.dp))

            if (profile.badges.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Badge", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            profile.badges.forEach { _ ->
                                Text(text = "🏆 ", style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Logout")
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
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineSmall)
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun SettingsScreen(isDarkMode: Boolean, onToggleDarkMode: (Boolean) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Impostazioni", style = MaterialTheme.typography.headlineMedium)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Text("Tema Scuro", modifier = Modifier.weight(1f))
            Switch(checked = isDarkMode, onCheckedChange = onToggleDarkMode)
        }
    }
}

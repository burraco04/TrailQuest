package com.example.trailquest.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.trailquest.data.model.Trail
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material.icons.filled.ArrowBack

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Sentieri",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // Ricerca
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Cerca sentieri...")
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Cerca"
                )
            },
            singleLine = true
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // Filtri difficoltà
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            FilterChip(
                selected = selectedDifficulty == "Tutti",
                onClick = {
                    selectedDifficulty = "Tutti"
                },
                label = {
                    Text("Tutti")
                }
            )

            FilterChip(
                selected = selectedDifficulty == "Facile",
                onClick = {
                    selectedDifficulty = "Facile"
                },
                label = {
                    Text("Facile")
                }
            )

            FilterChip(
                selected = selectedDifficulty == "Medio",
                onClick = {
                    selectedDifficulty = "Medio"
                },
                label = {
                    Text("Medio")
                }
            )

            FilterChip(
                selected = selectedDifficulty == "Difficile",
                onClick = {
                    selectedDifficulty = "Difficile"
                },
                label = {
                    Text("Difficile")
                }
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "Sentieri Disponibili",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

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

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {

                items(filteredTrails) { trail ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                onTrailClick(trail)
                            }
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                Text(
                                    text = trail.name,
                                    style =
                                        MaterialTheme
                                            .typography
                                            .titleMedium
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(4.dp)
                                )

                                Text(
                                    text =
                                        "${trail.difficulty} • " +
                                                "${trail.lengthKm} km • " +
                                                "${trail.durationMinutes} min",
                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodyMedium,
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .onSurfaceVariant
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(4.dp)
                                )

                                Text(
                                    text =
                                        "⭐ ${trail.points} punti",
                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodySmall
                                )
                            }

                            IconButton(
                                onClick = {
                                    onToggleFavorite(trail)
                                }
                            ) {

                                Icon(
                                    imageVector =
                                        if (trail.isFavorite) {
                                            Icons.Default.Favorite
                                        } else {
                                            Icons.Default.FavoriteBorder
                                        },
                                    contentDescription =
                                        if (trail.isFavorite) {
                                            "Rimuovi dai preferiti"
                                        } else {
                                            "Aggiungi ai preferiti"
                                        },
                                    tint =
                                        if (trail.isFavorite) {
                                            MaterialTheme
                                                .colorScheme
                                                .primary
                                        } else {
                                            MaterialTheme
                                                .colorScheme
                                                .onSurfaceVariant
                                        }
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
fun TrailDetailScreen(trail: Trail?,
                      onStartHike: () -> Unit,
                      onToggleFavorite: () -> Unit,
                      onBack: () ->Unit) {
    if (trail == null) return
    Column(modifier = Modifier.fillMaxSize().padding(16.dp))  {

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Indietro"
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text("Indietro")
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {

            item {

                Text(
                    text = trail.name,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                // Difficoltà
                AssistChip(
                    onClick = {},
                    label = {
                        Text(trail.difficulty)
                    }
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                // Informazioni principali
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement =
                            Arrangement.SpaceEvenly
                    ) {

                        TrailInfo(
                            value = "${trail.lengthKm} km",
                            label = "Distanza"
                        )

                        TrailInfo(
                            value = "${trail.durationMinutes} min",
                            label = "Durata"
                        )

                        TrailInfo(
                            value = "${trail.points}",
                            label = "Punti"
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                // Descrizione
                Text(
                    text = "Descrizione",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = trail.description,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                // Dettagli
                Text(
                    text = "Informazioni",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                TrailDetailRow(
                    label = "Difficoltà",
                    value = trail.difficulty
                )

                TrailDetailRow(
                    label = "Distanza",
                    value = "${trail.lengthKm} km"
                )

                TrailDetailRow(
                    label = "Durata stimata",
                    value = "${trail.durationMinutes} minuti"
                )

                TrailDetailRow(
                    label = "Punti ottenibili",
                    value = "${trail.points} punti"
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                // Preferito
                OutlinedButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Icon(
                        imageVector =
                            if (trail.isFavorite) {
                                Icons.Default.Favorite
                            } else {
                                Icons.Default.FavoriteBorder
                            },
                        contentDescription = null
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text(
                        if (trail.isFavorite) {
                            "Nei preferiti"
                        } else {
                            "Aggiungi ai preferiti"
                        }
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // Bottone principale
        Button(
            onClick = onStartHike,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("🥾  Inizia Escursione")
        }
    }
}

@Composable
private fun TrailInfo(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
private fun TrailDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ProfileScreen(
    username: String,
    email: String,
    points: Int,
    onLogout: () -> Unit
) {
    val level = when {
        points >= 2000 -> 6
        points >= 1000 -> 5
        points >= 500 -> 4
        points >= 250 -> 3
        points >= 100 -> 2
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
        points < 100 -> 100
        points < 250 -> 250
        points < 500 -> 500
        points < 1000 -> 1000
        points < 2000 -> 2000
        else -> 2000
    }

    val previousLevelPoints = when {
        points < 100 -> 0
        points < 250 -> 100
        points < 500 -> 250
        points < 1000 -> 500
        points < 2000 -> 1000
        else -> 2000
    }

    val progress = if (level == 6) {
        1f
    } else {
        ((points - previousLevelPoints).toFloat() /
                (nextLevelPoints - previousLevelPoints))
            .coerceIn(0f, 1f)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profilo",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "👤",
            style = MaterialTheme.typography.displayMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = username,
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Livello $level",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = levelName,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (level == 6) {
                        "$points punti"
                    } else {
                        "$points / $nextLevelPoints punti"
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Statistiche",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "0",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text("Escursioni")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "0 km",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text("Distanza")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$points",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text("Punti")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Badge",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text("🥾")
                    Text("🔒")
                    Text("🔒")
                    Text("🔒")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
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

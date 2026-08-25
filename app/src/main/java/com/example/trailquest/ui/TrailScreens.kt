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

@Composable
fun LoginScreen(
    onLogin: (
        String,
        String,
        (Boolean) -> Unit
    ) -> Unit,

    onRegister: (
        String,
        String,
        String,
        (Boolean) -> Unit
    ) -> Unit ) {
    var isRegistering by remember {
        mutableStateOf(false)
    }

    var username by remember {
        mutableStateOf("")
    }

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var confirmPassword by remember {
        mutableStateOf("")
    }

    var message by remember {
        mutableStateOf("")
    }

    var isError by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),

        verticalArrangement = Arrangement.Center,

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = if (isRegistering) {
                "Crea Account"
            } else {
                "TrailQuest Login"
            },

            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        OutlinedTextField(
            value = username,

            onValueChange = {
                username = it
                message = ""
            },

            label = {
                Text("Username")
            },

            singleLine = true,

            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        if (isRegistering) {

            OutlinedTextField(
                value = email,

                onValueChange = {
                    email = it
                    message = ""
                },

                label = {
                    Text("Email")
                },

                singleLine = true,

                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }

        OutlinedTextField(
            value = password,

            onValueChange = {
                password = it
                message = ""
            },

            label = {
                Text("Password")
            },

            singleLine = true,

            visualTransformation =
                PasswordVisualTransformation(),

            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        if (isRegistering) {

            OutlinedTextField(
                value = confirmPassword,

                onValueChange = {
                    confirmPassword = it
                    message = ""
                },

                label = {
                    Text("Conferma Password")
                },

                singleLine = true,

                visualTransformation =
                    PasswordVisualTransformation(),

                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )
        }

        if (message.isNotBlank()) {

            Text(
                text = message,

                color = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }

        Button(
            onClick = {

                if (isRegistering) {

                    when {

                        username.isBlank() ||
                                email.isBlank() ||
                                password.isBlank() ||
                                confirmPassword.isBlank() -> {

                            message =
                                "Compila tutti i campi"

                            isError = true
                        }

                        !email.contains("@") -> {

                            message =
                                "Inserisci un'email valida"

                            isError = true
                        }

                        password.length < 6 -> {

                            message =
                                "La password deve avere almeno 6 caratteri"

                            isError = true
                        }

                        password != confirmPassword -> {

                            message =
                                "Le password non coincidono"

                            isError = true
                        }

                        else -> {

                            onRegister(
                                username,
                                email,
                                password
                            ) { success ->

                                if (success) {

                                    message =
                                        "Registrazione completata! Ora puoi accedere."

                                    isError = false

                                    isRegistering = false

                                    password = ""
                                    confirmPassword = ""

                                } else {

                                    message =
                                        "Username già esistente"

                                    isError = true
                                }
                            }
                        }
                    }

                } else {

                    if (
                        username.isBlank() ||
                        password.isBlank()
                    ) {

                        message =
                            "Inserisci username e password"

                        isError = true

                    } else {

                        onLogin(
                            username,
                            password
                        ) { success ->

                            if (!success) {

                                message =
                                    "Username o password non corretti"

                                isError = true

                            } else {

                                message = ""
                                isError = false
                            }
                        }
                    }
                }
            },

            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                if (isRegistering) {
                    "Registrati"
                } else {
                    "Login"
                }
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        TextButton(
            onClick = {

                isRegistering =
                    !isRegistering

                message = ""

                username = ""
                email = ""
                password = ""
                confirmPassword = ""
            }
        ) {

            Text(
                if (isRegistering) {
                    "Hai già un account? Accedi"
                } else {
                    "Non hai un account? Registrati"
                }
            )
        }

        if (!isRegistering) {

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                "Account di test:",
                style = MaterialTheme.typography.titleSmall
            )

            Text(
                "simone / password123"
            )

            Text(
                "test / test123"
            )
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
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Cerca sentieri...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                Text("Sentieri Disponibili", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(trails) { trail ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onTrailClick(trail) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(trail.name, style = MaterialTheme.typography.titleMedium)
                            Text("${trail.difficulty} • ${trail.lengthKm} km • ${trail.durationMinutes} min")
                        }
                        IconButton(onClick = { onToggleFavorite(trail) }) {
                            Icon(
                                imageVector = if (trail.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (trail.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrailDetailScreen(trail: Trail?, onStartHike: () -> Unit) {
    if (trail == null) return
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(trail.name, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(trail.description)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Punti guadagnati: ${trail.points}")
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onStartHike,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Inizia Escursione")
        }
    }
}

@Composable
fun ProfileScreen(username: String, points: Int, onLogout: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Profilo di $username", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        Text("Punti Totali: $points", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Badge Sbloccati: 🎖️ 🏆 🏔️")
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onLogout) {
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

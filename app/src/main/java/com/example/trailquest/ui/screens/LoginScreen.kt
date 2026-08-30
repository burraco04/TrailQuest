package com.example.trailquest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp



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
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isRegistering) "Crea Account" else "TrailQuest Login",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
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
package com.example.loadassist.ui_

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.functions.functions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserScreen(
    onNavigateBack: () -> Unit
) {
    var employeeNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    
    val roles = listOf("Worker", "Manager")
    var selectedRole by remember { mutableStateOf(roles[0]) }
    
    val context = LocalContext.current
    val functions = Firebase.functions
    val auth = Firebase.auth

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register New User") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Account Credentials",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = employeeNumber,
                onValueChange = { employeeNumber = it },
                label = { Text("Employee Number") },
                placeholder = { Text("e.g. 123456") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Temporary Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Select User Role",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Column(Modifier.selectableGroup()) {
                roles.forEach { role ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (role == selectedRole),
                                onClick = { selectedRole = role },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (role == selectedRole),
                            onClick = null
                        )
                        Text(
                            text = role,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val trimmed = employeeNumber.trim()
                    if (trimmed.length < 4 || password.length < 6) {
                        Toast.makeText(context, "Invalid ID or Password", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val currentUser = auth.currentUser
                    if (currentUser == null) {
                        Toast.makeText(context, "Authentication error: No user signed in", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    isSaving = true
                    
                    // Force refresh token and check claims
                    currentUser.getIdToken(true).addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            val result = tokenTask.result
                            val isManager = result?.claims?.get("isManager") as? Boolean ?: false
                            val callerEmail = currentUser.email ?: ""
                            val callerEmpNumber = callerEmail.substringBefore("@")
                            
                            Log.d("AddUserScreen", "Caller: $callerEmpNumber, isManager: $isManager")

                            // Prepare data for the Cloud Function
                            val data = hashMapOf(
                                "employeeNumber" to trimmed,
                                "password" to password,
                                "role" to selectedRole,
                                "callerId" to callerEmpNumber // Pass this just in case the function can use it
                            )

                            // CALL THE CLOUD FUNCTION
                            functions
                                .getHttpsCallable("registerNewUser")
                                .call(data)
                                .addOnSuccessListener {
                                    isSaving = false
                                    Toast.makeText(context, "User registered successfully!", Toast.LENGTH_LONG).show()
                                    
                                    // Refresh token for the current user in case they promoted themselves
                                    // or just to ensure the token is up to date after the admin action.
                                    currentUser.getIdToken(true).addOnCompleteListener { refreshTask ->
                                        if (refreshTask.isSuccessful) {
                                            Log.d("Auth", "Token refreshed successfully after user creation")
                                        } else {
                                            Log.e("Auth", "Token refresh failed after user creation", refreshTask.exception)
                                        }
                                    }

                                    onNavigateBack()
                                }
                                .addOnFailureListener { e ->
                                    isSaving = false
                                    Log.e("AddUserScreen", "Cloud Function Error", e)
                                    val errorMsg = if (e.message?.contains("logged in") == true && callerEmpNumber == "148596") {
                                        "Master Admin $callerEmpNumber is logged in, but the server rejected the token. Try signing out and back in."
                                    } else {
                                        e.message ?: "Unknown Cloud Error"
                                    }
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                }
                        } else {
                            isSaving = false
                            Toast.makeText(context, "Failed to refresh auth token", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("CREATE USER ACCOUNT")
                }
            }
        }
    }
}

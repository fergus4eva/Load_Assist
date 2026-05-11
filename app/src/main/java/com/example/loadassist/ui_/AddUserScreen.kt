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
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.functions

private const val FUNCTION_REGION = "us-central1"
private const val FUNCTION_NAME = "registerNewUser" 

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserScreen(
    onNavigateBack: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var employeeNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    
    val roles = listOf("Worker", "Manager")
    var selectedRole by remember { mutableStateOf(roles[0]) }
    
    val context = LocalContext.current
    val auth = remember { Firebase.auth }
    val functions = remember { Firebase.functions(FUNCTION_REGION) }

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
            Text(text = "Account Credentials", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                placeholder = { Text("e.g. Robert Smith") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
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
                        Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
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
                    val user = auth.currentUser
                    if (user == null) {
                        Toast.makeText(context, "Local Auth Error: User is null", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (fullName.isBlank() || employeeNumber.length < 4 || password.length < 6) {
                        Toast.makeText(context, "Please fill in all fields correctly (Password min 6 chars)", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSaving = true
                    
                    user.getIdToken(true).addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            val idToken = tokenTask.result?.token
                            Log.d("AddUserScreen", "Token refresh success. Token length: ${idToken?.length}")
                            
                            val data = hashMapOf(
                                "fullName" to fullName.trim(),
                                "employeeNumber" to employeeNumber.trim(),
                                "password" to password,
                                "role" to selectedRole.lowercase(),
                                "callerId" to (user.email?.substringBefore("@") ?: "")
                            )

                            functions
                                .getHttpsCallable(FUNCTION_NAME)
                                .call(data)
                                .addOnSuccessListener { result ->
                                    isSaving = false
                                    Toast.makeText(context, "User $fullName Created!", Toast.LENGTH_LONG).show()
                                    onNavigateBack()
                                }
                                .addOnFailureListener { e ->
                                    isSaving = false
                                    Log.e("AddUserScreen", "Cloud Function Error", e)
                                    val errorMsg = if (e is FirebaseFunctionsException) {
                                        "Error: ${e.message}"
                                    } else {
                                        "Network Error: ${e.localizedMessage}"
                                    }
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                }
                        } else {
                            isSaving = false
                            Log.e("AddUserScreen", "Token Fetch Failed", tokenTask.exception)
                            Toast.makeText(context, "Authentication verification failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isSaving
            ) {
                if (isSaving) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                else Text("CREATE ACCOUNT")
            }
        }
    }
}

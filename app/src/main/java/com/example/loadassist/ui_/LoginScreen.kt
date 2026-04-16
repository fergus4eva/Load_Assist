package com.example.loadassist.ui_

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.loadassist.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun Login(onLoginSuccess: () -> Unit, modifier: Modifier = Modifier) {
    var employeeNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val auth = Firebase.auth

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .padding(bottom = 32.dp)
        )
        Text(text = "Employee Number:")
        TextField(
            value = employeeNumber,
            onValueChange = { employeeNumber = it },
            label = { Text("Enter number") },
            enabled = !isLoading,
            singleLine = true
        )
        Text(text = "Password:", modifier = Modifier.padding(top = 8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Enter password") },
            enabled = !isLoading,
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else {
            Button(
                onClick = {
                    val trimmedNumber = employeeNumber.trim()
                    if (trimmedNumber.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    isLoading = true
                    // Map employee number to a unique fake email for Firebase Auth
                    val email = "${trimmedNumber}@loadassist.com"
                    
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                onLoginSuccess()
                            } else {
                                val message = task.exception?.message ?: "Login failed"
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        }
                },
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text(text = "Login")
            }
        }
    }
}

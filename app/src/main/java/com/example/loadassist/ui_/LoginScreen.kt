package com.example.loadassist.ui_

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import com.example.loadassist.R
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@Composable
fun Login(onLoginSuccess: () -> Unit, modifier: Modifier = Modifier) {
    //employee number for login
    var employeeNumber by remember { mutableStateOf("") }
    //employee password for login
    var password by remember { mutableStateOf("") }
    //isLoading boolean to show loading spinner -
    var isLoading by remember { mutableStateOf(false) }
    //local context variable to show toast messages
    // (error messages or prompts that appear on the screen)
    val context = LocalContext.current
    //our datebase variable to Firebase Firestore
    val db = Firebase.firestore

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(250.dp)
        )
        Text(text = "Employee Number:")
        TextField(
            value = employeeNumber,
            onValueChange = { employeeNumber = it },
            label = { Text("Enter number") },
            enabled = !isLoading
        )
        Text(text = "Password:", modifier = Modifier.padding(top = 8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Enter password") },
            enabled = !isLoading
        )
        //our conditional to show spinner or login button
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }
        else {
            Button(
                onClick = {
                    //a nice message for the user to show that they need to do something
                    if (employeeNumber.isBlank() || password.isBlank()) {
                        //toast is a message prompt, showing the error message
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    //is Loading True to show spinner -while accessing the database
                    isLoading = true
                    //accessing database
                    db.collection("employees")
                        .document(employeeNumber)
                        .get()
                        .addOnSuccessListener { document ->
                            isLoading = false
                            if (document.exists()) {
                                val dbPassword = document.getString("password")
                                if (dbPassword == password) {
                                    onLoginSuccess()
                                } else {
                                    Toast.makeText(context, "Incorrect password", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Employee not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            isLoading = false
                            //toast is a message prompt, showing the error message
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Login")
            }
        }
    }
}

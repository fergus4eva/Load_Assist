package com.example.loadassist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.loadassist.ui.theme.LoadAssistTheme
import com.example.loadassist.ui_.Login
import com.example.loadassist.ui_.WorkerMenuScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


//this enum class is used for navigation purposes, cited from
//https://developer.android.com/codelabs/basic-android-kotlin-compose-navigation#3
enum class LoadAssistScreen {
    START,
    WORKERMENU,
    MANUAL_INPUT,
}

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase Auth
        auth = Firebase.auth
        enableEdgeToEdge()
        setContent {
            LoadAssistTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = LoadAssistScreen.START.name,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(route = LoadAssistScreen.START.name) {
                            Login(
                                onLoginSuccess = {
                                    navController.navigate(LoadAssistScreen.WORKERMENU.name)
                                }
                            )
                        }
                        composable(route = LoadAssistScreen.WORKERMENU.name) {
                            WorkerMenuScreen(
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

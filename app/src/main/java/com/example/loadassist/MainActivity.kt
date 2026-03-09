package com.example.loadassist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.loadassist.ui.theme.LoadAssistTheme
import com.example.loadassist.ui_.Login
import com.example.loadassist.ui_.WorkerMenuScreen


//this enum class is used for navigation purposes, cited from
//https://developer.android.com/codelabs/basic-android-kotlin-compose-navigation#3
enum class LoadAssistScreen {
    START,
    WORKERMENU
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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


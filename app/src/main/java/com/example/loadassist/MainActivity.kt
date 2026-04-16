package com.example.loadassist

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.loadassist.ui.theme.LoadAssistTheme
import com.example.loadassist.ui_.AddProductScreen
import com.example.loadassist.ui_.AddUserScreen
import com.example.loadassist.ui_.FinishedLoadScreen
import com.example.loadassist.ui_.LoadPlanScreen
import com.example.loadassist.ui_.Login
import com.example.loadassist.ui_.ManagerMenuScreen
import com.example.loadassist.ui_.ManualInputScreen
import com.example.loadassist.ui_.ManualInputViewModel
import com.example.loadassist.ui_.ProductDirectoryScreen
import com.example.loadassist.ui_.ReceivingScreen
import com.example.loadassist.ui_.RunnerOrganizationScreen
import com.example.loadassist.ui_.ScannerScreen
import com.example.loadassist.ui_.WorkerMenuScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


//this enum class is used for navigation purposes, cited from
//https://developer.android.com/codelabs/basic-android-kotlin-compose-navigation#3
enum class LoadAssistScreen {
    START,
    WORKERMENU,
    MANAGERMENU,
    MANUAL_INPUT,
    LOAD_PLAN,
    RECEIVING,
    SCANNER,
    RUNNER_BUILDS,
    PRODUCT_DIRECTORY,
    ADD_PRODUCT,
    FINISHED_LOAD,
    ADD_USER
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
                
                // Create the shared ViewModel here
                val sharedViewModel: ManualInputViewModel = viewModel()
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = LoadAssistScreen.START.name,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(route = LoadAssistScreen.START.name) {
                            Login(
                                onLoginSuccess = {
                                    val currentUser = auth.currentUser
                                    val email = currentUser?.email ?: ""
                                    val empNumber = email.substringBefore("@")
                                    
                                    // Navigate to Manager Menu if ID is 148596, otherwise Worker Menu
                                    if (empNumber == "148596") {
                                        navController.navigate(LoadAssistScreen.MANAGERMENU.name)
                                    } else {
                                        navController.navigate(LoadAssistScreen.WORKERMENU.name)
                                    }
                                }
                            )
                        }
                        composable(route = LoadAssistScreen.MANAGERMENU.name) {
                            ManagerMenuScreen(
                                onProductDirectoryClick = {
                                    navController.navigate(LoadAssistScreen.PRODUCT_DIRECTORY.name)
                                },
                                onAddProductClick = {
                                    navController.navigate(LoadAssistScreen.ADD_PRODUCT.name)
                                },
                                onAddUserClick = {
                                    navController.navigate(LoadAssistScreen.ADD_USER.name)
                                },
                                onViewReportsClick = {
                                    // TODO: Implement Reports Screen
                                    Toast.makeText(this@MainActivity, "Reports Coming Soon", Toast.LENGTH_SHORT).show()
                                },
                                onWorkerMenuClick = {
                                    navController.navigate(LoadAssistScreen.WORKERMENU.name)
                                }
                            )
                        }
                        composable(route = LoadAssistScreen.WORKERMENU.name) {
                            WorkerMenuScreen(
                                modifier = Modifier.fillMaxSize(),
                                onManualInputClick = {
                                    navController.navigate(LoadAssistScreen.MANUAL_INPUT.name)
                                },
                                onProductDirectoryClick = {
                                    navController.navigate(LoadAssistScreen.PRODUCT_DIRECTORY.name)
                                },
                                onAddProductClick = {
                                    navController.navigate(LoadAssistScreen.ADD_PRODUCT.name)
                                }
                            )
                        }
                        composable(route = LoadAssistScreen.MANUAL_INPUT.name) {
                            ManualInputScreen(
                                modifier = Modifier.fillMaxSize(),
                                viewModel = sharedViewModel,
                                onLoadPlanClick = {
                                    navController.navigate(LoadAssistScreen.LOAD_PLAN.name)
                                }
                            )
                        }
                        composable(route = LoadAssistScreen.LOAD_PLAN.name) {
                            LoadPlanScreen(
                                modifier = Modifier.fillMaxSize(),
                                viewModel = sharedViewModel,
                                onBeginReceivingClick = {
                                    navController.navigate(LoadAssistScreen.RECEIVING.name)
                                },
                                onRunnerBuildsClick = {
                                    navController.navigate(LoadAssistScreen.RUNNER_BUILDS.name)
                                }
                            )
                        }
                        composable(route = LoadAssistScreen.RECEIVING.name) {
                            ReceivingScreen(
                                modifier = Modifier.fillMaxSize(),
                                viewModel = sharedViewModel,
                                onScanClick = {
                                    navController.navigate(LoadAssistScreen.SCANNER.name)
                                },
                                onRunnerBuildsClick = {
                                    navController.navigate(LoadAssistScreen.RUNNER_BUILDS.name)
                                },
                                onFinishClick = {
                                    navController.navigate(LoadAssistScreen.FINISHED_LOAD.name)
                                }
                            )
                        }
                        composable(route = LoadAssistScreen.RUNNER_BUILDS.name) {
                            RunnerOrganizationScreen(
                                modifier = Modifier.fillMaxSize(),
                                viewModel = sharedViewModel
                            )
                        }
                        composable(route = LoadAssistScreen.PRODUCT_DIRECTORY.name) {
                            ProductDirectoryScreen(
                                modifier = Modifier.fillMaxSize(),
                                viewModel = sharedViewModel,
                                onAddProductClick = {
                                    navController.navigate(LoadAssistScreen.ADD_PRODUCT.name)
                                }
                            )
                        }
                        composable(route = LoadAssistScreen.ADD_PRODUCT.name) {
                            AddProductScreen(
                                viewModel = sharedViewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(route = LoadAssistScreen.ADD_USER.name) {
                            AddUserScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(route = LoadAssistScreen.FINISHED_LOAD.name) {
                            FinishedLoadScreen(
                                viewModel = sharedViewModel,
                                onReturnHome = {
                                    navController.navigate(LoadAssistScreen.WORKERMENU.name) {
                                        popUpTo(LoadAssistScreen.WORKERMENU.name) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(route = LoadAssistScreen.SCANNER.name) {
                            ScannerScreen(
                                onBarcodeDetected = { barcode ->
                                    val wasFound = sharedViewModel.onBarcodeScanned(barcode)
                                    if (wasFound) {
                                        Toast.makeText(this@MainActivity, "Item Found!", Toast.LENGTH_SHORT).show()
                                        // Use popBackStack to return to the Receiving screen
                                        navController.popBackStack()
                                    } else {
                                        Toast.makeText(this@MainActivity, "Unknown Barcode: $barcode", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onClose = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

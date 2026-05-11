package com.example.loadassist.ui_
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loadassist.R
import com.example.loadassist.ui.theme.LoadAssistTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore


/**
 * Composable that allows the user to select the desired cupcake quantity and expects
 * [onNextButtonClicked] lambda that expects the selected quantity and triggers the navigation to
 * next screen
 */
@Composable
fun WorkerMenuScreen(
    modifier: Modifier = Modifier,
    onManualInputClick: () -> Unit = {},
    onProductDirectoryClick: () -> Unit = {},
    onAddProductClick: () -> Unit = {},
    onBackToManagerClick: () -> Unit = {}
) {
    val auth = Firebase.auth
    val db = Firebase.firestore
    var isManager by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("") }

    // Check if user has manager role/ID and fetch name
    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser
        val email = currentUser?.email ?: ""
        val empNumber = email.substringBefore("@")
        
        // Initial check based on ID
        if (empNumber == "148596") {
            isManager = true
            userName = "Admin"
        } else {
            // Also check custom claims for promoted managers
            currentUser?.getIdToken(false)?.addOnSuccessListener { result ->
                if (result.claims["isManager"] == true) {
                    isManager = true
                }
            }
        }

        // Fetch user's name from Firestore
        if (empNumber.isNotEmpty()) {
            db.collection("users").document(empNumber).get()
                .addOnSuccessListener { document ->
                    val fullName = document.getString("fullName")
                    if (!fullName.isNullOrBlank()) {
                        // Extract first name for a friendly greeting
                        userName = fullName.split(" ").firstOrNull() ?: fullName
                    }
                }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Manager Back Button (Top Bar style)
        if (isManager) {
            Button(
                onClick = onBackToManagerClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = MaterialTheme.shapes.small
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("RETURN TO MANAGER PORTAL", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Added Logo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "LoadAssist Logo",
            modifier = Modifier
                .size(150.dp)
                .padding(top = 16.dp)
        )

        // Updated Greeting with Name
        Text(
            text = if (userName.isNotEmpty()) "Welcome To Load/Assist, $userName!" else "Welcome, Team Member!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            textAlign = TextAlign.Center
        )

        Image(
            painter = painterResource(id = R.drawable.menu_image),
            contentDescription = "Menu Illustration",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(if (isManager) 300.dp else 350.dp)
        )
        
        Text(
            text = "Please choose ASN File to upload or choose to input invoice manually",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Branded Buttons (Styled to match logo font look)
        Button(
            onClick = { /* TODO: Implement ASN Upload */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = "UPLOAD ASN",
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        Button(
            onClick = onManualInputClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = "INPUT LOAD INVOICE",
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
        
        Button(
            onClick = onProductDirectoryClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = "PRODUCT DIRECTORY",
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
        
        Button(
            onClick = onAddProductClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = "ADD NEW PRODUCT",
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showSystemUi = true)
@Composable
fun WorkerMenuPreview() {
    LoadAssistTheme {
        WorkerMenuScreen()
    }
}

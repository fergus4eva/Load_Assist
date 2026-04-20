package com.example.loadassist.ui_
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.example.loadassist.R
import com.example.loadassist.ui.theme.LoadAssistTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth


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
    var isManager by remember { mutableStateOf(false) }

    // Check if user has manager role/ID
    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser
        val email = currentUser?.email ?: ""
        val empNumber = email.substringBefore("@")
        
        // Initial check based on ID
        if (empNumber == "148596") {
            isManager = true
        } else {
            // Also check custom claims for promoted managers
            currentUser?.getIdToken(false)?.addOnSuccessListener { result ->
                if (result.claims["isManager"] == true) {
                    isManager = true
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
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

        Image(
            painter = painterResource(id = R.drawable.menu_image),
            contentDescription = "Logo",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.size(if (isManager) 440.dp else 500.dp)
        )
        Text(
            text =
                "Please choose ASN File to upload or choose to input invoice manually",
                textAlign = TextAlign.Center,
            modifier = Modifier.padding(10.dp)

        )
        Button(
            onClick = { /* TODO: Implement ASN Upload */ },
            modifier = Modifier.padding(10.dp)
        )
        {
            Text(text = "UPLOAD ASN")
        }
        Button(
            onClick = onManualInputClick,
            modifier = Modifier.padding(10.dp)
        )
        {
            Text(text = "Manually Input Load Items and Quantities")
        }
        Button(
            onClick = onProductDirectoryClick,
            modifier = Modifier.padding(10.dp)
        )
        {
            Text(text = "PRODUCT DIRECTORY")
        }
        Button(
            onClick = onAddProductClick,
            modifier = Modifier.padding(10.dp)
        )
        {
            Text(text = "ADD NEW PRODUCT")
        }


    }
}

@Preview(showSystemUi = true)
@Composable
fun WorkerMenuPreview() {
    LoadAssistTheme {
        WorkerMenuScreen()
    }
}

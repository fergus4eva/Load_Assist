package com.example.loadassist.ui_

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.loadassist.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerMenuScreen(
    onProductDirectoryClick: () -> Unit = {},
    onAddProductClick: () -> Unit = {},
    onAddUserClick: () -> Unit = {},
    onViewReportsClick: () -> Unit = {},
    onWorkerMenuClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Manager Portal", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(250.dp)
                    .padding(bottom = 24.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "Administrative Controls",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Manager Actions
            ManagerMenuButton(
                text = "Product Inventory Directory",
                icon = Icons.Default.Inventory,
                onClick = onProductDirectoryClick
            )

            ManagerMenuButton(
                text = "Register New Item",
                icon = Icons.Default.Inventory, // Could be specialized icon
                onClick = onAddProductClick
            )

            ManagerMenuButton(
                text = "Add New User",
                icon = Icons.Default.PersonAdd,
                onClick = onAddUserClick
            )

            ManagerMenuButton(
                text = "View Receiving Reports",
                icon = Icons.Default.Assessment,
                onClick = onViewReportsClick
            )

            Spacer(modifier = Modifier.weight(1f))

            // Switch to Worker View
            OutlinedButton(
                onClick = onWorkerMenuClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.People, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("SWITCH TO WORKER INTERFACE")
            }
        }
    }
}

@Composable
fun ManagerMenuButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(64.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

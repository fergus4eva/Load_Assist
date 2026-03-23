package com.example.loadassist.ui_

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@Preview(name = "manualscreenpreview", showSystemUi = true)
@Composable
fun ManualInputScreen(modifier: Modifier = Modifier) {
    // Category States - mutableStateof
    var categoryList by remember { mutableStateOf(listOf<String>()) }
    var selectedCategory by remember { mutableStateOf("Select a Category") }
    var categoryExpanded by remember { mutableStateOf(false) }

    // Item States
    var itemList by remember { mutableStateOf(listOf<String>()) }
    var selectedItem by remember { mutableStateOf("Select an Item") }
    var itemExpanded by remember { mutableStateOf(false) }

    // Instance of our database
    val db = Firebase.firestore

    // Get Categories from Firestore once
    LaunchedEffect(Unit) {
        db.collection("category")
            .get()
            .addOnSuccessListener { result ->
                categoryList = result.documents.mapNotNull { it.getString("name") }
            }
    }

    // Fetch Items when selectedCategory changes
    LaunchedEffect(selectedCategory) {
        if (selectedCategory != "Select a Category") {
            db.collection("items")
                .whereEqualTo("category", selectedCategory)
                .get()
                .addOnSuccessListener { result ->
                    itemList = result.documents.mapNotNull { it.getString("name") }
                    selectedItem = "Select an Item"
                }
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        //HEADER HEADLINE
        Text(
            text = "Manual Input",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        HorizontalDivider()

        // ----- CATEGORY SELECTION -----
        Text(text = "Choose Category", modifier = Modifier.padding(top = 8.dp))
        
        Box(modifier = Modifier.padding(vertical = 8.dp)) {
            // Use a Row to put text next to the IconButton
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { categoryExpanded = !categoryExpanded }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Choose Category")
                }
                // Text showing current selection
                Text(
                    text = selectedCategory,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            DropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                categoryList.forEach { categoryName ->
                    DropdownMenuItem(
                        text = { Text(categoryName) },
                        onClick = {
                            selectedCategory = categoryName
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        // ----- ITEM SELECTION -----
        Text(text = "Choose Item", modifier = Modifier.padding(top = 16.dp))

        Box(modifier = Modifier.padding(vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { itemExpanded = !itemExpanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Choose Item")
                }
                Text(
                    text = selectedItem,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp).
                )
            }

            DropdownMenu(
                expanded = itemExpanded,
                onDismissRequest = { itemExpanded = false }
            ) {
                itemList.forEach { itemName ->
                    DropdownMenuItem(
                        text = { Text(itemName) },
                        onClick = {
                            selectedItem = itemName
                            itemExpanded = false
                        }
                    )
                }
            }
        }
    }
}

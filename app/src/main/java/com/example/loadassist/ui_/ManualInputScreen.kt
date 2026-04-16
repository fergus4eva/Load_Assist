package com.example.loadassist.ui_

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ManualInputScreen(
    modifier: Modifier = Modifier,
    onLoadPlanClick: () -> Unit = {},
    viewModel: ManualInputViewModel = viewModel()
) {
    //update our state variables from the viewmodel to UI
    val categories by viewModel.categories.collectAsState()
    val invoice by viewModel.invoice.collectAsState()
    val refreshTrigger by viewModel.refreshTrigger.collectAsState()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Manual Invoice Input",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        HorizontalDivider()

        // Give the LazyColumn weight(1f) to take all available space above the footer
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // 1. List all categories
            items(categories) { category ->
                CategoryExpandableItem(
                    categoryName = category,
                    viewModel = viewModel
                )
            }

            // 2. Push the Invoice Preview to the bottom of the scrollable list
            item {
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Current Invoice Preview",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp, max = 300.dp) // Dynamic height constraints
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    val invoiceText = remember(refreshTrigger) {
                        val sb = StringBuilder()
                        if (invoice.totalQuantity == 0) {
                            sb.append("No items added yet.")
                        } else {
                            invoice.forEach { item ->
                                sb.append("• ${item.getlineItem()} (${item.getCategory()}) QUANTITY: ${item.getQuantity()}\n")
                            }
                        }
                        sb.toString()
                    }

                    Text(
                        text = invoiceText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            }
        }

        // Summary Footer - stays fixed at the very bottom
        if (refreshTrigger >= 0) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Items:",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${invoice.totalQuantity}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Button(
            onClick = onLoadPlanClick,
            modifier = Modifier.fillMaxWidth()
        )
        {
            Text(text = "LOAD INPUT COMPLETE")
        }

    }
}

@Composable
fun CategoryExpandableItem(
    categoryName: String,
    viewModel: ManualInputViewModel
) {
    val isExpanded = viewModel.expandedCategories[categoryName] ?: false
    val itemsList = viewModel.categoryItems[categoryName] ?: emptyList()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleCategoryExpansion(categoryName) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    if (itemsList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    } else {
                        itemsList.forEach { itemName ->
                            ItemRow(
                                itemName = itemName,
                                category = categoryName,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemRow(
    itemName: String,
    category: String,
    viewModel: ManualInputViewModel
) {
    val refreshTrigger by viewModel.refreshTrigger.collectAsState()
    val currentQty = remember(refreshTrigger) {
        viewModel.getItemQuantity(itemName, category)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = itemName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.removeItem(itemName, category) }) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease")
            }

            Text(
                text = currentQty.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            IconButton(onClick = { viewModel.addItem(itemName, category) }) {
                Icon(Icons.Default.Add, contentDescription = "Increase")
            }
        }
    }
}

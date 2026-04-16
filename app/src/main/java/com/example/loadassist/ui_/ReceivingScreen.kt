package com.example.loadassist.ui_

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.loadassist.objects.lineItems

@Composable
fun ReceivingScreen(
    modifier: Modifier = Modifier,
    viewModel: ManualInputViewModel = viewModel(),
    onScanClick: () -> Unit = {},
    onRunnerBuildsClick: () -> Unit = {},
    onFinishClick: () -> Unit = {}
) {
    val invoice by viewModel.invoice.collectAsState()
    val refreshTrigger by viewModel.refreshTrigger.collectAsState()
    val scannedItems = viewModel.scannedItems
    
    // Local expansion state for this screen
    val expandedCategories = remember { mutableStateMapOf<String, Boolean>() }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Receiving Load",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        HorizontalDivider()

        if (refreshTrigger >= 0) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categoryMap = invoice.itemsMap
                
                items(categoryMap.keys.toList()) { category ->
                    val itemsInCategory = categoryMap[category]?.toList() ?: emptyList()
                    val isExpanded = expandedCategories[category] ?: false
                    
                    ReceivingCategoryExpandableItem(
                        categoryName = category,
                        items = itemsInCategory,
                        isExpanded = isExpanded,
                        onToggleExpand = { expandedCategories[category] = !isExpanded },
                        scannedItems = scannedItems,
                        onIncrement = { item -> viewModel.incrementScannedItem(item) },
                        onDecrement = { item -> viewModel.decrementScannedItem(item) }
                    )
                }
            }
        }

        // Summary Progress
        val totalToScan = invoice.totalQuantity
        val totalScanned = scannedItems.values.sum()
        
        // Logical check for missing products when scanning is partially done
        val missingProduct = totalScanned > 0 && totalScanned < totalToScan
        
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Overall Progress", style = MaterialTheme.typography.titleMedium)
                    Text(text = "$totalScanned / $totalToScan items scanned")
                    if (missingProduct) {
                        Text(
                            text = "Note: Some items are missing",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                CircularProgressIndicator(
                    progress = { if (totalToScan > 0) totalScanned.toFloat() / totalToScan else 0f },
                    modifier = Modifier.size(40.dp),
                )
            }
        }

        // Navigation to Runner Builds
        Button(
            onClick = onRunnerBuildsClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text(text = "VIEW RUNNER PROGRESS")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // General Scan Button
        Button(
            onClick = onScanClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(text = "OPEN CAMERA SCANNER")
        }
        
        // Finish Button
        Button(
            onClick = { 
                viewModel.finishReceiving {
                    onFinishClick()
                }
            },
            enabled = (totalScanned == totalToScan || missingProduct) && totalToScan > 0,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text(text = if (missingProduct) "FINISH WITH MISSING ITEMS" else "FINISH RECEIVING")
        }
    }
}

@Composable
fun ReceivingCategoryExpandableItem(
    categoryName: String,
    items: List<lineItems>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    scannedItems: Map<lineItems, Int>,
    onIncrement: (lineItems) -> Unit,
    onDecrement: (lineItems) -> Unit
) {
    val categoryTotal = items.sumOf { it.quantity }
    val categoryScanned = items.sumOf { scannedItems[it] ?: 0 }
    val isCategoryComplete = categoryScanned >= categoryTotal && categoryTotal > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCategoryComplete) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$categoryScanned / $categoryTotal items",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                if (isCategoryComplete) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    items.forEach { item ->
                        val count = scannedItems[item] ?: 0
                        ReceivingItemRow(
                            item = item,
                            scannedCount = count,
                            onIncrement = { onIncrement(item) },
                            onDecrement = { onDecrement(item) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ReceivingItemRow(
    item: lineItems,
    scannedCount: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val isComplete = scannedCount >= item.quantity
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isComplete) Color(0xFFF1F8E9) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isComplete) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isComplete) Color(0xFF4CAF50) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    text = item.getlineItem(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.getBrand(),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$scannedCount / ${item.quantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isComplete) Color(0xFF2E7D32) else Color.Unspecified,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    TextButton(
                        onClick = onIncrement, 
                        enabled = !isComplete,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(text = "SCAN", fontSize = 12.sp)
                    }
                    TextButton(
                        onClick = onDecrement, 
                        enabled = scannedCount > 0,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(text = "REMOVE", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

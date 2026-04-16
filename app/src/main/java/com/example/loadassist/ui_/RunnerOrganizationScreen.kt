package com.example.loadassist.ui_

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.loadassist.objects.lineItems

@Composable
fun RunnerOrganizationScreen(
    modifier: Modifier = Modifier,
    viewModel: ManualInputViewModel = viewModel()
) {
    val invoice by viewModel.invoice.collectAsState()
    val runnerMap = remember(invoice) { invoice.getItemsByRunner() }
    val expandedRunners = remember { mutableStateMapOf<Int, Boolean>() }
    val scannedItems = viewModel.scannedItems
    val refreshTrigger by viewModel.refreshTrigger.collectAsState()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Runner Organization",
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
                items(runnerMap.keys.toList()) { runnerId ->
                    val itemsForRunner = runnerMap[runnerId]?.toList() ?: emptyList()
                    val isExpanded = expandedRunners[runnerId] ?: false

                    RunnerExpandableItem(
                        runnerId = runnerId,
                        items = itemsForRunner,
                        isExpanded = isExpanded,
                        onToggleExpand = { expandedRunners[runnerId] = !isExpanded },
                        scannedItems = scannedItems
                    )
                }
            }
        }
    }
}

@Composable
fun RunnerExpandableItem(
    runnerId: Int,
    items: List<lineItems>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    scannedItems: Map<lineItems, Int>
) {
    val totalQty = items.sumOf { it.quantity }
    val scannedQty = items.sumOf { scannedItems[it] ?: 0 }
    val isComplete = scannedQty >= totalQty && totalQty > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isComplete) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.tertiaryContainer
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Runner $runnerId",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (isComplete) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.padding(start = 8.dp).size(20.dp)
                            )
                        }
                    }
                    Text(
                        text = "$scannedQty / $totalQty items loaded",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    if (totalQty > 0) {
                        LinearProgressIndicator(
                            progress = { scannedQty.toFloat() / totalQty.toFloat() },
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            color = if (isComplete) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(16.dp)) {
                    items.forEach { item ->
                        val count = scannedItems[item] ?: 0
                        RunnerItemRow(item, count)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun RunnerItemRow(item: lineItems, scannedCount: Int) {
    val isItemComplete = scannedCount >= item.getQuantity()
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Reference Image
        if (item.getImageUrl().isNotEmpty()) {
            AsyncImage(
                model = item.getImageUrl(),
                contentDescription = item.getlineItem(),
                modifier = Modifier
                    .size(60.dp)
                    .padding(end = 12.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            // Placeholder if no image
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .padding(end = 12.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No Image", style = MaterialTheme.typography.labelSmall)
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.getlineItem(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (isItemComplete) Color(0xFF2E7D32) else Color.Unspecified
            )
            Text(
                text = "${item.getBrand()} - ${item.getCategory()}",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$scannedCount / ${item.getQuantity()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isItemComplete) Color(0xFF2E7D32) else Color.Unspecified
            )
            if (isItemComplete) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

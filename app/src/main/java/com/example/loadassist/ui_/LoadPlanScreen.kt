package com.example.loadassist.ui_

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.loadassist.objects.Invoice
import com.example.loadassist.objects.lineItems

@Composable
fun LoadPlanScreen(
    modifier: Modifier = Modifier,
    onBeginReceivingClick: () -> Unit = {},
    onRunnerBuildsClick : ()  -> Unit = {},
    viewModel: ManualInputViewModel = viewModel()
) {
    val invoice by viewModel.invoice.collectAsState()
    // Local expansion state for this screen
    val expandedCategories = remember { mutableStateMapOf<String, Boolean>() }
    //header title
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Load Plan Details",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        HorizontalDivider()

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Load Summary Card with info
            item {
                LoadSummaryCard(invoice)
            }

            // Categories Quantities information
            item {
                Text(
                    text = "Category Breakdown",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            val categoryMap = invoice.itemsMap
            // Sort categories by total quantity (least to greatest)
            val sortedCategories = categoryMap.keys.toList().sortedBy { category ->
                categoryMap[category]?.sumOf { it.quantity } ?: 0
            }
            //data list
            items(sortedCategories) { category ->
                val list = categoryMap[category]
                val itemsInCategory = list?.toList() ?: emptyList()
                val isExpanded = expandedCategories[category] ?: false
                
                ExpandableCategoryPlanItem(
                    categoryName = category,
                    items = itemsInCategory,
                    isExpanded = isExpanded,
                    onToggleExpand = { expandedCategories[category] = !isExpanded }
                )
            }
        }
        //button that navigates to recieving screen
        Button(
            onClick = { onBeginReceivingClick() },
            modifier = Modifier.fillMaxWidth()
        )
        {
            Text(text = "BEGIN RECEIVING LOAD")
        }
    }
}

@Composable
fun ExpandableCategoryPlanItem(
    categoryName: String,
    items: List<lineItems>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val totalQty = items.sumOf { it.quantity }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$totalQty items total",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = item.getlineItem(), style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "x${item.quantity}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadSummaryCard(invoice: Invoice) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Load Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Invoice #:")
                Text(text = invoice.invoiceNumber, fontWeight = FontWeight.Medium)
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Invoice Date:")
            Text(text = "${invoice.formattedDate}", fontWeight = FontWeight.Bold)
        }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Total Items:")
                Text(text = "${invoice.totalQuantity}", fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Unique Categories:")
                Text(text = "${invoice.categoryCount}", fontWeight = FontWeight.Medium)
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Largest Category:")
                Text(text = "${invoice.getLargestCategoryInfo()}", fontWeight = FontWeight.Medium)
            }
        }
    }
}

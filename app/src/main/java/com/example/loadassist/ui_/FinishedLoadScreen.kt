package com.example.loadassist.ui_

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun FinishedLoadScreen(
    onReturnHome: () -> Unit,
    viewModel: ManualInputViewModel = viewModel()
) {
    val report by viewModel.receivingReport.collectAsState()

    if (report == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val completionRate = report!!["completionRate"] as Float
    val totalExpected = report!!["totalExpected"] as Int
    val totalReceived = report!!["totalReceived"] as Int
    val missingItems = report!!["missingItems"] as List<Map<String, Any>>
    val invoiceNumber = report!!["invoiceNumber"] as String

    // Animation States
    val infiniteTransition = rememberInfiniteTransition(label = "ring")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val scaleState = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scaleState.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated Status Indicator
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(150.dp).padding(top = 16.dp)
        ) {
            val statusColor = if (completionRate >= 100f) Color(0xFF4CAF50) else Color(0xFFFF9800)
            
            // Rotating dashed ring
            Canvas(modifier = Modifier.fillMaxSize().scale(pulse)) {
                drawCircle(
                    color = statusColor,
                    style = Stroke(
                        width = 4.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(40f, 20f), rotation)
                    ),
                    alpha = 0.4f
                )
            }
            
            Icon(
                imageVector = if (completionRate >= 100f) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(100.dp).scale(scaleState.value)
            )
        }

        Text(
            text = if (completionRate >= 100f) "Load Completed!" else "Load Partially Received",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Summary for Invoice #:", style = MaterialTheme.typography.labelLarge)
                Text(text = invoiceNumber, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = "Expected", style = MaterialTheme.typography.labelSmall)
                        Text(text = "$totalExpected items", style = MaterialTheme.typography.titleMedium)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Received", style = MaterialTheme.typography.labelSmall)
                        Text(text = "$totalReceived items", style = MaterialTheme.typography.titleMedium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Completion Rate", style = MaterialTheme.typography.labelSmall)
                LinearProgressIndicator(
                    progress = { completionRate / 100f },
                    modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                    color = if (completionRate >= 100f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${completionRate.toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                )
            }
        }

        if (missingItems.isNotEmpty()) {
            Text(
                text = "Missing Items Detail",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Start).padding(top = 24.dp, bottom = 8.dp)
            )
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(missingItems) { item ->
                    ListItem(
                        headlineContent = { Text(item["name"] as String, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("Short by ${item["missing"]} units") },
                        trailingContent = {
                            Text(
                                text = "${item["received"]} / ${item["expected"]}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color(0xFFFFF3E0))
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Perfect match! No items are missing.",
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(top = 32.dp)
            )
        }

        Text(
            text = "Report has been filed successfully in the cloud.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 16.dp),
            textAlign = TextAlign.Center
        )

        Button(
            onClick = {
                viewModel.resetReceiving()
                onReturnHome()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(Icons.Default.Home, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("RETURN TO MAIN MENU")
        }
    }
}

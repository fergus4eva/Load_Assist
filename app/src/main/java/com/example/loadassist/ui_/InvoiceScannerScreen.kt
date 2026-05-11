package com.example.loadassist.ui_

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors

@Composable
fun InvoiceScannerScreen(
    onTextDetected: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    if (hasCameraPermission) {
        InvoiceScannerView(onTextDetected, onClose)
    } else {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Camera permission required", color = Color.White, modifier = Modifier.padding(16.dp))
                Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) { Text("Grant Permission") }
                TextButton(onClick = onClose) { Text("Go Back", color = Color.LightGray) }
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun InvoiceScannerView(
    onTextDetected: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    // UI state
    var isDetected by remember { mutableStateOf(false) }
    var detectedNumbers by remember { mutableStateOf("") }
    var shouldCapture by remember { mutableStateOf(false) }
    
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            recognizer.close()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null && !isDetected) {
                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            recognizer.process(image)
                                .addOnSuccessListener { visionText ->
                                    // Always update the preview text if it looks like an ID
                                    val idMatch = Regex("\\d{4,7}").find(visionText.text)
                                    if (idMatch != null) {
                                        detectedNumbers = idMatch.value
                                        
                                        // ONLY call onTextDetected if the user has pressed the trigger
                                        if (shouldCapture && !isDetected) {
                                            isDetected = true
                                            onTextDetected(visionText.text)
                                        }
                                    }
                                }
                                .addOnCompleteListener { imageProxy.close() }
                        } else {
                            imageProxy.close()
                        }
                    }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
                    } catch (exc: Exception) {
                        Log.e("InvoiceScanner", "Binding failed", exc)
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Dim background
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))

        // Targeted Scanning Frame
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val borderColor = if (isDetected) Color(0xFF4CAF50) else if (detectedNumbers.isNotEmpty()) Color.Yellow else Color.White
            val borderWidth = if (isDetected) 4.dp else 2.dp
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(width = 300.dp, height = 180.dp)
                        .border(BorderStroke(borderWidth, borderColor), shape = MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDetected) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
                
                if (detectedNumbers.isNotEmpty()) {
                    Text(
                        text = if (isDetected) "ID: $detectedNumbers ADDED" else "ID: $detectedNumbers READY",
                        color = if (isDetected) Color(0xFF4CAF50) else Color.Yellow,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // Navigation and Trigger
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.TopStart).background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            
            // THE TRIGGER BUTTON
            LargeFloatingActionButton(
                onClick = { if (detectedNumbers.isNotEmpty()) shouldCapture = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .size(80.dp),
                containerColor = if (detectedNumbers.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = "Capture",
                    modifier = Modifier.size(40.dp)
                )
            }

            if (detectedNumbers.isEmpty()) {
                Text(
                    text = "Align item row to enable trigger",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 120.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

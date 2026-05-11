package com.example.loadassist.ui_

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.loadassist.R
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    // Calculate distance based on screen width in pixels for smooth movement
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    
    // Increased multipliers ensure the larger logo starts and ends fully off-screen
    val startX = -screenWidthPx * 2.5f
    val endX = screenWidthPx * 2.5f
    
    val offsetX = remember { Animatable(startX) } 
    val scale = remember { Animatable(1f) }
    
    LaunchedEffect(Unit) {
        // 1. Drive in from left - Slowed to 3000ms (3 seconds)
        offsetX.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 3000)
        )
        
        // 2. THE HORN (Sound + Vibration + Visual Pulse)
        // Trigger haptic feedback
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(200)
        }

        // Play the horn sound (matches horn.mp3 in res/raw)
        try {
            val resId = context.resources.getIdentifier("horn", "raw", context.packageName)
            if (resId != 0) {
                MediaPlayer.create(context, resId).apply {
                    start()
                    setOnCompletionListener { release() }
                }
            }
        } catch (e: Exception) {
            // Sound failed or not found, animation continues
        }

        // Visual "honk" pulse effect - Slowed to 300ms phases
        scale.animateTo(1.15f, tween(300))
        scale.animateTo(1f, tween(300))
        
        delay(1500) // Longer pause to show the large logo (1.5 seconds)
        
        // 3. Drive out to right - Slowed to 2000ms (2 seconds)
        offsetX.animateTo(
            targetValue = endX,
            animationSpec = tween(durationMillis = 2000)
        )
        
        // 4. Navigate to Login screen
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.load_assist_truck_logo),
            contentDescription = "Truck Logo",
            modifier = Modifier
                .size(500.dp) // Increased logo size from 320.dp to 500.dp
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .scale(scale.value)
        )
    }
}

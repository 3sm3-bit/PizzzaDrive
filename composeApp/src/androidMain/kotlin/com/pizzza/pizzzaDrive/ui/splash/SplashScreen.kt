package com.pizzza.pizzzaDrive.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pizzza.pizzzaDrive.R
import com.valu.uitaycompose.swipe.UiTayGif
import com.valu.uitaycompose.utils.tay_red_600
import com.valu.uitaycompose.utils.textGabbiB25
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    val scale = remember { Animatable(0.6f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        
        delay(3000.milliseconds)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            UiTayGif(
                resId = R.drawable.ui_ani_drive,
                width = 150.dp,
                height = 150.dp
            )
            
            Spacer(Modifier.height(16.dp))

            Text(
                text = "PIZZZA DRIVER",
                color = tay_red_600,
                fontSize = 42.sp,
                style = textGabbiB25,
                modifier = Modifier.scale(scale.value)
            )
            
            Spacer(Modifier.height(60.dp))

            CircularProgressIndicator(
                color = tay_red_600,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = "Cargando pedidos...",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

package com.pizzza.pizzzaDrive.ui.splash

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.pizzza.pizzzaDrive.R
import com.valu.uitaycompose.swipe.UiTayGif
import com.valu.uitaycompose.utils.tay_red_600
import com.valu.uitaycompose.utils.textGabbiB28
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scale = remember { Animatable(0.6f) }
    
    var permissionsGranted by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val allGranted = result.values.all { it }
        if (allGranted) {
            permissionsGranted = true
            showPermissionDialog = false
        } else {
            showPermissionDialog = true
        }
    }

    // 1. Efecto para manejar el flujo de permisos
    LaunchedEffect(Unit) {
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        
        if (allGranted) {
            permissionsGranted = true
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    // 2. Efecto para la animación y navegación (solo cuando hay permisos)
    LaunchedEffect(permissionsGranted) {
        if (permissionsGranted) {
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
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Permiso de Ubicación") },
            text = { Text("Esta aplicación necesita acceder a tu ubicación para gestionar las rutas de entrega. Por favor, acepta los permisos para continuar.") },
            confirmButton = {
                Button(onClick = {
                    showPermissionDialog = false
                    permissionLauncher.launch(permissions)
                }) {
                    Text("Reintentar")
                }
            },
            dismissButton = {
                Button(onClick = { activity?.finish() }) {
                    Text("Salir")
                }
            }
        )
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
                style = textGabbiB28,
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

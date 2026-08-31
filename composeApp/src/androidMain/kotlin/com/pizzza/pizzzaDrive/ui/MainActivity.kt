package com.pizzza.pizzzaDrive.ui

import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.pizzza.pizzzaDrive.component.AppNavigation
import com.pizzza.pizzzaDrive.ui.base.BaseActivity
import com.pizzza.pizzzaDrive.ui.base.BaseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.pizzza.pizzzaDrive.service.TrackingService
import android.Manifest
import com.pizzza.pizzzaDrive.model.ParentOrderModel

class MainActivity : BaseActivity() {

    private val viewModel : AppViewModel by viewModel()

    private val prefs by lazy { getSharedPreferences("pizza_prefs", Context.MODE_PRIVATE) }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        if (locationGranted) {
            println("UI_TAG_DRIVER: MainActivity: Permisos concedidos.")
            if (viewModel.uiState.orders.any { it.isReadyForTracking() }) {
                startTrackingService()
            }
        }
    }

    private fun ParentOrderModel.isReadyForTracking(): Boolean {
        val s = state.trim().uppercase()
        val hasValidCoords = latitude != "0" && latitude.isNotEmpty() &&
                             longitude != "0" && longitude.isNotEmpty() &&
                             currentLatitude != "0" && currentLatitude.isNotEmpty() &&
                             currentLongitude != "0" && currentLongitude.isNotEmpty()
        return s == "INICIADO" && hasValidCoords
    }

    @Composable
    override fun SetScreenConfig() {
        AppNavigation(
            viewModel = viewModel
        )
    }

    override fun setDataGlobal() {
        // 1. Cargar el estado guardado y aplicarlo al ViewModel sin disparar efectos aún
        val isEnabled = prefs.getBoolean("notifications_enabled", false)
        viewModel.setNotificationsEnabled(isEnabled)

        // 3. Iniciar la observación del Switch
        observeNotificationToggle()
        
        // 4. Observar cambios en el estado de las órdenes para el TrackingService
        observeTrackingTrigger()
    }

    private fun observeTrackingTrigger() {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                snapshotFlow { viewModel.uiState.orders.any { it.isReadyForTracking() } }
                    .collectLatest { hasActiveTracking ->
                        if (hasActiveTracking) {
                            checkPermissionsAndStartService()
                        } else {
                            stopTrackingService()
                        }
                    }
            }
        }
    }

    private fun checkPermissionsAndStartService() {
        val permissionsToRequest = mutableListOf<String>()
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isEmpty()) {
            println("UI_TAG_DRIVER: MainActivity: Hay órdenes válidas en INICIADO. Arrancando TrackingService.")
            startTrackingService()
        } else {
            println("UI_TAG_DRIVER: MainActivity: Solicitando permisos necesarios...")
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun startTrackingService() {
        val intent = Intent(this, TrackingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopTrackingService() {
        val intent = Intent(this, TrackingService::class.java)
        stopService(intent)
    }

    private fun observeNotificationToggle() {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                snapshotFlow { viewModel.uiState.notificationsEnabled }
                    .collectLatest { enabled ->
                        // Guardar en persistencia cada vez que cambie
                        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
                    }
            }
        }
    }

    override fun getViewModel(): BaseViewModel = viewModel

    override fun getViewModels(): List<BaseViewModel> = listOf(
        viewModel
    )
}

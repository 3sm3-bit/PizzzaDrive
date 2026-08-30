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

class MainActivity : BaseActivity() {

    private val viewModel : AppViewModel by viewModel()

    private val prefs by lazy { getSharedPreferences("pizza_prefs", Context.MODE_PRIVATE) }

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

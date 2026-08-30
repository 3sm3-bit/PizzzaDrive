package com.pizzza.pizzzaDrive.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pizzza.pizzzaDrive.DispatcherProvider
import com.pizzza.pizzzaDrive.model.ParentOrderModel
import com.pizzza.pizzzaDrive.ui.base.BaseViewModel
import com.pizzza.pizzzaDrive.ui.driver.model.DriverUiState
import com.pizzza.pizzzaDrive.usecases.DataUseCase

class AppViewModel(
    private val dataUseCase: DataUseCase,
    dispatchers: DispatcherProvider
) : BaseViewModel(dispatchers) {

    var uiState by mutableStateOf(DriverUiState())
        private set

    fun getGeneralOrderList() {
        execute {
            val response = dataUseCase.loadParentOrder()
            updateStateWithOrders(response)
        }
    }

    fun refresh() {
        execute {
            val response = dataUseCase.loadParentOrder(forceRefresh = true)
            updateStateWithOrders(response)
        }
    }

    private fun updateStateWithOrders(orders: List<ParentOrderModel>) {
        // Ahora el servidor ya envía solo DELIVERY. Filtramos solo por estados relevantes.
        val relevantOrders = orders.filter { 
            val state = it.state.trim().uppercase()
            state == "ENVIADO" || state == "INICIADO" || state == "ENTREGADO"
        }

        val sortedOrders = relevantOrders.sortedBy {
            when (it.state.trim().uppercase()) {
                "ENVIADO" -> 1
                "INICIADO" -> 2
                "ENTREGADO" -> 3
                else -> 4
            }
        }

        val countEnviado = sortedOrders.count { it.state.trim().uppercase() == "ENVIADO" }
        val countEntregado = sortedOrders.count { it.state.trim().uppercase() == "ENTREGADO" }

        uiState = uiState.copy(
            orders = sortedOrders,
            filteredOrders = sortedOrders,
            countEnviado = countEnviado,
            countEntregado = countEntregado
        )
    }

    fun updateOrderState(order: ParentOrderModel, newState: String) {
        if (order.state.trim().uppercase() == newState.uppercase()) return
        val previousState = uiState

        val updatedOrders = uiState.orders.map { 
            if (it.uid == order.uid) it.copy(state = newState) else it 
        }
        updateStateWithOrders(updatedOrders)

        execute(loading = false) {
            try {
                dataUseCase.updateOrder(order.copy(state = newState))
                
                // Nota: El inicio del TrackingService se gestiona en MainActivity 
                // observando el cambio de estado en uiState.orders
            } catch (e: Exception) {
                uiState = previousState
                throw e
            }
        }
    }

    fun avanzarEstado(order: ParentOrderModel) {
        val currentState = order.state.trim().uppercase()
        
        val nextState = when (currentState) {
            "ENVIADO" -> "INICIADO"
            "INICIADO" -> "ENTREGADO"
            else -> currentState
        }
        
        if (nextState != currentState) {
            updateOrderState(order, nextState)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        uiState = uiState.copy(notificationsEnabled = enabled)
    }

    fun selectOrder(order: ParentOrderModel?) {
        uiState = uiState.copy(selectedOrder = order)
    }
}

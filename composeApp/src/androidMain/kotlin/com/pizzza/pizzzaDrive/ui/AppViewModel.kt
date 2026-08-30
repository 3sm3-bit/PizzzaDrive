package com.pizzza.pizzzaDrive.ui

import android.util.Log
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
            try {
                val response = dataUseCase.loadParentOrder()
                updateStateWithOrders(response)
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error en getGeneralOrderList", e)
                throw e
            }
        }
    }

    fun refresh() {
        execute {
            try {
                val response = dataUseCase.loadParentOrder(forceRefresh = true)
                updateStateWithOrders(response)
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error en refresh", e)
                throw e
            }
        }
    }

    private fun updateStateWithOrders(orders: List<ParentOrderModel>) {
        // Filtrar solo pedidos Delivery en estados relevantes para el repartidor
        val deliveryOrders = orders.filter { 
            it.reception.trim().uppercase().contains("DELIVERY") &&
            (it.state.trim().uppercase() == "ENVIADO" || 
             it.state.trim().uppercase() == "INICIADO" || 
             it.state.trim().uppercase() == "ENTREGADO")
        }

        val sortedOrders = deliveryOrders.sortedBy {
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

        // Actualización optimista: Cambiamos estado en la lista local inmediatamente
        val updatedOrders = uiState.orders.map { 
            if (it.uid == order.uid) it.copy(state = newState) else it 
        }
        updateStateWithOrders(updatedOrders)

        execute(loading = false) {
            try {
                // Sincronizamos con DB y Servicios
                dataUseCase.updateOrder(order.copy(state = newState))
            } catch (e: Exception) {
                // Si falla, revertimos al estado anterior
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

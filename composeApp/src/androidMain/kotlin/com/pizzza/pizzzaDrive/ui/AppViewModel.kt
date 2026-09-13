package com.pizzza.pizzzaDrive.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pizzza.pizzzaDrive.model.ParentOrderModel
import com.pizzza.pizzzaDrive.ui.base.BaseViewModel
import com.pizzza.pizzzaDrive.ui.base.GlobalUiStateManager
import com.pizzza.pizzzaDrive.ui.driver.model.DriverUiState
import com.pizzza.pizzzaDrive.usecases.DataUseCase

class AppViewModel(
    private val dataUseCase: DataUseCase,
    private val globalUiStateManager: GlobalUiStateManager,
) : BaseViewModel() {

    var uiState by mutableStateOf(DriverUiState())
        private set

    fun getGeneralOrderList() {
        execute(globalUiStateManager = globalUiStateManager) {
            val response = dataUseCase.loadParentOrder()
            updateStateWithOrders(response)
        }
    }

    fun refresh() {
        execute(globalUiStateManager = globalUiStateManager) {
            val response = dataUseCase.loadParentOrder(forceRefresh = true)
            updateStateWithOrders(response)
        }
    }

    private fun updateStateWithOrders(orders: List<ParentOrderModel>) {
        val relevantOrders = orders.filter { 
            val state = it.state.trim().uppercase()
            val isDelivery = it.reception.trim().uppercase().contains("DELIVERY")
            
            isDelivery && (state == "ENVIADO" || state == "INICIADO" || state == "ENTREGADO")
        }

        val sortedOrders = relevantOrders.sortedBy {
            when (it.state.trim().uppercase()) {
                "ENVIADO" -> 1
                "INICIADO" -> 2
                "ENTREGADO" -> 3
                else -> 4
            }
        }

        val countEnviado = relevantOrders.count { 
            val s = it.state.trim().uppercase()
            s == "ENVIADO" || s == "INICIADO" 
        }
        val countEntregado = relevantOrders.count { it.state.trim().uppercase() == "ENTREGADO" }

        uiState = uiState.copy(
            orders = sortedOrders,
            countEnviado = countEnviado,
            countEntregado = countEntregado,
            filteredOrders = sortedOrders // Mostrar todo por defecto
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
                dataUseCase.updateOrder(order.copy(state = newState))
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


    fun syncProducts(onComplete: (Boolean) -> Unit = {}) {
        execute(loading = false) {
                    val localUser = io { dataUseCase.getUserLocal() }
                    onComplete(localUser!=null)

        }
    }

    fun logout(onSuccess: () -> Unit) {
        execute(globalUiStateManager = globalUiStateManager) {
            io { dataUseCase.logout() }
            onSuccess()
        }
    }
}

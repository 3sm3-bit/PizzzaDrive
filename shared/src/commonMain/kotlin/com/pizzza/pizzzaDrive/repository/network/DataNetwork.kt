package com.pizzza.pizzzaDrive.repository.network

import com.pizzza.pizzzaDrive.model.ParentOrderModel
import com.pizzza.pizzzaDrive.repository.network.exception.ErrorNetwork
import com.pizzza.pizzzaDrive.repository.network.model.loadParentOrder
import com.pizzza.pizzzaDrive.usecases.network.IDataNetwork
import com.pizzza.pizzzaDrive.repository.utils.ConnectivityManager

class DataNetwork(
    private val apiService: KmmService,
    private val connectivityManager: ConnectivityManager
) : IDataNetwork {

    override suspend fun updateOrder(data: ParentOrderModel): String = apiCall {
        if (!connectivityManager.isConnected()) throw ErrorNetwork()
        println("UI_TAG_DRIVER: DataNetwork: Actualizando pedido ${data.uid} a estado ${data.state} en el servidor...")
        apiService.updateParentOrder(data.toParentOrderRequest())
    }

    override suspend fun loadParentOrder(forceRefresh: Boolean): List<ParentOrderModel> {
        println("UI_TAG_DRIVER: DataNetwork: Iniciando carga de pedidos. forceRefresh=$forceRefresh")

        if (!connectivityManager.isConnected()) {
            println("UI_TAG_DRIVER: DataNetwork: Sin conexión a internet.")
            throw ErrorNetwork()
        }

        println("UI_TAG_DRIVER: DataNetwork: Solicitando pedidos actualizados al servidor...")
        return apiCall({
            apiService.getParentOrder()
        }) { response ->
            println("UI_TAG_DRIVER: DataNetwork: Servidor respondió con ${response.size} pedidos.")
            response.loadParentOrder()
        }
    }

    override suspend fun logout() {
        println("UI_TAG_DRIVER: DataNetwork: Cerrando sesión (sin persistencia local que limpiar)...")
    }
}

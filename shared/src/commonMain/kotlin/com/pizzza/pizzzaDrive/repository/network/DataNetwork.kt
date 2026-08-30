package com.pizzza.pizzzaDrive.repository.network

import com.pizzza.pizzzaDrive.model.ParentOrderModel
import com.pizzza.pizzzaDrive.repository.network.exception.ErrorNetwork
import com.pizzza.pizzzaDrive.repository.network.model.loadParentOrder
import com.pizzza.pizzzaDrive.usecases.network.IDataNetwork
import com.pizzza.pizzzaDrive.repository.utils.ConnectivityManager
import com.pizzza.pizzzaDrive.repository.db.manager.AppDataBase
import com.pizzza.pizzzaDrive.repository.db.toEntity
import com.pizzza.pizzzaDrive.repository.db.toEntityListFromResponse
import com.pizzza.pizzzaDrive.repository.db.toModelList as toModelListFromDb

class DataNetwork(
    private val apiService: KmmService,
    private val connectivityManager: ConnectivityManager,
    private val database: AppDataBase
) : IDataNetwork {

    override suspend fun updateOrder(data: ParentOrderModel): String = apiCall {
        if (!connectivityManager.isConnected()) throw ErrorNetwork()
        println("UI_TAG_DRIVER: DataNetwork: Actualizando pedido ${data.uid} a estado ${data.state} en el servidor...")
        val response = apiService.updateParentOrder(data.toParentOrderRequest())
        
        println("UI_TAG_DRIVER: DataNetwork: Sincronizando actualización del pedido ${data.uid} en la DB local...")
        database.parentOrderDao().insertAll(listOf(data.toEntity()))
        response
    }

    override suspend fun loadParentOrder(forceRefresh: Boolean): List<ParentOrderModel> {
        val dao = database.parentOrderDao()
        val localOrders = dao.getAll()
        
        println("UI_TAG_DRIVER: DataNetwork: Iniciando carga de pedidos. forceRefresh=$forceRefresh, localCount=${localOrders.size}")

        if (localOrders.isNotEmpty() && !forceRefresh) {
            println("UI_TAG_DRIVER: DataNetwork: Cargando desde base de datos local...")
            return localOrders.toModelListFromDb()
        }

        if (!connectivityManager.isConnected()) {
            println("UI_TAG_DRIVER: DataNetwork: Sin conexión a internet.")
            if (localOrders.isNotEmpty()) {
                println("UI_TAG_DRIVER: DataNetwork: Retornando datos locales por falta de conexión.")
                return localOrders.toModelListFromDb()
            } else {
                println("UI_TAG_DRIVER: DataNetwork: No hay datos locales ni conexión. Lanzando error.")
                throw ErrorNetwork()
            }
        }

        println("UI_TAG_DRIVER: DataNetwork: Solicitando pedidos actualizados al servidor...")
        return apiCall({
            apiService.getParentOrder()
        }) { response ->
            println("UI_TAG_DRIVER: DataNetwork: Servidor respondió con ${response.size} pedidos. Actualizando DB local...")
            dao.deleteAll()
            val entities = response.toEntityListFromResponse()
            dao.insertAll(entities)
            println("UI_TAG_DRIVER: DataNetwork: DB local actualizada correctamente con ${entities.size} registros.")
            response.loadParentOrder()
        }
    }

    override suspend fun logout() {
        println("UI_TAG_DRIVER: DataNetwork: Cerrando sesión y limpiando datos locales...")
        database.userDao().logout()
    }
}

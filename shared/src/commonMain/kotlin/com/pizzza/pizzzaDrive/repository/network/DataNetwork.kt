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
        println("DataNetwork: Actualizando pedido ${data.uid} a estado ${data.state}...")
        val response = apiService.updateParentOrder(data.toParentOrderRequest())
        database.parentOrderDao().insertAll(listOf(data.toEntity()))
        response
    }

    override suspend fun loadParentOrder(forceRefresh: Boolean): List<ParentOrderModel> {
        val dao = database.parentOrderDao()
        val localOrders = dao.getAll()
        
        if (localOrders.isNotEmpty() && !forceRefresh) {
            val models = localOrders.toModelListFromDb()
            if (models.any { it.orders.isEmpty() }) {
                println("DataNetwork: Datos locales incompletos. Forzando refresco de red.")
            } else {
                return models
            }
        }

        if (!connectivityManager.isConnected()) {
            if (localOrders.isNotEmpty()) {
                return localOrders.toModelListFromDb()
            } else {
                throw ErrorNetwork()
            }
        }

        return apiCall({
            apiService.getParentOrder()
        }) { response ->
            dao.deleteAll()
            dao.insertAll(response.toEntityListFromResponse())
            response.loadParentOrder()
        }
    }

    override suspend fun logout() {
        database.userDao().logout()
    }
}

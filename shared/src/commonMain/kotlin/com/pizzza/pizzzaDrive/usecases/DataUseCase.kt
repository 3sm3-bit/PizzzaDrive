package com.pizzza.pizzzaDrive.usecases

import com.pizzza.pizzzaDrive.model.ParentOrderModel
import com.pizzza.pizzzaDrive.usecases.network.IDataNetwork

class DataUseCase(private val iDataNetwork: IDataNetwork) {

    suspend fun loadParentOrder(forceRefresh: Boolean = false) = iDataNetwork.loadParentOrder(forceRefresh)

    suspend fun updateOrder(data: ParentOrderModel) = iDataNetwork.updateOrder(data)


}

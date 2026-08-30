package com.pizzza.pizzzaDrive.usecases.network

import com.pizzza.pizzzaDrive.model.ParentOrderModel

interface IDataNetwork {

    suspend fun updateOrder(data: ParentOrderModel): String

    suspend fun loadParentOrder(forceRefresh: Boolean = false): List<ParentOrderModel>

    suspend fun logout()

}

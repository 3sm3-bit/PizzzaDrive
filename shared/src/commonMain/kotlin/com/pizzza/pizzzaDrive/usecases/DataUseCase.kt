package com.pizzza.pizzzaDrive.usecases

import com.pizzza.pizzzaDrive.model.ParentOrderModel
import com.pizzza.pizzzaDrive.repository.db.entity.UserEntity
import com.pizzza.pizzzaDrive.repository.network.model.LoginRequest
import com.pizzza.pizzzaDrive.usecases.network.IDataNetwork

class DataUseCase(private val iDataNetwork: IDataNetwork) {

    suspend fun loadParentOrder(forceRefresh: Boolean = false) = iDataNetwork.loadParentOrder(forceRefresh)

    suspend fun updateOrder(data: ParentOrderModel) = iDataNetwork.updateOrder(data)

    suspend fun login(data: LoginRequest) = iDataNetwork.login(data)

    suspend fun saveUserLocal(user: UserEntity) = iDataNetwork.saveUserLocal(user)

    suspend fun getUserLocal() = iDataNetwork.getUserLocal()

    suspend fun logout() {
        iDataNetwork.logout()
    }

}

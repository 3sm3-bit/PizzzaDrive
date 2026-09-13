package com.pizzza.pizzzaDrive.usecases.network

import com.pizzza.pizzzaDrive.model.ParentOrderModel
import com.pizzza.pizzzaDrive.repository.db.entity.UserEntity
import com.pizzza.pizzzaDrive.repository.network.model.LoginRequest
import com.pizzza.pizzzaDrive.repository.network.model.LoginResponse

interface IDataNetwork {

    suspend fun updateOrder(data: ParentOrderModel): String

    suspend fun loadParentOrder(forceRefresh: Boolean = false): List<ParentOrderModel>

    suspend fun logout()

    suspend fun login(data: LoginRequest): LoginResponse

    suspend fun saveUserLocal(user: UserEntity)

    suspend fun getUserLocal(): UserEntity?
}

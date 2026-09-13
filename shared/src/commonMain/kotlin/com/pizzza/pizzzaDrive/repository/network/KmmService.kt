package com.pizzza.pizzzaDrive.repository.network

import com.pizzza.pizzzaDrive.repository.network.model.OrderResponse
import com.pizzza.pizzzaDrive.repository.network.model.ParentOrderResponse
import com.pizzza.pizzzaDrive.repository.network.model.ProductResponse
import com.pizzza.pizzzaDrive.repository.network.model.BranchResponse
import com.pizzza.pizzzaDrive.repository.network.model.UserResponse
import com.pizzza.pizzzaDrive.repository.network.model.LoginRequest
import com.pizzza.pizzzaDrive.repository.network.model.LoginResponse
import com.pizzza.pizzzaDrive.shared.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KmmService(private val client: HttpClient) {

    companion object {
        val BASE_URL = if (BuildConfig.IS_DEBUG) {
            BuildConfig.BASE_URL_SERVICE_DEV
        } else {
            BuildConfig.BASE_URL_SERVICE
        }
    }

    suspend fun getParentOrder(): List<ParentOrderResponse> {
        return client.get("${BASE_URL}/pizzzeria/order/reception/buscar?reception=DELIVERY").body()
    }

    suspend fun updateParentOrder(request: ParentOrderResponse): String {
        return client.put("${BASE_URL}/pizzzeria/order/generalOrder") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun login(request: LoginRequest): LoginResponse {
        return client.post("${BASE_URL}/services/user/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}

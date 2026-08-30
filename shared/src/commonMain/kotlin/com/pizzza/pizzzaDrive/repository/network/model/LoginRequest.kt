package com.pizzza.pizzzaDrive.repository.network.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val nameUser: String,
    val password: String
)

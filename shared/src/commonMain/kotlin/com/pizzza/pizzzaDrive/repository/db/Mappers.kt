package com.pizzza.pizzzaDrive.repository.db

import com.pizzza.pizzzaDrive.model.ParentOrderModel
import com.pizzza.pizzzaDrive.model.ProductModel
import com.pizzza.pizzzaDrive.repository.db.entity.ParentOrderEntity
import com.pizzza.pizzzaDrive.repository.db.entity.ProductEntity
import com.pizzza.pizzzaDrive.repository.network.model.ParentOrderResponse

fun ParentOrderEntity.toModel() = ParentOrderModel(
    uid = uid,
    nameClient = nameClient,
    description = description,
    price = price,
    phone = phone,
    date = date,
    state = state,
    address = address,
    reception = reception,
    symbol = symbol,
    branchId = branchId,
    stage = stage,
    latitude = latitude,
    longitude = longitude,
    userId = userId,
    driverId = driverId,
    currentLatitude = currentLatitude,
    currentLongitude = currentLongitude,
    statePay = statePay,
    orders = emptyList() // No orders stored in Entity for now
)

fun ParentOrderModel.toEntity() = ParentOrderEntity(
    uid = uid,
    nameClient = nameClient,
    description = description,
    price = price,
    phone = phone,
    date = date,
    state = state,
    address = address,
    reception = reception,
    symbol = symbol,
    branchId = branchId,
    stage = stage,
    latitude = latitude,
    longitude = longitude,
    userId = userId,
    driverId = driverId,
    currentLatitude = currentLatitude,
    currentLongitude = currentLongitude,
    statePay = statePay
)

fun List<ParentOrderEntity>.toModelList() = map { it.toModel() }

fun List<ParentOrderResponse>.toEntityListFromResponse() = map {
    ParentOrderEntity(
        uid = it.uid ?: "",
        nameClient = it.nameClient ?: "",
        description = it.description ?: "",
        price = it.price ?: "",
        phone = it.phone ?: "",
        date = it.date ?: "",
        state = it.state ?: "COMFIRMADO",
        address = it.address ?: "",
        reception = it.reception ?: "RECOJO",
        symbol = it.symbol ?: "$",
        branchId = it.branchId ?: "1",
        stage = it.stage ?: "1",
        latitude = it.latitude ?: "0",
        longitude = it.longitude ?: "0",
        userId = it.userId ?: "0",
        driverId = it.driverId ?: "0",
        currentLatitude = it.currentLatitude ?: "0",
        currentLongitude = it.currentLongitude ?: "0",
        statePay = it.statePay ?: "PENDIENTE"
    )
}

fun ProductEntity.toModel() = ProductModel(
    uid = uid,
    nameProduct = nameProduct,
    type = type,
    price = price,
    tamanio = tamanio,
    description = description,
    priceChosse = priceChosse,
    currency = currency,
    currencySymbol = currencySymbol,
    state = state,
    urlImg = urlImg
)

fun ProductModel.toEntity() = ProductEntity(
    uid = uid,
    nameProduct = nameProduct,
    type = type,
    price = price,
    tamanio = tamanio,
    description = description,
    priceChosse = priceChosse,
    currency = currency,
    currencySymbol = currencySymbol,
    state = state,
    urlImg = urlImg
)

fun List<ProductEntity>.toProductModelList() = map { it.toModel() }
fun List<ProductModel>.toProductEntityList() = map { it.toEntity() }

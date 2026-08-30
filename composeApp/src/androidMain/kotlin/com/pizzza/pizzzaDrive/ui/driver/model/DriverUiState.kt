package com.pizzza.pizzzaDrive.ui.driver.model

import com.pizzza.pizzzaDrive.model.ParentOrderModel
import com.pizzza.pizzzaDrive.model.ProductModel
import com.pizzza.pizzzaDrive.model.BranchModel

data class DriverUiState(
    val orders: List<ParentOrderModel> = emptyList(),
    val filteredOrders: List<ParentOrderModel> = emptyList(),
    val products: List<ProductModel> = emptyList(),
    val branches: List<BranchModel> = emptyList(),
    val selectedOrder: ParentOrderModel? = null,
    val selectedBranch: BranchModel? = null,
    val selectedProduct: ProductModel? = null,
    val selectedCategory: String = "TODOS",
    val selectedFilter: String = "TODOS",
    val notificationsEnabled: Boolean = false,
    val countEnviado: Int = 0,
    val countEntregado: Int = 0
)

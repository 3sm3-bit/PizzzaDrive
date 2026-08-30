package com.pizzza.pizzzaDrive.ui.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pizzza.pizzzaDrive.model.ParentOrderModel
import com.pizzza.pizzzaDrive.ui.AppViewModel
import com.valu.uitaycompose.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenDriverHome(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val uiState = viewModel.uiState
    var showSheet by remember { mutableStateOf(false) }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val columns = when {
        screenWidth < 600 -> 1
        isLandscape -> 4
        else -> 2
    }

    LaunchedEffect(uiState.selectedOrder) {
        showSheet = uiState.selectedOrder != null
    }

    LaunchedEffect(Unit) {
        viewModel.getGeneralOrderList()
    }

    Scaffold(
        containerColor = Color(0xFFF0F2F5)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Rutas de Entrega",
                        style = textB20,
                        fontSize = 18.sp,
                        color = Color(0xFF1C1E21)
                    )

                    IconButton(
                        onClick = { viewModel.refresh() },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFFDDDFE2), RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refrescar",
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFF007BFF)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusIndicator(
                        text = "ENVIADO",
                        count = uiState.countEnviado,
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                    StatusIndicator(
                        text = "ENTREGADO",
                        count = uiState.countEntregado,
                        color = Color(0xFF8A8D91),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.filteredOrders.isEmpty() && !viewModel.uiStateBase.loading) {
                    EmptyOrdersPlaceholder { viewModel.refresh() }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.filteredOrders) { order ->
                            OrderCard(
                                order = order,
                                backgroundColor = Color.White,
                                textColor = Color(0xFF1C1E21),
                                onDetailClick = { viewModel.selectOrder(order) },
                                onStateChange = { action ->
                                    if (action == "AVANZAR") {
                                        viewModel.avanzarEstado(order)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            if (showSheet && uiState.selectedOrder != null) {
                OrderDetailSheet(
                    order = uiState.selectedOrder,
                    onDismiss = { viewModel.selectOrder(null) }
                )
            }
        }
    }
}

@Composable
fun StatusIndicator(
    text: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = textB10,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = count.toString(),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = textB12,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun EmptyOrdersPlaceholder(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No hay repartos pendientes",
                style = textM16,
                color = Color.Gray
            )
            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Actualizar")
            }
        }
    }
}

@Composable
fun OrderCard(
    order: ParentOrderModel,
    backgroundColor: Color,
    textColor: Color,
    onDetailClick: () -> Unit,
    onStateChange: (String) -> Unit
) {
    val stateUpper = order.state.trim().uppercase()
    
    val statusColor = when (stateUpper) {
        "ENVIADO" -> Color(0xFFF59E0B)
        "INICIADO" -> Color(0xFF3B82F6)
        "ENTREGADO" -> Color(0xFF8A8D91)
        else -> Color(0xFF3B82F6)
    }

    val displayState = when (stateUpper) {
        "ENVIADO" -> "RECIBIDO"
        "INICIADO" -> "EN CAMINO"
        "ENTREGADO" -> "ENTREGADO"
        else -> stateUpper
    }

    val actionButtonText = when (stateUpper) {
        "ENVIADO" -> "INICIAR"
        "INICIADO" -> "ENTREGADO"
        else -> null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(statusColor)
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = order.nameClient,
                        style = textB16,
                        color = textColor,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        color = statusColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = displayState,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = textB10,
                            color = statusColor
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(top = 4.dp, bottom = 8.dp), color = Color(0xFFF0F2F5))

                order.orders.forEach { item ->
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.quantity} ${item.nameProduct}",
                                style = textM14,
                                color = textColor,
                                modifier = Modifier.weight(1f)
                            )
                            val subtotal = (item.quantity.toDoubleOrNull() ?: 0.0) * (item.price.toDoubleOrNull() ?: 0.0)
                            Text(
                                text = "$${subtotal.toInt()}",
                                style = textB14,
                                color = textColor
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(top = 4.dp, bottom = 8.dp), color = Color(0xFFF0F2F5))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        val isDelivery = order.reception.trim().uppercase().contains("DELIVERY")
                        val deliveryPrice = order.orders.firstOrNull()?.priceDelivery ?: "0"
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isDelivery) "🏠 DELIVERY" else "🛍️ LOCAL",
                                style = textB12,
                                fontSize = 11.sp,
                                color = if (isDelivery) Color(0xFFE91E63) else Color(0xFF007BFF)
                            )
                            if (isDelivery && deliveryPrice != "0") {
                                Text(
                                    text = " ($${deliveryPrice})",
                                    style = textB12,
                                    fontSize = 11.sp,
                                    color = Color(0xFF65676B)
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "TOTAL", style = textB10, color = Color(0xFF8A8D91))
                        Text(
                            text = "$${order.price}",
                            style = textB20,
                            color = Color(0xFF10B981)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDetailClick,
                        modifier = Modifier.weight(1f).height(32.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F2F5)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(text = "DETALLE", style = textB12, color = Color(0xFF1C1E21), fontSize = 12.sp)
                    }

                    if (actionButtonText != null) {
                        Button(
                            onClick = { onStateChange("AVANZAR") },
                            modifier = Modifier.weight(1f).height(32.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = statusColor),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(text = actionButtonText, style = textB12, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailSheet(
    order: ParentOrderModel,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFF0F2F5),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(text = "Detalle del Pedido", style = textB20, color = Color(0xFF1C1E21))
            Text(text = "Cliente: ${order.nameClient}", style = textM16, color = Color(0xFF007BFF), modifier = Modifier.padding(top = 4.dp))

            Spacer(modifier = Modifier.height(20.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                order.orders.forEach { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = Color(0xFFF0F2F5), shape = RoundedCornerShape(8.dp), modifier = Modifier.size(40.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = "x${item.quantity}", style = textB16, color = Color(0xFF1C1E21))
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.nameProduct, style = textB16, color = Color(0xFF1C1E21))
                                Text(text = "${item.tamanio} • ${item.typeDough}", style = textS12, color = Color(0xFF65676B))
                            }
                            Text(text = "$${(item.price.toDoubleOrNull() ?: 0.0).toInt()}", style = textB16, color = Color(0xFF1C1E21))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Total del Pedido", style = textB16, color = Color(0xFF1C1E21))
                    Text(text = "$${order.price}", style = textB20, color = Color(0xFF10B981))
                }
            }
        }
    }
}

package com.momipets.admin.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.momipets.admin.data.AdminOrder
import com.momipets.admin.data.FirebaseAdminRepository
import com.momipets.admin.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun OrdersScreen(orders: List<AdminOrder>) {
    val statuses = listOf("All", "Pending", "Confirmed", "Processing", "Shipped", "Delivered", "Cancelled", "Returned")
    var selectedStatus by remember { mutableStateOf("All") }
    var activeOrder by remember { mutableStateOf<AdminOrder?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val filtered = if (selectedStatus == "All") orders else orders.filter { it.status == selectedStatus }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamCanvas)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "Orders (${filtered.size})",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ForestDark
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(statuses) { st ->
                FilterChip(
                    selected = selectedStatus == st,
                    onClick = { selectedStatus = st },
                    label = { Text(st, fontSize = 11.sp) }
                )
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered, key = { it.id }) { order ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CrispWhite),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${order.orderNumber} · ${order.status}",
                                fontWeight = FontWeight.Bold,
                                color = ForestDark
                            )
                            Text(
                                "PKR ${order.totalPkr.toLong()}",
                                fontWeight = FontWeight.Bold,
                                color = ForestDark
                            )
                        }
                        Text(
                            "${order.customerName} (${order.customerPhone}) · ${order.city}",
                            fontSize = 13.sp,
                            color = InkPrimary
                        )
                        Text(
                            order.address,
                            fontSize = 12.sp,
                            color = InkMuted
                        )
                        Text(
                            "Payment: ${order.paymentMethod} · Supplier: ${order.supplierForwardStatus}",
                            fontSize = 11.sp,
                            color = InkMuted
                        )
                        if (order.trackingNumber.isNotBlank()) {
                            Text(
                                "Courier: ${order.courierName} · Tracking: ${order.trackingNumber}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ForestMid
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { activeOrder = order },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestDark),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) { Text("Manage & Tracking", fontSize = 12.sp) }

                            OutlinedButton(
                                onClick = {
                                    val slip = buildString {
                                        appendLine("MOMI PETS STORE — SUPPLIER DISPATCH SLIP")
                                        appendLine("Order: ${order.orderNumber}")
                                        appendLine("Customer: ${order.customerName} (${order.customerPhone})")
                                        appendLine("City: ${order.city}")
                                        appendLine("Address: ${order.address}")
                                        appendLine("Payment: ${order.paymentMethod}")
                                        order.items.forEach {
                                            appendLine("- ${it.title} (SKU: ${it.sku}) x ${it.quantity}")
                                        }
                                    }
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Supplier Order", slip))
                                    scope.launch {
                                        FirebaseAdminRepository.updateOrderStatus(
                                            order.id,
                                            if (order.status == "Pending") "Processing" else order.status,
                                            "Forwarded Manually",
                                            order.courierName,
                                            order.trackingNumber
                                        )
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) { Text("Forward to Supplier", fontSize = 12.sp, color = ForestDark) }
                        }
                    }
                }
            }
        }
    }

    if (activeOrder != null) {
        OrderManageDialog(
            order = activeOrder!!,
            onDismiss = { activeOrder = null },
            onSave = { status, forwardStatus, courier, tracking ->
                scope.launch {
                    FirebaseAdminRepository.updateOrderStatus(
                        activeOrder!!.id,
                        status,
                        forwardStatus,
                        courier,
                        tracking
                    )
                    activeOrder = null
                }
            }
        )
    }
}

@Composable
private fun OrderManageDialog(
    order: AdminOrder,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    val allStatuses = listOf("Pending", "Confirmed", "Processing", "Shipped", "Delivered", "Cancelled", "Returned")
    val forwardModes = listOf("Awaiting Supplier Confirmation", "Forwarded Manually", "Dispatched by Supplier")

    var status by remember { mutableStateOf(order.status) }
    var forwardStatus by remember { mutableStateOf(order.supplierForwardStatus) }
    var courier by remember { mutableStateOf(order.courierName) }
    var tracking by remember { mutableStateOf(order.trackingNumber) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage ${order.orderNumber}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Order Status: $status", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(allStatuses) { st ->
                        FilterChip(selected = status == st, onClick = { status = st }, label = { Text(st, fontSize = 10.sp) })
                    }
                }
                Text("Supplier Fulfillment: $forwardStatus", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(forwardModes) { fm ->
                        FilterChip(selected = forwardStatus == fm, onClick = { forwardStatus = fm }, label = { Text(fm, fontSize = 10.sp) })
                    }
                }
                OutlinedTextField(value = courier, onValueChange = { courier = it }, label = { Text("Courier (e.g., TCS, Leopards, Trax)") })
                OutlinedTextField(value = tracking, onValueChange = { tracking = it }, label = { Text("Tracking Number") })
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(status, forwardStatus, courier, tracking) },
                colors = ButtonDefaults.buttonColors(containerColor = ForestDark)
            ) { Text("Update Order") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

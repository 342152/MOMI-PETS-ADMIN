package com.momipets.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.momipets.admin.data.AdminCommission
import com.momipets.admin.data.AdminOrder
import com.momipets.admin.data.AdminSupplier
import com.momipets.admin.data.FirebaseAdminRepository
import com.momipets.admin.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProfitsScreen(
    orders: List<AdminOrder>,
    commissions: List<AdminCommission>,
    suppliers: List<AdminSupplier>
) {
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }

    val totalConfirmedProfit = commissions.sumOf { it.actualProfitPkr }
    val pendingCommission = commissions.filter { it.commissionStatus == "Pending" }.sumOf { it.actualProfitPkr }
    val paidCommission = commissions.filter { it.commissionStatus == "Paid" }.sumOf { it.actualProfitPkr }

    val now = System.currentTimeMillis()
    val oneDayMs = 86_400_000L
    val dailyProfit = commissions.filter {
        val t = it.createdAt?.toDate()?.time ?: 0L
        now - t <= oneDayMs
    }.sumOf { it.actualProfitPkr }
    val weeklyProfit = commissions.filter {
        val t = it.createdAt?.toDate()?.time ?: 0L
        now - t <= 7 * oneDayMs
    }.sumOf { it.actualProfitPkr }
    val monthlyProfit = commissions.filter {
        val t = it.createdAt?.toDate()?.time ?: 0L
        now - t <= 30 * oneDayMs
    }.sumOf { it.actualProfitPkr }

    Scaffold(
        containerColor = CreamCanvas,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ForestDark,
                contentColor = CreamCanvas
            ) {
                Icon(Icons.Default.Add, contentDescription = "Record Realized Profit")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("Profit & Commission Ledger", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ForestDark)
                Text(
                    "Actual Profit = Customer Total − Supplier Cost − Shipping − Other Costs",
                    fontSize = 12.sp,
                    color = InkMuted
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryBox("Confirmed Profit", "PKR ${totalConfirmedProfit.toLong()}", Modifier.weight(1f))
                    SummaryBox("Pending Comm.", "PKR ${pendingCommission.toLong()}", Modifier.weight(1f))
                    SummaryBox("Paid Comm.", "PKR ${paidCommission.toLong()}", Modifier.weight(1f))
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryBox("24h Profit", "PKR ${dailyProfit.toLong()}", Modifier.weight(1f))
                    SummaryBox("7d Profit", "PKR ${weeklyProfit.toLong()}", Modifier.weight(1f))
                    SummaryBox("30d Profit", "PKR ${monthlyProfit.toLong()}", Modifier.weight(1f))
                }
            }

            items(commissions, key = { it.id }) { com ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CrispWhite),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Order: ${com.orderId}", fontWeight = FontWeight.Bold, color = ForestDark)
                            Text(
                                "Net: PKR ${com.actualProfitPkr.toLong()}",
                                fontWeight = FontWeight.Bold,
                                color = if (com.actualProfitPkr >= 0) ForestDark else AlertRed
                            )
                        }
                        Text(
                            "Sold: PKR ${com.sellingTotalPkr.toLong()} · Supplier: PKR ${com.supplierCostPkr.toLong()} · " +
                                "Ship: PKR ${com.shippingCostPkr.toLong()} · Other: PKR ${com.otherCostPkr.toLong()}",
                            fontSize = 11.sp,
                            color = InkMuted
                        )
                        Text("Commission Status: ${com.commissionStatus}", fontSize = 11.sp, color = ForestMid)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        RecordProfitDialog(
            orders = orders,
            suppliers = suppliers,
            onDismiss = { showAddDialog = false },
            onSave = { orderId, supId, sell, cost, ship, other, status ->
                scope.launch {
                    FirebaseAdminRepository.recordCommissionAndProfit(
                        orderId, supId, sell, cost, ship, other, status
                    )
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
private fun SummaryBox(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CrispWhite),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 10.sp, color = InkMuted)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ForestDark)
        }
    }
}

@Composable
private fun RecordProfitDialog(
    orders: List<AdminOrder>,
    suppliers: List<AdminSupplier>,
    onDismiss: () -> Unit,
    onSave: (String, String, Double, Double, Double, Double, String) -> Unit
) {
    val delivered = orders.firstOrNull { it.status == "Delivered" } ?: orders.firstOrNull()
    var orderId by remember { mutableStateOf(delivered?.id ?: "") }
    var supplierId by remember { mutableStateOf(suppliers.firstOrNull()?.id ?: "sup_1") }
    var sellingTotal by remember { mutableStateOf(delivered?.totalPkr?.toString() ?: "0") }
    var supplierCost by remember { mutableStateOf("0") }
    var shippingCost by remember { mutableStateOf("250") }
    var otherCost by remember { mutableStateOf("0") }
    var status by remember { mutableStateOf("Pending") }

    val actual = (sellingTotal.toDoubleOrNull() ?: 0.0) -
        (supplierCost.toDoubleOrNull() ?: 0.0) -
        (shippingCost.toDoubleOrNull() ?: 0.0) -
        (otherCost.toDoubleOrNull() ?: 0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Actual Order Profit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = orderId, onValueChange = { orderId = it }, label = { Text("Delivered Order ID *") })
                OutlinedTextField(value = supplierId, onValueChange = { supplierId = it }, label = { Text("Supplier ID *") })
                OutlinedTextField(value = sellingTotal, onValueChange = { sellingTotal = it }, label = { Text("Customer Paid Total (PKR)") })
                OutlinedTextField(value = supplierCost, onValueChange = { supplierCost = it }, label = { Text("Supplier Purchase Cost (PKR)") })
                OutlinedTextField(value = shippingCost, onValueChange = { shippingCost = it }, label = { Text("Courier / Shipping Cost (PKR)") })
                OutlinedTextField(value = otherCost, onValueChange = { otherCost = it }, label = { Text("Other Expenses (PKR)") })
                Text("Actual Net Profit: PKR ${actual.toLong()}", fontWeight = FontWeight.Bold, color = ForestDark)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Pending", "Paid").forEach { s ->
                        FilterChip(selected = status == s, onClick = { status = s }, label = { Text(s) })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        orderId.ifBlank { "ord_manual" },
                        supplierId.ifBlank { "sup_manual" },
                        sellingTotal.toDoubleOrNull() ?: 0.0,
                        supplierCost.toDoubleOrNull() ?: 0.0,
                        shippingCost.toDoubleOrNull() ?: 0.0,
                        otherCost.toDoubleOrNull() ?: 0.0,
                        status
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestDark)
            ) { Text("Save Profit Record") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

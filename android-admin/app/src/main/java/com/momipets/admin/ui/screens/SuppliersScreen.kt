package com.momipets.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.momipets.admin.data.AdminPayment
import com.momipets.admin.data.AdminSupplier
import com.momipets.admin.data.FirebaseAdminRepository
import com.momipets.admin.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SuppliersScreen(
    suppliers: List<AdminSupplier>,
    payments: List<AdminPayment>
) {
    val scope = rememberCoroutineScope()
    var showSupplierDialog by remember { mutableStateOf(false) }
    var editingSupplier by remember { mutableStateOf<AdminSupplier?>(null) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = CreamCanvas,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingSupplier = null
                    showSupplierDialog = true
                },
                containerColor = ForestDark,
                contentColor = CreamCanvas
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Supplier")
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Suppliers (${suppliers.size})",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestDark
                    )
                    OutlinedButton(onClick = { showPaymentDialog = true }) {
                        Text("+ Record Payment", fontSize = 12.sp, color = ForestDark)
                    }
                }
            }

            if (suppliers.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CrispWhite),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "No suppliers added yet. Tap + to add a verified Pakistani pet supplier with clear COD, advance payment, and shipping terms.",
                            fontSize = 13.sp,
                            color = InkMuted,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(suppliers, key = { it.id }) { sup ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CrispWhite),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(sup.name, fontWeight = FontWeight.Bold, color = ForestDark)
                                TextButton(onClick = {
                                    editingSupplier = sup
                                    showSupplierDialog = true
                                }) { Text("Edit", color = ForestDark) }
                            }
                            Text("${sup.city} · Contact: ${sup.contactPerson} (${sup.phone})", fontSize = 12.sp, color = InkPrimary)
                            Text(
                                "COD: ${if (sup.supportsCod) "Yes" else "No"} · " +
                                    "Direct Ship: ${if (sup.supportsDirectDelivery) "Yes" else "No"} · " +
                                    "Advance Req: ${if (sup.requiresAdvancePayment) "Yes" else "No"}",
                                fontSize = 11.sp,
                                color = InkMuted
                            )
                            Text(
                                "Shipping Fee: PKR ${sup.shippingChargesPkr.toLong()} · Mode: ${sup.apiIntegrationMode}",
                                fontSize = 11.sp,
                                color = ForestMid
                            )
                            if (sup.deliveryPolicy.isNotBlank()) {
                                Text("Policy: ${sup.deliveryPolicy}", fontSize = 11.sp, color = InkMuted)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Supplier Payments Ledger (${payments.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ForestDark,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(payments.take(15), key = { it.id }) { pay ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CrispWhite),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("${pay.type} · ${pay.status}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = ForestDark)
                            Text("Order: ${pay.orderId} · Ref: ${pay.reference.ifBlank { "-" }}", fontSize = 11.sp, color = InkMuted)
                        }
                        Text("PKR ${pay.amountPkr.toLong()}", fontWeight = FontWeight.Bold, color = ForestDark)
                    }
                }
            }
        }
    }

    if (showSupplierDialog) {
        SupplierEditorDialog(
            initial = editingSupplier,
            onDismiss = { showSupplierDialog = false },
            onSave = { sup, isNew ->
                scope.launch {
                    FirebaseAdminRepository.saveSupplier(sup, isNew)
                    showSupplierDialog = false
                }
            }
        )
    }

    if (showPaymentDialog) {
        PaymentRecordDialog(
            suppliers = suppliers,
            onDismiss = { showPaymentDialog = false },
            onSave = { orderId, supId, amount, type, status, ref ->
                scope.launch {
                    FirebaseAdminRepository.recordSupplierPayment(orderId, supId, amount, type, status, ref)
                    showPaymentDialog = false
                }
            }
        )
    }
}

@Composable
private fun SupplierEditorDialog(
    initial: AdminSupplier?,
    onDismiss: () -> Unit,
    onSave: (AdminSupplier, Boolean) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var city by remember { mutableStateOf(initial?.city ?: "Lahore") }
    var contactPerson by remember { mutableStateOf(initial?.contactPerson ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var whatsapp by remember { mutableStateOf(initial?.whatsapp ?: "") }
    var shippingCharges by remember { mutableStateOf(initial?.shippingChargesPkr?.toString() ?: "250") }
    var deliveryPolicy by remember { mutableStateOf(initial?.deliveryPolicy ?: "") }
    var supportsCod by remember { mutableStateOf(initial?.supportsCod ?: false) }
    var supportsDirect by remember { mutableStateOf(initial?.supportsDirectDelivery ?: true) }
    var requiresAdvance by remember { mutableStateOf(initial?.requiresAdvancePayment ?: true) }
    var apiMode by remember { mutableStateOf(initial?.apiIntegrationMode ?: "manual_forwarding") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add Verified Supplier" else "Edit Supplier") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Supplier Business Name *") })
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City (Pakistan) *") })
                OutlinedTextField(value = contactPerson, onValueChange = { contactPerson = it }, label = { Text("Contact Person *") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone *") })
                OutlinedTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = { Text("WhatsApp") })
                OutlinedTextField(value = shippingCharges, onValueChange = { shippingCharges = it }, label = { Text("Shipping Fee (PKR)") })
                OutlinedTextField(value = deliveryPolicy, onValueChange = { deliveryPolicy = it }, label = { Text("Delivery & Return Policy") })

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = supportsCod, onCheckedChange = { supportsCod = it })
                    Text("Supplier Supports COD", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = supportsDirect, onCheckedChange = { supportsDirect = it })
                    Text("Direct Delivery to Customer", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = requiresAdvance, onCheckedChange = { requiresAdvance = it })
                    Text("Requires Advance Payment", fontSize = 12.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = apiMode == "manual_forwarding",
                        onClick = { apiMode = "manual_forwarding" },
                        label = { Text("Manual Forwarding", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = apiMode == "api_ready",
                        onClick = { apiMode = "api_ready" },
                        label = { Text("API Ready", fontSize = 11.sp) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        AdminSupplier(
                            id = initial?.id ?: "",
                            name = name,
                            city = city,
                            contactPerson = contactPerson,
                            phone = phone,
                            whatsapp = whatsapp,
                            supportsCod = supportsCod,
                            supportsDirectDelivery = supportsDirect,
                            requiresAdvancePayment = requiresAdvance,
                            shippingChargesPkr = shippingCharges.toDoubleOrNull() ?: 250.0,
                            deliveryPolicy = deliveryPolicy,
                            apiIntegrationMode = apiMode,
                            isActive = true
                        ),
                        initial == null
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestDark)
            ) { Text("Save Supplier") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun PaymentRecordDialog(
    suppliers: List<AdminSupplier>,
    onDismiss: () -> Unit,
    onSave: (String, String, Double, String, String, String) -> Unit
) {
    var orderId by remember { mutableStateOf("") }
    var supplierId by remember { mutableStateOf(suppliers.firstOrNull()?.id ?: "manual_supplier") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Supplier Advance") }
    var status by remember { mutableStateOf("Paid") }
    var reference by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Supplier Payment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = orderId, onValueChange = { orderId = it }, label = { Text("Order ID *") })
                OutlinedTextField(value = supplierId, onValueChange = { supplierId = it }, label = { Text("Supplier ID *") })
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (PKR) *") })
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Supplier Advance", "Supplier Settlement", "Customer Refund").forEach { t ->
                        FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t, fontSize = 9.sp) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Pending", "Paid").forEach { s ->
                        FilterChip(selected = status == s, onClick = { status = s }, label = { Text(s, fontSize = 11.sp) })
                    }
                }
                OutlinedTextField(value = reference, onValueChange = { reference = it }, label = { Text("Bank / JazzCash / Easypaisa Ref") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        orderId.ifBlank { "ord_manual" },
                        supplierId.ifBlank { "sup_manual" },
                        amount.toDoubleOrNull() ?: 0.0,
                        type,
                        status,
                        reference
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestDark)
            ) { Text("Save Payment") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

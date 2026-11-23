package com.momipets.admin.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.momipets.admin.data.*
import com.momipets.admin.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProductsScreen(
    products: List<AdminProduct>,
    categories: List<AdminCategory>,
    suppliers: List<AdminSupplier>
) {
    val scope = rememberCoroutineScope()
    var search by remember { mutableStateOf("") }
    var editingProduct by remember { mutableStateOf<AdminProduct?>(null) }
    var showProductDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    val filtered = products.filter {
        search.isBlank() ||
            it.title.contains(search, ignoreCase = true) ||
            it.sku.contains(search, ignoreCase = true) ||
            it.brand.contains(search, ignoreCase = true)
    }

    Scaffold(
        containerColor = CreamCanvas,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingProduct = null
                    showProductDialog = true
                },
                containerColor = ForestDark,
                contentColor = CreamCanvas
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Products (${products.size})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestDark
                )
                OutlinedButton(onClick = { showCategoryDialog = true }) {
                    Text("+ New Category", fontSize = 12.sp, color = ForestDark)
                }
            }

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Search SKU, Brand or Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filtered, key = { it.id }) { prod ->
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
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "${prod.brand} · ${prod.sizeWeight}",
                                        fontSize = 11.sp,
                                        color = InkMuted
                                    )
                                    Text(
                                        prod.title,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = InkPrimary
                                    )
                                    Text(
                                        "PKR ${prod.sellingPricePkr.toLong()} · SKU: ${prod.sku}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = ForestDark
                                    )
                                }
                                Row {
                                    IconButton(onClick = {
                                        editingProduct = prod
                                        showProductDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = ForestDark)
                                    }
                                    IconButton(onClick = {
                                        scope.launch { FirebaseAdminRepository.deleteProduct(prod.id) }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AlertRed)
                                    }
                                }
                            }
                            Text(
                                "${if (prod.isPublished) "Published" else "Hidden"} · " +
                                    "${if (prod.supplierConfirmedAvailable) "Supplier Stock OK" else "Out of Stock"} · " +
                                    if (prod.codSupported) "COD Enabled" else "Advance Only",
                                fontSize = 11.sp,
                                color = InkMuted
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showCategoryDialog = false },
            onCreate = { name, group, desc ->
                scope.launch {
                    FirebaseAdminRepository.createCategory(name, group, desc)
                    showCategoryDialog = false
                }
            }
        )
    }

    if (showProductDialog) {
        ProductEditorDialog(
            initial = editingProduct,
            categories = categories,
            suppliers = suppliers,
            onDismiss = { showProductDialog = false },
            onSave = { prod, sourcing, isNew ->
                scope.launch {
                    FirebaseAdminRepository.saveProduct(prod, sourcing, isNew)
                    showProductDialog = false
                }
            }
        )
    }
}

@Composable
private fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String) -> Unit
) {
    val groups = listOf("Cats", "Dogs", "Birds", "Fish", "Rabbits & Hamsters", "Other Pets", "General Pet Products")
    var name by remember { mutableStateOf("") }
    var group by remember { mutableStateOf(groups.first()) }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Unlimited Category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Category Name") })
                Text("Department: $group", fontSize = 12.sp, color = InkMuted)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    groups.take(4).forEach { g ->
                        FilterChip(selected = group == g, onClick = { group = g }, label = { Text(g, fontSize = 10.sp) })
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    groups.drop(4).forEach { g ->
                        FilterChip(selected = group == g, onClick = { group = g }, label = { Text(g, fontSize = 10.sp) })
                    }
                }
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description (Optional)") })
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onCreate(name, group, desc) },
                colors = ButtonDefaults.buttonColors(containerColor = ForestDark)
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ProductEditorDialog(
    initial: AdminProduct?,
    categories: List<AdminCategory>,
    suppliers: List<AdminSupplier>,
    onDismiss: () -> Unit,
    onSave: (AdminProduct, ProductSourcing, Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var brand by remember { mutableStateOf(initial?.brand ?: "") }
    var sku by remember { mutableStateOf(initial?.sku ?: "MOMI-${System.currentTimeMillis() % 10000}") }
    var sizeWeight by remember { mutableStateOf(initial?.sizeWeight ?: "1.5 kg") }
    var sellingPrice by remember { mutableStateOf(initial?.sellingPricePkr?.toString() ?: "") }
    var supplierCost by remember { mutableStateOf("0") }
    var supplierShipping by remember { mutableStateOf("250") }
    var selectedSupplierId by remember { mutableStateOf(suppliers.firstOrNull()?.id ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var ingredients by remember { mutableStateOf(initial?.ingredients ?: "") }
    var expiryInfo by remember { mutableStateOf(initial?.expiryInfo ?: "") }
    var imageUrl by remember { mutableStateOf(initial?.images?.firstOrNull() ?: "") }
    var isPublished by remember { mutableStateOf(initial?.isPublished ?: true) }
    var supplierAvailable by remember { mutableStateOf(initial?.supplierConfirmedAvailable ?: true) }
    var codSupported by remember { mutableStateOf(initial?.codSupported ?: true) }
    var selectedCat by remember {
        mutableStateOf(
            categories.firstOrNull { it.slug == initial?.categorySlug }
                ?: categories.firstOrNull()
                ?: AdminCategory(id = "cat_cats_dry_cat_food", name = "Dry Cat Food", slug = "cats-dry-cat-food", group = "Cats")
        )
    }
    var uploading by remember { mutableStateOf(false) }

    LaunchedEffect(initial?.id) {
        if (initial != null && initial.id.isNotBlank()) {
            val s = FirebaseAdminRepository.getProductSourcing(initial.id)
            if (s != null) {
                selectedSupplierId = s.supplierId
                supplierCost = s.supplierPurchasePricePkr.toString()
                supplierShipping = s.supplierShippingFeePkr.toString()
            }
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            uploading = true
            scope.launch {
                try {
                    imageUrl = FirebaseAdminRepository.uploadProductImage(uri, sku)
                } finally {
                    uploading = false
                }
            }
        }
    }

    val sellNum = sellingPrice.toDoubleOrNull() ?: 0.0
    val costNum = supplierCost.toDoubleOrNull() ?: 0.0
    val shipNum = supplierShipping.toDoubleOrNull() ?: 0.0
    val estMargin = sellNum - costNum - shipNum

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add Product" else "Edit Product") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Product Title *") })
                OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand Name *") })
                OutlinedTextField(value = sku, onValueChange = { sku = it }, label = { Text("SKU *") })
                OutlinedTextField(value = sizeWeight, onValueChange = { sizeWeight = it }, label = { Text("Size / Weight *") })

                if (categories.isNotEmpty()) {
                    Text("Category: ${selectedCat.group} → ${selectedCat.name}", fontSize = 12.sp, color = ForestDark)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        categories.take(4).forEach { cat ->
                            FilterChip(
                                selected = selectedCat.id == cat.id,
                                onClick = { selectedCat = cat },
                                label = { Text(cat.name, fontSize = 10.sp) }
                            )
                        }
                    }
                }

                if (suppliers.isNotEmpty()) {
                    Text("Supplier: ${suppliers.firstOrNull { it.id == selectedSupplierId }?.name ?: "Select"}", fontSize = 12.sp, color = ForestDark)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        suppliers.take(3).forEach { sup ->
                            FilterChip(
                                selected = selectedSupplierId == sup.id,
                                onClick = {
                                    selectedSupplierId = sup.id
                                    codSupported = sup.supportsCod
                                    supplierShipping = sup.shippingChargesPkr.toString()
                                },
                                label = { Text(sup.name, fontSize = 10.sp) }
                            )
                        }
                    }
                }

                OutlinedTextField(value = sellingPrice, onValueChange = { sellingPrice = it }, label = { Text("Selling Price (PKR) *") })
                OutlinedTextField(value = supplierCost, onValueChange = { supplierCost = it }, label = { Text("Supplier Cost (PKR) *") })
                OutlinedTextField(value = supplierShipping, onValueChange = { supplierShipping = it }, label = { Text("Supplier Shipping (PKR)") })

                Text(
                    "Est. Net Margin: PKR ${estMargin.toLong()} (after supplier cost & shipping)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (estMargin >= 0) ForestDark else AlertRed
                )

                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description (min 10 chars) *") })
                OutlinedTextField(value = ingredients, onValueChange = { ingredients = it }, label = { Text("Food Ingredients / Materials") })
                OutlinedTextField(value = expiryInfo, onValueChange = { expiryInfo = it }, label = { Text("Expiry / Shelf-Life Info") })
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL") })

                OutlinedButton(onClick = { imagePicker.launch("image/*") }, enabled = !uploading) {
                    Text(if (uploading) "Uploading Image..." else "Upload Photo from Phone")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isPublished, onCheckedChange = { isPublished = it })
                    Text("Published on Website", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = supplierAvailable, onCheckedChange = { supplierAvailable = it })
                    Text("Supplier Confirmed In Stock", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = codSupported, onCheckedChange = { codSupported = it })
                    Text("Supplier Supports COD", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val prod = AdminProduct(
                        id = initial?.id ?: "",
                        title = title,
                        brand = brand,
                        sku = sku,
                        categoryGroup = selectedCat.group,
                        categorySlug = selectedCat.slug,
                        categoryName = selectedCat.name,
                        description = if (description.length >= 10) description else "$title — verified supplier pet product.",
                        images = if (imageUrl.isNotBlank()) listOf(imageUrl) else listOf("/src/assets/images/product_cat_nutrition_1795415308581.jpg"),
                        sellingPricePkr = sellNum.coerceAtLeast(1.0),
                        sizeWeight = sizeWeight,
                        ingredients = ingredients,
                        expiryInfo = expiryInfo,
                        isPublished = isPublished,
                        supplierConfirmedAvailable = supplierAvailable,
                        codSupported = codSupported,
                        isFeatured = true
                    )
                    val sourcing = ProductSourcing(
                        productId = prod.id,
                        supplierId = selectedSupplierId.ifBlank { "manual_supplier" },
                        supplierPurchasePricePkr = costNum,
                        supplierShippingFeePkr = shipNum,
                        supplierRequiresConfirmation = true
                    )
                    onSave(prod, sourcing, initial == null)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestDark)
            ) { Text("Save Product") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

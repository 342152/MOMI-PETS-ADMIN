package com.momipets.admin.data

import com.google.firebase.Timestamp

data class AdminProduct(
    val id: String = "",
    val title: String = "",
    val brand: String = "",
    val sku: String = "",
    val categoryGroup: String = "Cats",
    val categorySlug: String = "",
    val categoryName: String = "",
    val description: String = "",
    val images: List<String> = emptyList(),
    val sellingPricePkr: Double = 0.0,
    val compareAtPricePkr: Double? = null,
    val sizeWeight: String = "",
    val ingredients: String = "",
    val expiryInfo: String = "",
    val isPublished: Boolean = true,
    val supplierConfirmedAvailable: Boolean = true,
    val codSupported: Boolean = true,
    val estimatedDispatchDays: String = "1–2 business days",
    val isFeatured: Boolean = false,
    val isBestSeller: Boolean = false,
    val isNewArrival: Boolean = false,
    val isSpecialOffer: Boolean = false
)

data class ProductSourcing(
    val productId: String = "",
    val supplierId: String = "",
    val supplierPurchasePricePkr: Double = 0.0,
    val supplierShippingFeePkr: Double = 0.0,
    val supplierRequiresConfirmation: Boolean = true
)

data class AdminCategory(
    val id: String = "",
    val name: String = "",
    val slug: String = "",
    val group: String = "Cats",
    val description: String = "",
    val sortOrder: Int = 1,
    val isActive: Boolean = true
)

data class AdminSupplier(
    val id: String = "",
    val name: String = "",
    val city: String = "",
    val contactPerson: String = "",
    val phone: String = "",
    val whatsapp: String = "",
    val email: String = "",
    val supportsCod: Boolean = false,
    val supportsDirectDelivery: Boolean = true,
    val requiresAdvancePayment: Boolean = true,
    val shippingChargesPkr: Double = 250.0,
    val deliveryPolicy: String = "",
    val apiIntegrationMode: String = "manual_forwarding",
    val isActive: Boolean = true
)

data class OrderItem(
    val productId: String = "",
    val title: String = "",
    val sku: String = "",
    val quantity: Int = 1,
    val unitPricePkr: Double = 0.0,
    val imageUrl: String = ""
)

data class AdminOrder(
    val id: String = "",
    val orderNumber: String = "",
    val userId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val city: String = "",
    val address: String = "",
    val postalCode: String = "",
    val items: List<OrderItem> = emptyList(),
    val subtotalPkr: Double = 0.0,
    val shippingFeePkr: Double = 0.0,
    val totalPkr: Double = 0.0,
    val paymentMethod: String = "COD",
    val codEligible: Boolean = true,
    val status: String = "Pending",
    val supplierForwardStatus: String = "Awaiting Supplier Confirmation",
    val trackingNumber: String = "",
    val courierName: String = "",
    val customerNotes: String = "",
    val createdAt: Timestamp? = null
)

data class AdminPayment(
    val id: String = "",
    val orderId: String = "",
    val supplierId: String = "",
    val amountPkr: Double = 0.0,
    val type: String = "Supplier Settlement",
    val status: String = "Pending",
    val reference: String = "",
    val createdAt: Timestamp? = null
)

data class AdminCommission(
    val id: String = "",
    val orderId: String = "",
    val supplierId: String = "",
    val sellingTotalPkr: Double = 0.0,
    val supplierCostPkr: Double = 0.0,
    val shippingCostPkr: Double = 0.0,
    val otherCostPkr: Double = 0.0,
    val actualProfitPkr: Double = 0.0,
    val commissionStatus: String = "Pending",
    val createdAt: Timestamp? = null
)

data class AdminBanner(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val ctaLabel: String = "Explore Catalog",
    val targetGroup: String = "All",
    val imageUrl: String = "",
    val isActive: Boolean = true,
    val sortOrder: Int = 1
)

data class AdminWebsiteSettings(
    val storeName: String = "MOMI PETS STORE",
    val ownerName: String = "Mozim Ali",
    val businessLocation: String = "Islamabad, Pakistan",
    val tagline: String = "Verified Pet Nutrition & Everyday Companion Care Across Pakistan",
    val logoUrl: String = "",
    val whatsappDisplay: String = "03363958385",
    val whatsappNumber: String = "+923363958385",
    val supportEmail: String = "care@momipets.pk",
    val announcementText: String = "Nationwide delivery across Pakistan · Free shipping on orders over PKR 5,000",
    val standardShippingPkr: Double = 350.0,
    val freeDeliveryThresholdPkr: Double = 5000.0,
    val codPolicyNote: String = "Cash on Delivery (COD) is available when every item in your bag is supplied by a COD-verified partner."
)

data class AdminNotification(
    val id: String = "",
    val orderId: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val isRead: Boolean = false,
    val createdAt: Timestamp? = null
)

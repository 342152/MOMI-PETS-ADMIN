
package com.momipets.admin.data

import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.app
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Locale

object FirebaseAdminRepository {

    private const val FIRESTORE_DB_ID =
        "ai-studio-4e1f8186-6a0f-4f39-90e1-10851df61993"

    private const val OWNER_EMAIL = "mozim342@gmail.com"

    val auth = Firebase.auth

    val db by lazy {
        Firebase.firestore(Firebase.app, FIRESTORE_DB_ID)
    }

    val storage by lazy {
        Firebase.storage
    }

    // =========================================================
    // OWNER / ADMIN ACCESS VERIFICATION
    // =========================================================

    suspend fun verifyOwnerAccess(user: FirebaseUser?): Boolean {
        if (user == null) return false

        val userEmail = user.email
            ?.trim()
            ?.lowercase(Locale.ROOT)
            ?: return false

        val ownerEmail = OWNER_EMAIL
            .trim()
            .lowercase(Locale.ROOT)

        val isGoogleLogin = user.providerData.any {
            it.providerId == "google.com"
        }

        // Google accounts are verified by Google.
        // Other login providers require verified email.
        if (!isGoogleLogin && !user.isEmailVerified) {
            return false
        }

        val adminRef = db.collection("admins")
            .document(user.uid)

        // Authorized store owner
        if (userEmail == ownerEmail) {

            val snapshot = adminRef.get().await()

            if (!snapshot.exists()) {
                val ownerData = hashMapOf<String, Any>(
                    "uid" to user.uid,
                    "email" to userEmail,
                    "displayName" to (user.displayName ?: "Store Owner"),
                    "role" to "owner",
                    "createdAt" to FieldValue.serverTimestamp()
                )

                adminRef.set(ownerData).await()
            }

            return true
        }

        // Other admins must already be registered.
        val adminSnapshot = adminRef.get().await()

        return adminSnapshot.exists()
    }

    fun signOut() {
        auth.signOut()
    }

    // =========================================================
    // OBSERVE ORDERS
    // =========================================================

    fun observeOrders(): Flow<List<AdminOrder>> = callbackFlow {
        val reg = db.collection("orders")
            .addSnapshotListener { snap, err ->

                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val list = snap?.documents
                    ?.mapNotNull {
                        it.toObject(AdminOrder::class.java)
                    }
                    ?.sortedByDescending {
                        it.createdAt?.seconds ?: 0L
                    }
                    ?: emptyList()

                trySend(list)
            }

        awaitClose { reg.remove() }
    }

    // =========================================================
    // OBSERVE PRODUCTS
    // =========================================================

    fun observeProducts(): Flow<List<AdminProduct>> = callbackFlow {
        val reg = db.collection("products")
            .addSnapshotListener { snap, err ->

                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val list = snap?.documents
                    ?.mapNotNull {
                        it.toObject(AdminProduct::class.java)
                    }
                    ?: emptyList()

                trySend(list)
            }

        awaitClose { reg.remove() }
    }

    // =========================================================
    // OBSERVE CATEGORIES
    // =========================================================

    fun observeCategories(): Flow<List<AdminCategory>> = callbackFlow {
        val reg = db.collection("categories")
            .addSnapshotListener { snap, err ->

                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val list = snap?.documents
                    ?.mapNotNull {
                        it.toObject(AdminCategory::class.java)
                    }
                    ?.sortedBy { it.sortOrder }
                    ?: emptyList()

                trySend(list)
            }

        awaitClose { reg.remove() }
    }

    // =========================================================
    // OBSERVE SUPPLIERS
    // =========================================================

    fun observeSuppliers(): Flow<List<AdminSupplier>> = callbackFlow {
        val reg = db.collection("suppliers")
            .addSnapshotListener { snap, err ->

                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val list = snap?.documents
                    ?.mapNotNull {
                        it.toObject(AdminSupplier::class.java)
                    }
                    ?: emptyList()

                trySend(list)
            }

        awaitClose { reg.remove() }
    }

    // =========================================================
    // OBSERVE PAYMENTS
    // =========================================================

    fun observePayments(): Flow<List<AdminPayment>> = callbackFlow {
        val reg = db.collection("payments")
            .addSnapshotListener { snap, err ->

                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val list = snap?.documents
                    ?.mapNotNull {
                        it.toObject(AdminPayment::class.java)
                    }
                    ?.sortedByDescending {
                        it.createdAt?.seconds ?: 0L
                    }
                    ?: emptyList()

                trySend(list)
            }

        awaitClose { reg.remove() }
    }

    // =========================================================
    // OBSERVE COMMISSIONS
    // =========================================================

    fun observeCommissions(): Flow<List<AdminCommission>> = callbackFlow {
        val reg = db.collection("commissions")
            .addSnapshotListener { snap, err ->

                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val list = snap?.documents
                    ?.mapNotNull {
                        it.toObject(AdminCommission::class.java)
                    }
                    ?.sortedByDescending {
                        it.createdAt?.seconds ?: 0L
                    }
                    ?: emptyList()

                trySend(list)
            }

        awaitClose { reg.remove() }
    }

    // =========================================================
    // OBSERVE NOTIFICATIONS
    // =========================================================

    fun observeNotifications(): Flow<List<AdminNotification>> = callbackFlow {
        val reg = db.collection("notifications")
            .addSnapshotListener { snap, err ->

                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val list = snap?.documents
                    ?.mapNotNull {
                        it.toObject(AdminNotification::class.java)
                    }
                    ?.sortedByDescending {
                        it.createdAt?.seconds ?: 0L
                    }
                    ?: emptyList()

                trySend(list)
            }

        awaitClose { reg.remove() }
    }

    // =========================================================
    // OBSERVE WEBSITE SETTINGS
    // =========================================================

    fun observeWebsiteSettings(): Flow<AdminWebsiteSettings> = callbackFlow {
        val reg = db.collection("websiteSettings")
            .document("main")
            .addSnapshotListener { snap, err ->

                if (err == null && snap != null && snap.exists()) {
                    trySend(
                        snap.toObject(AdminWebsiteSettings::class.java)
                            ?: AdminWebsiteSettings()
                    )
                } else {
                    trySend(AdminWebsiteSettings())
                }
            }

        awaitClose { reg.remove() }
    }

    // =========================================================
    // OBSERVE BANNERS
    // =========================================================

    fun observeBanners(): Flow<List<AdminBanner>> = callbackFlow {
        val reg = db.collection("banners")
            .addSnapshotListener { snap, err ->

                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val list = snap?.documents
                    ?.mapNotNull {
                        it.toObject(AdminBanner::class.java)
                    }
                    ?.sortedBy { it.sortOrder }
                    ?: emptyList()

                trySend(list)
            }

        awaitClose { reg.remove() }
    }

    // =========================================================
    // UPLOAD PRODUCT IMAGE
    // =========================================================

    suspend fun uploadProductImage(
        uri: Uri,
        sku: String
    ): String {

        val ref = storage.reference.child(
            "products/${sku}_${System.currentTimeMillis()}.jpg"
        )

        ref.putFile(uri).await()

        return ref.downloadUrl.await().toString()
    }

    // =========================================================
    // SAVE PRODUCT
    // =========================================================

    suspend fun saveProduct(
        product: AdminProduct,
        sourcing: ProductSourcing,
        isNew: Boolean
    ) {

        val id = if (product.id.isBlank()) {
            "prod_${System.currentTimeMillis()}"
        } else {
            product.id
        }

        val docRef = db.collection("products").document(id)

        val payload = hashMapOf<String, Any?>(
            "id" to id,
            "title" to product.title.trim(),
            "brand" to product.brand.trim(),
            "sku" to product.sku.trim(),
            "categoryGroup" to product.categoryGroup,
            "categorySlug" to product.categorySlug,
            "categoryName" to product.categoryName,
            "description" to product.description.trim(),
            "images" to product.images.ifEmpty {
                listOf("https://firebasestorage.googleapis.com/placeholder")
            },
            "sellingPricePkr" to product.sellingPricePkr,
            "sizeWeight" to product.sizeWeight.trim(),
            "ingredients" to product.ingredients.trim(),
            "expiryInfo" to product.expiryInfo.trim(),
            "isPublished" to product.isPublished,
            "supplierConfirmedAvailable" to product.supplierConfirmedAvailable,
            "codSupported" to product.codSupported,
            "estimatedDispatchDays" to product.estimatedDispatchDays,
            "isFeatured" to product.isFeatured,
            "isBestSeller" to product.isBestSeller,
            "isNewArrival" to product.isNewArrival,
            "isSpecialOffer" to product.isSpecialOffer,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        if (product.compareAtPricePkr != null &&
            product.compareAtPricePkr > 0
        ) {
            payload["compareAtPricePkr"] = product.compareAtPricePkr
        }

        if (isNew) {
            payload["createdAt"] = FieldValue.serverTimestamp()
        }

        docRef.set(
            payload,
            com.google.firebase.firestore.SetOptions.merge()
        ).await()

        docRef.collection("sourcing")
            .document("details")
            .set(
                mapOf(
                    "productId" to id,
                    "supplierId" to sourcing.supplierId.ifBlank {
                        "unassigned"
                    },
                    "supplierPurchasePricePkr" to sourcing.supplierPurchasePricePkr,
                    "supplierShippingFeePkr" to sourcing.supplierShippingFeePkr,
                    "supplierRequiresConfirmation" to sourcing.supplierRequiresConfirmation,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
    }

    // =========================================================
    // GET PRODUCT SOURCING
    // =========================================================

    suspend fun getProductSourcing(
        productId: String
    ): ProductSourcing? {

        val snap = db.collection("products")
            .document(productId)
            .collection("sourcing")
            .document("details")
            .get()
            .await()

        return if (snap.exists()) {
            snap.toObject(ProductSourcing::class.java)
        } else {
            null
        }
    }

    // =========================================================
    // DELETE PRODUCT
    // =========================================================

    suspend fun deleteProduct(productId: String) {
        db.collection("products")
            .document(productId)
            .delete()
            .await()
    }

    // =========================================================
    // CREATE CATEGORY
    // =========================================================

    suspend fun createCategory(
        name: String,
        group: String,
        description: String = ""
    ) {

        val slug = "${group}-${name}"
            .lowercase(Locale.ROOT)
            .replace("&", "and")
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')

        val id = "cat_${slug.replace("-", "_")}_${System.currentTimeMillis() % 10000}"

        db.collection("categories")
            .document(id)
            .set(
                mapOf(
                    "id" to id,
                    "name" to name.trim(),
                    "slug" to slug,
                    "group" to group,
                    "description" to description.trim(),
                    "sortOrder" to 200,
                    "isActive" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
    }

    // =========================================================
    // UPDATE ORDER STATUS
    // =========================================================

    suspend fun updateOrderStatus(
        orderId: String,
        status: String,
        supplierForwardStatus: String,
        courierName: String,
        trackingNumber: String
    ) {

        db.collection("orders")
            .document(orderId)
            .update(
                mapOf(
                    "status" to status,
                    "supplierForwardStatus" to supplierForwardStatus,
                    "courierName" to courierName.trim(),
                    "trackingNumber" to trackingNumber.trim(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
    }

    // =========================================================
    // MARK NOTIFICATION READ
    // =========================================================

    suspend fun markNotificationRead(notificationId: String) {
        db.collection("notifications")
            .document(notificationId)
            .update("isRead", true)
            .await()
    }

    // =========================================================
    // SAVE SUPPLIER
    // =========================================================

    suspend fun saveSupplier(
        supplier: AdminSupplier,
        isNew: Boolean
    ) {

        val id = if (supplier.id.isBlank()) {
            "sup_${System.currentTimeMillis()}"
        } else {
            supplier.id
        }

        val payload = hashMapOf<String, Any>(
            "id" to id,
            "name" to supplier.name.trim(),
            "city" to supplier.city.trim(),
            "contactPerson" to supplier.contactPerson.trim(),
            "phone" to supplier.phone.trim(),
            "whatsapp" to supplier.whatsapp.trim(),
            "email" to supplier.email.trim(),
            "supportsCod" to supplier.supportsCod,
            "supportsDirectDelivery" to supplier.supportsDirectDelivery,
            "requiresAdvancePayment" to supplier.requiresAdvancePayment,
            "shippingChargesPkr" to supplier.shippingChargesPkr,
            "deliveryPolicy" to supplier.deliveryPolicy.trim(),
            "apiIntegrationMode" to supplier.apiIntegrationMode,
            "isActive" to supplier.isActive,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        if (isNew) {
            payload["createdAt"] = FieldValue.serverTimestamp()
        }

        db.collection("suppliers")
            .document(id)
            .set(
                payload,
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
    }

    // =========================================================
    // RECORD SUPPLIER PAYMENT
    // =========================================================

    suspend fun recordSupplierPayment(
        orderId: String,
        supplierId: String,
        amountPkr: Double,
        type: String,
        status: String,
        reference: String
    ) {

        val id = "pay_${System.currentTimeMillis()}"

        db.collection("payments")
            .document(id)
            .set(
                mapOf(
                    "id" to id,
                    "orderId" to orderId.trim(),
                    "supplierId" to supplierId.trim(),
                    "amountPkr" to amountPkr,
                    "type" to type,
                    "status" to status,
                    "reference" to reference.trim(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
    }

    // =========================================================
    // RECORD COMMISSION AND PROFIT
    // =========================================================

    suspend fun recordCommissionAndProfit(
        orderId: String,
        supplierId: String,
        sellingTotalPkr: Double,
        supplierCostPkr: Double,
        shippingCostPkr: Double,
        otherCostPkr: Double,
        commissionStatus: String
    ) {

        val actualProfit =
            sellingTotalPkr -
            supplierCostPkr -
            shippingCostPkr -
            otherCostPkr

        val id = "com_${orderId}"

        db.collection("commissions")
            .document(id)
            .set(
                mapOf(
                    "id" to id,
                    "orderId" to orderId.trim(),
                    "supplierId" to supplierId.trim(),
                    "sellingTotalPkr" to sellingTotalPkr,
                    "supplierCostPkr" to supplierCostPkr,
                    "shippingCostPkr" to shippingCostPkr,
                    "otherCostPkr" to otherCostPkr,
                    "actualProfitPkr" to actualProfit,
                    "commissionStatus" to commissionStatus,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
    }

    // =========================================================
    // SAVE WEBSITE SETTINGS
    // =========================================================

    suspend fun saveWebsiteSettings(
        settings: AdminWebsiteSettings
    ) {

        db.collection("websiteSettings")
            .document("main")
            .set(
                mapOf(
                    "storeName" to settings.storeName.trim(),
                    "ownerName" to settings.ownerName.trim(),
                    "businessLocation" to settings.businessLocation.trim(),
                    "tagline" to settings.tagline.trim(),
                    "logoUrl" to settings.logoUrl.trim(),
                    "whatsappDisplay" to settings.whatsappDisplay.trim(),
                    "whatsappNumber" to settings.whatsappNumber.trim(),
                    "supportEmail" to settings.supportEmail.trim(),
                    "announcementText" to settings.announcementText.trim(),
                    "standardShippingPkr" to settings.standardShippingPkr,
                    "freeDeliveryThresholdPkr" to settings.freeDeliveryThresholdPkr,
                    "codPolicyNote" to settings.codPolicyNote.trim(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
    }

    // =========================================================
    // SAVE BANNER
    // =========================================================

    suspend fun saveBanner(banner: AdminBanner) {

        val id = banner.id.ifBlank {
            "banner_hero_main"
        }

        db.collection("banners")
            .document(id)
            .set(
                mapOf(
                    "id" to id,
                    "title" to banner.title.trim(),
                    "subtitle" to banner.subtitle.trim(),
                    "ctaLabel" to banner.ctaLabel.trim(),
                    "targetGroup" to banner.targetGroup.trim(),
                    "imageUrl" to banner.imageUrl.trim(),
                    "isActive" to banner.isActive,
                    "sortOrder" to banner.sortOrder,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
    }

    // =========================================================
    // SEED DEFAULT CATEGORIES AND WEBSITE SETTINGS
    // =========================================================

    suspend fun seedDefaultCategoriesAndSettings() {

        val batch = db.batch()

        val taxonomy = mapOf(
            "Cats" to listOf(
                "Dry Cat Food",
                "Wet Cat Food",
                "Kitten Food",
                "Adult Cat Food",
                "Cat Treats",
                "Cat Toys",
                "Cat Litter",
                "Cat Beds",
                "Cat Collars",
                "Cat Carriers"
            ),

            "Dogs" to listOf(
                "Puppy Food",
                "Adult Dog Food",
                "Dry Dog Food",
                "Wet Dog Food",
                "Dog Treats",
                "Dog Toys",
                "Dog Collars",
                "Dog Leashes",
                "Dog Harnesses",
                "Dog Beds",
                "Dog Clothes"
            ),

            "Birds" to listOf(
                "Bird Seeds",
                "Bird Pellets",
                "Bird Food",
                "Bird Cages",
                "Bird Feeders",
                "Bird Toys",
                "Bird Accessories"
            ),

            "Fish" to listOf(
                "Fish Food",
                "Aquarium Food",
                "Fish Tanks",
                "Aquarium Filters",
                "Aquarium Lights",
                "Aquarium Accessories"
            ),

            "Rabbits & Hamsters" to listOf(
                "Rabbit Food",
                "Hamster Food",
                "Cages",
                "Bedding",
                "Toys",
                "Feeding Bowls"
            ),

            "Other Pets" to listOf(
                "Turtle & Reptile Care",
                "Guinea Pig & Chinchilla Essentials"
            ),

            "General Pet Products" to listOf(
                "Pet Shampoo",
                "Grooming Brushes",
                "Nail Clippers",
                "Cleaning Products",
                "Feeding Bowls",
                "Water Bottles",
                "Automatic Feeders",
                "Pet Carriers",
                "Pet Houses",
                "Pet Furniture",
                "Pet Hygiene Products",
                "Pet Care Products"
            )
        )

        var orderIdx = 1

        for ((group, names) in taxonomy) {

            for (name in names) {

                val slug = "${group}-${name}"
                    .lowercase(Locale.ROOT)
                    .replace("&", "and")
                    .replace(Regex("[^a-z0-9]+"), "-")
                    .trim('-')

                val id = "cat_${slug.replace("-", "_")}"

                val ref = db.collection("categories")
                    .document(id)

                batch.set(
                    ref,
                    mapOf(
                        "id" to id,
                        "name" to name,
                        "slug" to slug,
                        "group" to group,
                        "sortOrder" to orderIdx++,
                        "isActive" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
        }

        batch.set(
            db.collection("websiteSettings").document("main"),
            mapOf(
                "storeName" to "MOMI PETS STORE",
                "ownerName" to "Mozim Ali",
                "businessLocation" to "Islamabad, Pakistan",
                "tagline" to "Verified Pet Nutrition & Everyday Companion Care Across Pakistan",
                "logoUrl" to "",
                "whatsappDisplay" to "03363958385",
                "whatsappNumber" to "+923363958385",
                "supportEmail" to "care@momipets.pk",
                "announcementText" to "Nationwide delivery across Pakistan · Free shipping on orders over PKR 5,000",
                "standardShippingPkr" to 350.0,
                "freeDeliveryThresholdPkr" to 5000.0,
                "codPolicyNote" to "Cash on Delivery (COD) is available when every item in your bag is supplied by a COD-verified partner.",
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )

        batch.commit().await()
    }
}

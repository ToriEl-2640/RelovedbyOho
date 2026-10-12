package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.NumberFormat
import java.util.Locale

// Thrift Standardized Condition Categories
enum class ConditionType(val displayName: String, val description: String) {
    NEW_WITH_TAGS("New with Tags", "Brand new, never worn, original brand tags attached."),
    LIKE_NEW("Like New", "Appears unworn, zero visible flaws or wash wear."),
    EXCELLENT("Excellent", "Gently worn with no stains, holes, or fabric pills."),
    VERY_GOOD("Very Good", "Minor normal signs of previous wear, structurally pristine."),
    GOOD("Good", "Moderate signs of wear or minor faint mark noted in description."),
    FAIR("Fair", "Noticeable character, minor repaired defect or vintage wear.")
}

// Product Inventory Statuses
enum class ProductStatus {
    DRAFT,
    AVAILABLE,
    RESERVED,
    SOLD,
    ARCHIVED
}

// Order Status Pipeline
enum class OrderStatus(val displayName: String) {
    PENDING_PAYMENT("Pending Payment"),
    PAID("Payment Confirmed"),
    PROCESSING("Processing & Packing"),
    READY_FOR_DELIVERY("Ready for Dispatch"),
    SHIPPED("Out for Delivery"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled")
}

@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    val id: String, // e.g. "RLO-2041"
    val name: String,
    val description: String,
    val category: String, // "Dresses", "Tops", "Trousers", "Jeans", "Skirts", "Jackets", "Bags", "Shoes", "Accessories"
    val subcategory: String = "",
    val brand: String = "Unbranded Vintage",
    val size: String = "M",
    val colour: String = "Multicolour",
    val pattern: String = "Solid",
    val style: String = "Casual",
    val material: String = "Cotton Blend",
    val condition: String = ConditionType.VERY_GOOD.name,
    val conditionNotes: String = "Clean thrift item. No rips or visible stains.",
    val isConditionAdminVerified: Boolean = true,
    val measurements: String = "Bust: 36\" | Length: 38\"",
    val costPrice: Double = 5000.0, // ₦ cost price
    val sellingPrice: Double = 14500.0, // ₦ selling price
    val originalPrice: Double? = null, // for discounts
    val quantity: Int = 1, // 1-of-1 thrift piece
    val status: String = ProductStatus.AVAILABLE.name,
    val imageUrl: String,
    val additionalImages: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val aiConfidence: Float = 0.92f,
    val views: Int = 12,
    val wishlistCount: Int = 3,
    val daysInInventory: Int = 5,
    val sellerName: String = "OHO Vintage Archive",
    val isDeal: Boolean = false,
    val isSwapEligible: Boolean = true,
    val dateAdded: Long = System.currentTimeMillis()
) {
    val isOneAvailable: Boolean get() = quantity == 1 && status == ProductStatus.AVAILABLE.name
    val isAvailable: Boolean get() = status == ProductStatus.AVAILABLE.name
    val formattedSellingPrice: String get() = formatNaira(sellingPrice)
    val formattedOriginalPrice: String? get() = originalPrice?.let { formatNaira(it) }
}

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey
    val productId: String,
    val name: String,
    val price: Double,
    val size: String,
    val imageUrl: String,
    val condition: String,
    val addedAt: Long = System.currentTimeMillis()
) {
    val formattedPrice: String get() = formatNaira(price)
}

@Entity(tableName = "wishlist_items")
data class WishlistItem(
    @PrimaryKey
    val productId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey
    val id: String, // e.g. "ORD-9412"
    val customerName: String,
    val customerPhone: String,
    val customerEmail: String,
    val deliveryAddress: String,
    val city: String, // e.g. "Lekki, Lagos"
    val state: String = "Lagos",
    val deliveryMethod: String = "Express Dispatch Courier",
    val deliveryFee: Double = 2500.0,
    val subtotal: Double,
    val totalAmount: Double,
    val paymentMethod: String = "Paystack",
    val paymentStatus: String = "PAID",
    val orderStatus: String = OrderStatus.PAID.name,
    val productIds: List<String>,
    val productSummaries: List<String>,
    val createdAt: Long = System.currentTimeMillis(),
    val trackingNumber: String = "RLO-NG-" + (1000..9999).random()
) {
    val formattedTotal: String get() = formatNaira(totalAmount)
    val formattedSubtotal: String get() = formatNaira(subtotal)
    val formattedDeliveryFee: String get() = formatNaira(deliveryFee)
}

// AI Analysis Models
data class AiImageQualityResult(
    val isQualityGood: Boolean,
    val score: Int, // 0 - 100
    val lightingNote: String,
    val framingNote: String,
    val clarityNote: String,
    val actionableAdvice: String
)

data class AiPricingSuggestion(
    val suggestedPrice: Double,
    val minRange: Double,
    val maxRange: Double,
    val estimatedProfitMarginPercent: Int,
    val reasoning: String
)

data class AiListingResult(
    val category: String,
    val subcategory: String,
    val colour: String,
    val pattern: String,
    val style: String,
    val sleeveType: String = "",
    val neckline: String = "",
    val fit: String = "Regular Fit",
    val genderCategory: String = "Women",
    val estimatedMaterial: String,
    val materialUncertain: Boolean = true,
    val possibleBrand: String = "Unbranded Vintage",
    val brandUncertain: Boolean = true,
    val conditionObservation: String,
    val suggestedTitle: String,
    val suggestedDescription: String,
    val searchKeywords: List<String>,
    val tags: List<String>,
    val confidence: Float = 0.91f,
    val pricingSuggestion: AiPricingSuggestion,
    val qualityResult: AiImageQualityResult
)

// Helper currency formatting for Nigerian Naira
fun formatNaira(amount: Double): String {
    val format = NumberFormat.getNumberInstance(Locale("en", "NG"))
    format.maximumFractionDigits = 0
    return "₦" + format.format(amount)
}

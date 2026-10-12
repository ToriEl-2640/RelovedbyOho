package com.example.data.repository

import com.example.data.ai.DeadStockInsight
import com.example.data.ai.GeminiThriftService
import com.example.data.ai.NaturalLanguageFilter
import com.example.data.local.AppDatabase
import com.example.data.model.AiListingResult
import com.example.data.model.CartItem
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.data.model.ProductStatus
import com.example.data.model.WishlistItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

class ThriftRepository(
    private val database: AppDatabase,
    val aiService: GeminiThriftService = GeminiThriftService()
) {
    private val productDao = database.productDao()
    private val cartDao = database.cartDao()
    private val wishlistDao = database.wishlistDao()
    private val orderDao = database.orderDao()

    // Storefront Products
    val availableProducts: Flow<List<Product>> = productDao.getAvailableProducts()
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val dealProducts: Flow<List<Product>> = productDao.getDealProducts()
    val deadStockCandidates: Flow<List<Product>> = productDao.getDeadStockCandidates(20)

    fun observeProduct(id: String): Flow<Product?> = productDao.observeProductById(id)

    suspend fun getProduct(id: String): Product? = productDao.getProductById(id)

    suspend fun incrementViews(id: String) = productDao.incrementViews(id)

    // Cart Operations
    val cartItems: Flow<List<CartItem>> = cartDao.getAllCartItems()
    val cartCount: Flow<Int> = cartDao.getCartCount()

    suspend fun addToCart(product: Product): Boolean {
        // Enforce 1-of-1 check: only if available
        if (!product.isAvailable) return false
        cartDao.insertCartItem(
            CartItem(
                productId = product.id,
                name = product.name,
                price = product.sellingPrice,
                size = product.size,
                imageUrl = product.imageUrl,
                condition = product.condition
            )
        )
        return true
    }

    suspend fun removeFromCart(productId: String) {
        cartDao.removeCartItem(productId)
    }

    suspend fun clearCart() {
        cartDao.clearCart()
    }

    // Wishlist Operations
    val wishlistItems: Flow<List<WishlistItem>> = wishlistDao.getAllWishlistItems()
    val wishlistCount: Flow<Int> = wishlistDao.getWishlistCount()

    fun isWishlisted(productId: String): Flow<Boolean> = wishlistDao.isWishlisted(productId)

    suspend fun toggleWishlist(productId: String) {
        val exists = wishlistDao.isWishlistedSync(productId)
        if (exists) {
            wishlistDao.removeWishlist(productId)
            productDao.updateWishlistCount(productId, -1)
        } else {
            wishlistDao.insertWishlist(WishlistItem(productId))
            productDao.updateWishlistCount(productId, 1)
        }
    }

    // Checkout & Order Placement with Automatic 1-of-1 Inventory Lock
    suspend fun placeOrder(
        customerName: String,
        customerPhone: String,
        customerEmail: String,
        deliveryAddress: String,
        city: String,
        state: String,
        deliveryMethod: String,
        deliveryFee: Double,
        paymentMethod: String
    ): Result<Order> {
        val currentCart = cartDao.getAllCartItems().first()
        if (currentCart.isEmpty()) {
            return Result.failure(IllegalStateException("Cart is empty"))
        }

        // Verify all items are still AVAILABLE
        for (item in currentCart) {
            val prod = productDao.getProductById(item.productId)
            if (prod == null || prod.status != ProductStatus.AVAILABLE.name) {
                return Result.failure(IllegalStateException("Item '${item.name}' is a unique 1-of-1 piece and has just been reserved or sold."))
            }
        }

        val subtotal = currentCart.sumOf { it.price }
        val total = subtotal + deliveryFee
        val orderId = "ORD-" + (1000..9999).random()

        val order = Order(
            id = orderId,
            customerName = customerName,
            customerPhone = customerPhone,
            customerEmail = customerEmail,
            deliveryAddress = deliveryAddress,
            city = city,
            state = state,
            deliveryMethod = deliveryMethod,
            deliveryFee = deliveryFee,
            subtotal = subtotal,
            totalAmount = total,
            paymentMethod = paymentMethod,
            paymentStatus = "PAID",
            orderStatus = OrderStatus.PAID.name,
            productIds = currentCart.map { it.productId },
            productSummaries = currentCart.map { "${it.name} (${it.size})" }
        )

        // Lock inventory: automatically change products to SOLD
        for (item in currentCart) {
            productDao.updateProductStatus(item.productId, ProductStatus.SOLD.name)
        }

        // Save order and clear cart
        orderDao.insertOrder(order)
        cartDao.clearCart()

        return Result.success(order)
    }

    // Orders Management
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()
    val ordersCount: Flow<Int> = orderDao.getOrdersCount()
    val totalRevenue: Flow<Double> = orderDao.getTotalRevenue().map { it ?: 0.0 }

    suspend fun updateOrderStatus(orderId: String, newStatus: String) {
        orderDao.updateOrderStatus(orderId, newStatus)
    }

    // Admin Inventory Management
    suspend fun updateProductStatus(productId: String, status: String) {
        productDao.updateProductStatus(productId, status)
    }

    suspend fun applyDiscount(productId: String, discountedPrice: Double) {
        productDao.applyDiscount(productId, discountedPrice)
    }

    suspend fun createProduct(product: Product) {
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product)
    }

    // AI Dead Stock Analysis
    suspend fun getDeadStockInsights(): List<DeadStockInsight> {
        val products = productDao.getAllProducts().first()
        return aiService.analyzeInventoryHealth(products)
    }

    // Natural Language Search Filter Helper
    fun parseSearch(query: String): NaturalLanguageFilter {
        return aiService.parseNaturalLanguageSearch(query)
    }
}

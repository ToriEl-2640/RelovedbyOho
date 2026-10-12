package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.DeadStockInsight
import com.example.data.ai.NaturalLanguageFilter
import com.example.data.local.AppDatabase
import com.example.data.local.InitialData
import com.example.data.model.AiListingResult
import com.example.data.model.CartItem
import com.example.data.model.ConditionType
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.data.model.ProductStatus
import com.example.data.repository.ThriftRepository
import com.example.util.SalsaMusicPlayer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppRole {
    CUSTOMER,
    ADMIN
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class CustomerScreen {
    HOME,
    SHOP,
    WISHLIST,
    CART,
    ACCOUNT,
    PRODUCT_DETAIL,
    CHECKOUT,
    ORDER_CONFIRMATION,
    AI_STYLIST,
    VISUAL_SEARCH,
    ORDER_HISTORY
}

enum class AdminScreen {
    DASHBOARD,
    INVENTORY,
    AI_LISTING,
    ORDERS,
    DEAD_STOCK,
    INSIGHTS,
    FUTURE_SELLER
}

data class StylistMessage(
    val sender: String, // "USER" or "AI"
    val text: String,
    val recommendedProductIds: List<String> = emptyList()
)

class ThriftViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ThriftRepository(
        AppDatabase.getDatabase(application, viewModelScope)
    )

    // Current App Mode (Customer storefront vs Admin / Business Owner)
    private val _appRole = MutableStateFlow(AppRole.CUSTOMER)
    val appRole: StateFlow<AppRole> = _appRole.asStateFlow()

    // Background Classical Music Player (Erik Satie, Bach, Chopin)
    val musicPlayer: SalsaMusicPlayer = SalsaMusicPlayer.getInstance(application)

    private val _showMusicBar = MutableStateFlow(true)
    val showMusicBar: StateFlow<Boolean> = _showMusicBar.asStateFlow()

    fun toggleMusicBar() {
        _showMusicBar.value = !_showMusicBar.value
    }

    // Interactive Navigation Tutorial State
    private val _showTutorial = MutableStateFlow(false)
    val showTutorial: StateFlow<Boolean> = _showTutorial.asStateFlow()

    fun openTutorial() {
        _showTutorial.value = true
    }

    fun dismissTutorial() {
        _showTutorial.value = false
    }

    // Eye Comfort & Appearance Settings (Gentle on the eyes)
    private val _themeMode = MutableStateFlow(ThemeMode.LIGHT)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _eyeComfortMode = MutableStateFlow(true) // Default to true for gentle warm paper tone
    val eyeComfortMode: StateFlow<Boolean> = _eyeComfortMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        val msg = when (mode) {
            ThemeMode.LIGHT -> "Gentle Light Theme enabled"
            ThemeMode.DARK -> "Gentle Espresso Dark Theme enabled"
            ThemeMode.SYSTEM -> "Matching System appearance"
        }
        viewModelScope.launch { _toastEvent.emit(msg) }
    }

    fun toggleEyeComfortMode() {
        val next = !_eyeComfortMode.value
        _eyeComfortMode.value = next
        viewModelScope.launch {
            _toastEvent.emit(if (next) "Eye Comfort Active: Soothing warm paper tones" else "Standard tone restored")
        }
    }

    fun switchRole(role: AppRole) {
        _appRole.value = role
    }

    // Navigation state
    private val _customerScreen = MutableStateFlow(CustomerScreen.HOME)
    val customerScreen: StateFlow<CustomerScreen> = _customerScreen.asStateFlow()

    private val _adminScreen = MutableStateFlow(AdminScreen.DASHBOARD)
    val adminScreen: StateFlow<AdminScreen> = _adminScreen.asStateFlow()

    private val _selectedProductId = MutableStateFlow<String?>(null)
    val selectedProductId: StateFlow<String?> = _selectedProductId.asStateFlow()

    private val _confirmedOrder = MutableStateFlow<Order?>(null)
    val confirmedOrder: StateFlow<Order?> = _confirmedOrder.asStateFlow()

    // Transient message snackbars
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Data streams
    val availableProducts: StateFlow<List<Product>> = repository.availableProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dealProducts: StateFlow<List<Product>> = repository.dealProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItems: StateFlow<List<CartItem>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartCount: StateFlow<Int> = repository.cartCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val wishlistCount: StateFlow<Int> = repository.wishlistCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val orders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ordersCount: StateFlow<Int> = repository.ordersCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalRevenue: StateFlow<Double> = repository.totalRevenue
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val wishlistedIds: StateFlow<Set<String>> = repository.wishlistItems
        .combine(MutableStateFlow(Unit)) { items, _ -> items.map { it.productId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Search and filtering state
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")
    val selectedConditionFilter = MutableStateFlow<String?>(null)
    val activeNaturalFilter = MutableStateFlow<NaturalLanguageFilter?>(null)

    // Navigation triggers
    fun navigateCustomer(screen: CustomerScreen) {
        _customerScreen.value = screen
    }

    fun navigateAdmin(screen: AdminScreen) {
        _adminScreen.value = screen
    }

    fun openProductDetail(productId: String) {
        _selectedProductId.value = productId
        _customerScreen.value = CustomerScreen.PRODUCT_DETAIL
        viewModelScope.launch {
            repository.incrementViews(productId)
        }
    }

    // Cart and Wishlist actions
    fun addToCart(product: Product) {
        viewModelScope.launch {
            val success = repository.addToCart(product)
            if (success) {
                _toastEvent.emit("Added ${product.name} to bag")
            } else {
                _toastEvent.emit("Item is no longer available")
            }
        }
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            repository.removeFromCart(productId)
            _toastEvent.emit("Item removed from bag")
        }
    }

    fun toggleWishlist(productId: String) {
        viewModelScope.launch {
            repository.toggleWishlist(productId)
        }
    }

    // Checkout flow
    fun placeOrder(
        customerName: String,
        customerPhone: String,
        customerEmail: String,
        deliveryAddress: String,
        city: String,
        state: String,
        deliveryMethod: String,
        deliveryFee: Double,
        paymentMethod: String
    ) {
        viewModelScope.launch {
            val result = repository.placeOrder(
                customerName = customerName,
                customerPhone = customerPhone,
                customerEmail = customerEmail,
                deliveryAddress = deliveryAddress,
                city = city,
                state = state,
                deliveryMethod = deliveryMethod,
                deliveryFee = deliveryFee,
                paymentMethod = paymentMethod
            )

            result.onSuccess { order ->
                _confirmedOrder.value = order
                _customerScreen.value = CustomerScreen.ORDER_CONFIRMATION
                _toastEvent.emit("Order placed successfully! 1-of-1 inventory updated.")
            }.onFailure { error ->
                _toastEvent.emit(error.message ?: "Failed to place order")
            }
        }
    }

    // Thrift Swap Action (Buy • Sell • Swap • Discover)
    fun proposeSwap(product: Product) {
        viewModelScope.launch {
            _toastEvent.emit("Swap request submitted for ${product.name}! OHO stylists will review your trade piece.")
        }
    }

    // AI Listing Assistant State
    val intakePresets = InitialData.sampleIntakeItems
    val selectedIntakePreset = MutableStateFlow(intakePresets.first())
    val customIntakeUrl = MutableStateFlow(intakePresets.first().imageUrl)
    val isStudioBackgroundPreview = MutableStateFlow(false)
    val isAnalyzingListing = MutableStateFlow(false)
    val currentAiResult = MutableStateFlow<AiListingResult?>(null)

    // Form editing fields for Admin Review
    val editTitle = MutableStateFlow("")
    val editCategory = MutableStateFlow("Dresses")
    val editSubcategory = MutableStateFlow("Midi Dress")
    val editBrand = MutableStateFlow("Vintage Boutique")
    val editSize = MutableStateFlow("UK 10 / M")
    val editMaterial = MutableStateFlow("")
    val editCondition = MutableStateFlow(ConditionType.VERY_GOOD.name)
    val editConditionNotes = MutableStateFlow("")
    val editAdminVerified = MutableStateFlow(true)
    val editCostPrice = MutableStateFlow("4500")
    val editSellingPrice = MutableStateFlow("15000")
    val editMeasurements = MutableStateFlow("Bust: 36\" | Waist: 28\" | Length: 42\"")
    val editDescription = MutableStateFlow("")
    val editTags = MutableStateFlow("thrift, vintage, midi dress")

    fun selectPresetIntake(preset: InitialData.UnlistedIntake) {
        selectedIntakePreset.value = preset
        customIntakeUrl.value = preset.imageUrl
        editCostPrice.value = preset.costPrice.toInt().toString()
        runAiListingAnalysis()
    }

    fun runAiListingAnalysis() {
        viewModelScope.launch {
            isAnalyzingListing.value = true
            val cost = editCostPrice.value.toDoubleOrNull() ?: 4500.0
            val result = repository.aiService.analyzeClothingItem(
                imageUrl = customIntakeUrl.value,
                costPrice = cost,
                manualCategoryHint = selectedIntakePreset.value.category,
                notesHint = selectedIntakePreset.value.simulatedNotes
            )
            currentAiResult.value = result

            // Populate form
            editTitle.value = result.suggestedTitle
            editCategory.value = result.category
            editSubcategory.value = result.subcategory
            editBrand.value = result.possibleBrand
            editMaterial.value = result.estimatedMaterial
            editDescription.value = result.suggestedDescription
            editConditionNotes.value = result.conditionObservation
            editSellingPrice.value = result.pricingSuggestion.suggestedPrice.toInt().toString()
            editTags.value = result.tags.joinToString(", ")

            isAnalyzingListing.value = false
            _toastEvent.emit("AI Analysis & Pricing generated!")
        }
    }

    fun approveAndPublishListing() {
        viewModelScope.launch {
            val cost = editCostPrice.value.toDoubleOrNull() ?: 4500.0
            val selling = editSellingPrice.value.toDoubleOrNull() ?: 15000.0
            val tagsList = editTags.value.split(",").map { it.trim() }.filter { it.isNotBlank() }

            val newProduct = Product(
                id = "RLO-" + (1020..9999).random(),
                name = editTitle.value.ifBlank { "Curated Thrift Piece" },
                description = editDescription.value.ifBlank { "Unique vintage clothing piece." },
                category = editCategory.value,
                subcategory = editSubcategory.value,
                brand = editBrand.value,
                size = editSize.value,
                colour = currentAiResult.value?.colour ?: "Multicolour",
                pattern = currentAiResult.value?.pattern ?: "Solid",
                style = currentAiResult.value?.style ?: "Chic",
                material = editMaterial.value,
                condition = editCondition.value,
                conditionNotes = editConditionNotes.value,
                isConditionAdminVerified = editAdminVerified.value,
                measurements = editMeasurements.value,
                costPrice = cost,
                sellingPrice = selling,
                quantity = 1,
                status = ProductStatus.AVAILABLE.name,
                imageUrl = customIntakeUrl.value,
                tags = tagsList,
                aiConfidence = currentAiResult.value?.confidence ?: 0.93f,
                views = 1,
                wishlistCount = 0,
                daysInInventory = 0,
                isDeal = false
            )

            repository.createProduct(newProduct)
            _toastEvent.emit("Listing Approved! Live in storefront as 1-of-1 Available.")
            _adminScreen.value = AdminScreen.INVENTORY
        }
    }

    fun saveListingDraft() {
        viewModelScope.launch {
            val cost = editCostPrice.value.toDoubleOrNull() ?: 4500.0
            val selling = editSellingPrice.value.toDoubleOrNull() ?: 15000.0
            val tagsList = editTags.value.split(",").map { it.trim() }.filter { it.isNotBlank() }

            val draftProduct = Product(
                id = "RLO-" + (1020..9999).random(),
                name = editTitle.value.ifBlank { "Draft Listing" },
                description = editDescription.value,
                category = editCategory.value,
                subcategory = editSubcategory.value,
                brand = editBrand.value,
                size = editSize.value,
                material = editMaterial.value,
                condition = editCondition.value,
                conditionNotes = editConditionNotes.value,
                isConditionAdminVerified = false,
                measurements = editMeasurements.value,
                costPrice = cost,
                sellingPrice = selling,
                quantity = 1,
                status = ProductStatus.DRAFT.name,
                imageUrl = customIntakeUrl.value,
                tags = tagsList,
                daysInInventory = 0
            )

            repository.createProduct(draftProduct)
            _toastEvent.emit("Saved as DRAFT in Inventory.")
            _adminScreen.value = AdminScreen.INVENTORY
        }
    }

    // Admin Inventory Controls
    fun markProductSold(productId: String) {
        viewModelScope.launch {
            repository.updateProductStatus(productId, ProductStatus.SOLD.name)
            _toastEvent.emit("Product marked as SOLD.")
        }
    }

    fun markProductAvailable(productId: String) {
        viewModelScope.launch {
            repository.updateProductStatus(productId, ProductStatus.AVAILABLE.name)
            _toastEvent.emit("Product marked as AVAILABLE.")
        }
    }

    fun applyDeadStockDiscount(productId: String, discountedPrice: Double) {
        viewModelScope.launch {
            repository.applyDiscount(productId, discountedPrice)
            _toastEvent.emit("Discount applied! Added to Deals collection.")
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
            _toastEvent.emit("Order updated to $newStatus")
        }
    }

    // Dead Stock Insights
    private val _deadStockList = MutableStateFlow<List<DeadStockInsight>>(emptyList())
    val deadStockList: StateFlow<List<DeadStockInsight>> = _deadStockList.asStateFlow()

    fun loadDeadStockAnalysis() {
        viewModelScope.launch {
            _deadStockList.value = repository.getDeadStockInsights()
        }
    }

    // AI Stylist Conversation State
    val stylistMessages = MutableStateFlow(
        listOf(
            StylistMessage(
                sender = "AI",
                text = "Hello gorgeous! I'm Oho, your AI Thrift Stylist. Tell me what vibe or event you're dressing for (e.g. 'brunch in Ikoyi', 'effortless workwear', or 'chic Lagos wedding guest')."
            )
        )
    )
    val stylistInput = MutableStateFlow("")

    fun sendStylistMessage(query: String) {
        if (query.isBlank()) return
        val userMsg = StylistMessage(sender = "USER", text = query)
        stylistMessages.value = stylistMessages.value + userMsg
        stylistInput.value = ""

        viewModelScope.launch {
            val q = query.lowercase()
            val available = availableProducts.value
            val matches = when {
                q.contains("brunch") || q.contains("sunday") -> {
                    available.filter { it.category == "Dresses" || it.category == "Bags" }.take(3)
                }
                q.contains("work") || q.contains("office") || q.contains("blazer") -> {
                    available.filter { it.category == "Jackets" || it.category == "Trousers" || it.category == "Shoes" }.take(3)
                }
                q.contains("casual") || q.contains("weekend") || q.contains("chill") -> {
                    available.filter { it.category == "Jeans" || it.category == "Tops" || it.category == "Shoes" }.take(3)
                }
                q.contains("chic") || q.contains("classy") || q.contains("night") -> {
                    available.filter { it.category == "Dresses" || it.category == "Skirts" || it.category == "Bags" }.take(3)
                }
                else -> {
                    available.take(3)
                }
            }

            val replyText = if (matches.isNotEmpty()) {
                "I curated these exact 1-of-1 pieces from our archive for that look. They pair harmoniously with neutral accessories!"
            } else {
                "Here are our freshest statement arrivals that fit that aesthetic beautifully:"
            }

            val aiMsg = StylistMessage(
                sender = "AI",
                text = replyText,
                recommendedProductIds = matches.map { it.id }
            )
            stylistMessages.value = stylistMessages.value + aiMsg
        }
    }

    // AI Visual Search Simulation State
    val visualSearchSelectedImage = MutableStateFlow<String?>(null)
    val visualSearchResults = MutableStateFlow<List<Product>>(emptyList())
    val isVisualSearching = MutableStateFlow(false)

    fun runVisualSearch(imageUrl: String) {
        visualSearchSelectedImage.value = imageUrl
        isVisualSearching.value = true
        viewModelScope.launch {
            // Find closest matching products in available inventory
            val matched = availableProducts.value.filter { p ->
                if (imageUrl.contains("dress")) p.category == "Dresses" || p.category == "Skirts"
                else if (imageUrl.contains("blazer")) p.category == "Jackets"
                else if (imageUrl.contains("bag")) p.category == "Bags"
                else p.category == "Trousers" || p.category == "Jeans"
            }.ifEmpty { availableProducts.value.take(3) }

            visualSearchResults.value = matched
            isVisualSearching.value = false
        }
    }

    fun parseSearch(query: String): NaturalLanguageFilter {
        return repository.parseSearch(query)
    }

    init {
        // Run initial AI analysis on the default intake preset
        runAiListingAnalysis()
        loadDeadStockAnalysis()
        // Start peaceful, soothing classical music background session (Satie, Bach, Chopin)
        musicPlayer.play()
    }

    override fun onCleared() {
        super.onCleared()
        musicPlayer.release()
    }
}

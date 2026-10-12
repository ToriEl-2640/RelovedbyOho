package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AdminScreen
import com.example.ui.AppRole
import com.example.ui.CustomerScreen
import com.example.ui.ThriftViewModel
import com.example.ui.admin.AdminAiListingScreen
import com.example.ui.admin.AdminDashboardScreen
import com.example.ui.admin.AdminDeadStockScreen
import com.example.ui.admin.AdminFutureSellerScreen
import com.example.ui.admin.AdminInsightsScreen
import com.example.ui.admin.AdminInventoryScreen
import com.example.ui.admin.AdminOrdersScreen
import androidx.compose.foundation.layout.Column
import com.example.ui.components.NavigationTutorialDialog
import com.example.ui.components.SalsaMusicPlayerBar
import com.example.ui.components.ThriftHeader
import com.example.util.SalsaMusicPlayer
import com.example.ui.customer.AccountScreen
import com.example.ui.customer.AiStylistScreen
import com.example.ui.customer.CartScreen
import com.example.ui.customer.CheckoutScreen
import com.example.ui.customer.HomeScreen
import com.example.ui.customer.OrderConfirmationScreen
import com.example.ui.customer.ProductDetailScreen
import com.example.ui.customer.ShopScreen
import com.example.ui.customer.VisualSearchScreen
import com.example.ui.customer.WishlistScreen
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.ui.ThemeMode
import com.example.ui.theme.TerracottaDark
import com.example.ui.theme.TerracottaPrimary
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    private var musicPlayer: SalsaMusicPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        musicPlayer = SalsaMusicPlayer.getInstance(this)
        enableEdgeToEdge()
        setContent {
            val viewModel: ThriftViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val eyeComfortMode by viewModel.eyeComfortMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            MyApplicationTheme(
                darkTheme = isDark,
                eyeComfortMode = eyeComfortMode
            ) {
                RelovedApp(viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        musicPlayer?.onAppForeground()
    }

    override fun onPause() {
        super.onPause()
        musicPlayer?.onAppBackground()
    }
}

@Composable
fun RelovedApp(viewModel: ThriftViewModel = viewModel()) {
    val currentRole by viewModel.appRole.collectAsState()
    val eyeComfortMode by viewModel.eyeComfortMode.collectAsState()
    val customerScreen by viewModel.customerScreen.collectAsState()
    val adminScreen by viewModel.adminScreen.collectAsState()
    val cartCount by viewModel.cartCount.collectAsState()
    val wishlistCount by viewModel.wishlistCount.collectAsState()
    val availableProducts by viewModel.availableProducts.collectAsState()
    val dealProducts by viewModel.dealProducts.collectAsState()
    val wishlistedIds by viewModel.wishlistedIds.collectAsState()
    val selectedProductId by viewModel.selectedProductId.collectAsState()
    val confirmedOrder by viewModel.confirmedOrder.collectAsState()
    val orders by viewModel.orders.collectAsState()

    val showTutorial by viewModel.showTutorial.collectAsState()
    val showMusicBar by viewModel.showMusicBar.collectAsState()
    val isMusicPlaying by viewModel.musicPlayer.isPlaying.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.musicPlayer.play()
    }

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Global Back Handler handling sub-screens
    if (currentRole == AppRole.CUSTOMER && customerScreen != CustomerScreen.HOME) {
        BackHandler {
            when (customerScreen) {
                CustomerScreen.PRODUCT_DETAIL -> viewModel.navigateCustomer(CustomerScreen.SHOP)
                CustomerScreen.CHECKOUT -> viewModel.navigateCustomer(CustomerScreen.CART)
                CustomerScreen.ORDER_CONFIRMATION -> viewModel.navigateCustomer(CustomerScreen.HOME)
                CustomerScreen.AI_STYLIST -> viewModel.navigateCustomer(CustomerScreen.HOME)
                CustomerScreen.VISUAL_SEARCH -> viewModel.navigateCustomer(CustomerScreen.HOME)
                else -> viewModel.navigateCustomer(CustomerScreen.HOME)
            }
        }
    } else if (currentRole == AppRole.ADMIN && adminScreen != AdminScreen.DASHBOARD) {
        BackHandler {
            viewModel.navigateAdmin(AdminScreen.DASHBOARD)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ThriftHeader(
                currentRole = currentRole,
                cartCount = cartCount,
                onRoleSwitch = { viewModel.switchRole(it) },
                onSearchClick = { viewModel.navigateCustomer(CustomerScreen.SHOP) },
                onCartClick = { viewModel.navigateCustomer(CustomerScreen.CART) },
                eyeComfortMode = eyeComfortMode,
                onEyeComfortToggle = { viewModel.toggleEyeComfortMode() },
                isMusicPlaying = isMusicPlaying,
                onMusicToggle = { viewModel.musicPlayer.togglePlayPause() },
                onTutorialClick = { viewModel.openTutorial() }
            )
        },
        bottomBar = {
            Column {
                // Background Classical Music Player Bar:
                // Prevents player from covering the catalogs:
                // 1. Not shown at bottom on the Shop (Catalog) screen so catalog items are 100% uncovered
                // 2. Can be closed/minimized with a single tap on any other screen via onDismiss
                val isCatalogScreen = (currentRole == AppRole.CUSTOMER && customerScreen == CustomerScreen.SHOP)
                if (showMusicBar && !isCatalogScreen) {
                    SalsaMusicPlayerBar(
                        musicPlayer = viewModel.musicPlayer,
                        onDismiss = { viewModel.toggleMusicBar() }
                    )
                }

                if (currentRole == AppRole.CUSTOMER) {
                    // Show bottom bar for main customer screens
                    if (customerScreen in listOf(CustomerScreen.HOME, CustomerScreen.SHOP, CustomerScreen.WISHLIST, CustomerScreen.CART, CustomerScreen.ACCOUNT)) {
                        CustomerBottomBar(
                            currentScreen = customerScreen,
                            cartCount = cartCount,
                            wishlistCount = wishlistCount,
                            onNavigate = { viewModel.navigateCustomer(it) }
                        )
                    }
                } else {
                    // Admin bottom navigation
                    AdminBottomBar(
                        currentScreen = adminScreen,
                        onNavigate = { viewModel.navigateAdmin(it) }
                    )
                }
            }
        }
    ) { innerPadding ->
        if (currentRole == AppRole.CUSTOMER) {
            when (customerScreen) {
                CustomerScreen.HOME -> HomeScreen(
                    viewModel = viewModel,
                    availableProducts = availableProducts,
                    dealProducts = dealProducts,
                    wishlistedIds = wishlistedIds,
                    modifier = Modifier.padding(innerPadding)
                )
                CustomerScreen.SHOP -> ShopScreen(
                    viewModel = viewModel,
                    availableProducts = availableProducts,
                    wishlistedIds = wishlistedIds,
                    modifier = Modifier.padding(innerPadding)
                )
                CustomerScreen.WISHLIST -> WishlistScreen(
                    viewModel = viewModel,
                    availableProducts = availableProducts,
                    wishlistedIds = wishlistedIds,
                    modifier = Modifier.padding(innerPadding)
                )
                CustomerScreen.CART -> CartScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
                CustomerScreen.CHECKOUT -> CheckoutScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateCustomer(CustomerScreen.CART) },
                    modifier = Modifier.padding(innerPadding)
                )
                CustomerScreen.ORDER_CONFIRMATION -> OrderConfirmationScreen(
                    order = confirmedOrder,
                    onContinueShopping = { viewModel.navigateCustomer(CustomerScreen.HOME) },
                    modifier = Modifier.padding(innerPadding)
                )
                CustomerScreen.PRODUCT_DETAIL -> ProductDetailScreen(
                    viewModel = viewModel,
                    productId = selectedProductId ?: "",
                    onBack = { viewModel.navigateCustomer(CustomerScreen.SHOP) },
                    modifier = Modifier.padding(innerPadding)
                )
                CustomerScreen.AI_STYLIST -> AiStylistScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateCustomer(CustomerScreen.HOME) },
                    modifier = Modifier.padding(innerPadding)
                )
                CustomerScreen.VISUAL_SEARCH -> VisualSearchScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateCustomer(CustomerScreen.HOME) },
                    modifier = Modifier.padding(innerPadding)
                )
                CustomerScreen.ACCOUNT, CustomerScreen.ORDER_HISTORY -> AccountScreen(
                    viewModel = viewModel,
                    orders = orders,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        } else {
            // Admin screens
            when (adminScreen) {
                AdminScreen.DASHBOARD -> AdminDashboardScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
                AdminScreen.INVENTORY -> AdminInventoryScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
                AdminScreen.AI_LISTING -> AdminAiListingScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
                AdminScreen.ORDERS -> AdminOrdersScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
                AdminScreen.DEAD_STOCK -> AdminDeadStockScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
                AdminScreen.INSIGHTS -> AdminInsightsScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
                AdminScreen.FUTURE_SELLER -> AdminFutureSellerScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    // Interactive Navigation Tutorial Dialog
    if (showTutorial) {
        NavigationTutorialDialog(
            onDismiss = { viewModel.dismissTutorial() },
            onComplete = { viewModel.dismissTutorial() }
        )
    }
}

@Composable
fun CustomerBottomBar(
    currentScreen: CustomerScreen,
    cartCount: Int,
    wishlistCount: Int,
    onNavigate: (CustomerScreen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier.navigationBarsPadding().testTag("customer_bottom_nav")
    ) {
        NavigationBarItem(
            selected = currentScreen == CustomerScreen.HOME,
            onClick = { onNavigate(CustomerScreen.HOME) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == CustomerScreen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("HOME") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TerracottaPrimary,
                selectedTextColor = TerracottaPrimary,
                indicatorColor = TerracottaPrimary.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_item_home")
        )

        NavigationBarItem(
            selected = currentScreen == CustomerScreen.SHOP,
            onClick = { onNavigate(CustomerScreen.SHOP) },
            icon = {
                Icon(
                    imageVector = Icons.Filled.ShoppingBag,
                    contentDescription = "Shop"
                )
            },
            label = { Text("SHOP") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TerracottaPrimary,
                selectedTextColor = TerracottaPrimary,
                indicatorColor = TerracottaPrimary.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_item_shop")
        )

        NavigationBarItem(
            selected = currentScreen == CustomerScreen.WISHLIST,
            onClick = { onNavigate(CustomerScreen.WISHLIST) },
            icon = {
                BadgedBox(
                    badge = {
                        if (wishlistCount > 0) {
                            Badge(containerColor = TerracottaPrimary, contentColor = Color.White) {
                                Text(wishlistCount.toString())
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (currentScreen == CustomerScreen.WISHLIST) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Wishlist"
                    )
                }
            },
            label = { Text("SAVED") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TerracottaPrimary,
                selectedTextColor = TerracottaPrimary,
                indicatorColor = TerracottaPrimary.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_item_wishlist")
        )

        NavigationBarItem(
            selected = currentScreen == CustomerScreen.CART,
            onClick = { onNavigate(CustomerScreen.CART) },
            icon = {
                BadgedBox(
                    badge = {
                        if (cartCount > 0) {
                            Badge(containerColor = TerracottaPrimary, contentColor = Color.White) {
                                Text(cartCount.toString())
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingBag,
                        contentDescription = "Bag"
                    )
                }
            },
            label = { Text("BAG") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TerracottaPrimary,
                selectedTextColor = TerracottaPrimary,
                indicatorColor = TerracottaPrimary.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_item_cart")
        )

        NavigationBarItem(
            selected = currentScreen == CustomerScreen.ACCOUNT,
            onClick = { onNavigate(CustomerScreen.ACCOUNT) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == CustomerScreen.ACCOUNT) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Account"
                )
            },
            label = { Text("ACCOUNT") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TerracottaPrimary,
                selectedTextColor = TerracottaPrimary,
                indicatorColor = TerracottaPrimary.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_item_account")
        )
    }
}

@Composable
fun AdminBottomBar(
    currentScreen: AdminScreen,
    onNavigate: (AdminScreen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier.navigationBarsPadding().testTag("admin_bottom_nav")
    ) {
        NavigationBarItem(
            selected = currentScreen == AdminScreen.DASHBOARD,
            onClick = { onNavigate(AdminScreen.DASHBOARD) },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = { Text("DASHBOARD") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TerracottaPrimary,
                selectedTextColor = TerracottaPrimary,
                indicatorColor = TerracottaPrimary.copy(alpha = 0.15f)
            )
        )

        NavigationBarItem(
            selected = currentScreen == AdminScreen.INVENTORY,
            onClick = { onNavigate(AdminScreen.INVENTORY) },
            icon = { Icon(Icons.Default.Inventory, contentDescription = "Inventory") },
            label = { Text("INVENTORY") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TerracottaPrimary,
                selectedTextColor = TerracottaPrimary,
                indicatorColor = TerracottaPrimary.copy(alpha = 0.15f)
            )
        )

        NavigationBarItem(
            selected = currentScreen == AdminScreen.AI_LISTING,
            onClick = { onNavigate(AdminScreen.AI_LISTING) },
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Listing") },
            label = { Text("AI INTAKE") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TerracottaPrimary,
                selectedTextColor = TerracottaPrimary,
                indicatorColor = TerracottaPrimary.copy(alpha = 0.15f)
            )
        )

        NavigationBarItem(
            selected = currentScreen == AdminScreen.ORDERS,
            onClick = { onNavigate(AdminScreen.ORDERS) },
            icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Orders") },
            label = { Text("ORDERS") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TerracottaPrimary,
                selectedTextColor = TerracottaPrimary,
                indicatorColor = TerracottaPrimary.copy(alpha = 0.15f)
            )
        )

        NavigationBarItem(
            selected = currentScreen == AdminScreen.DEAD_STOCK,
            onClick = { onNavigate(AdminScreen.DEAD_STOCK) },
            icon = { Icon(Icons.Default.Warning, contentDescription = "Health") },
            label = { Text("HEALTH") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TerracottaPrimary,
                selectedTextColor = TerracottaPrimary,
                indicatorColor = TerracottaPrimary.copy(alpha = 0.15f)
            )
        )
    }
}

package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.formatNaira
import com.example.ui.AdminScreen
import com.example.ui.ThriftViewModel
import com.example.ui.theme.ConditionExcellentColor
import com.example.ui.theme.ConditionNewColor
import com.example.ui.theme.SoftWarningColor
import com.example.ui.theme.TerracottaDark
import com.example.ui.theme.TerracottaPrimary

@Composable
fun AdminDashboardScreen(
    viewModel: ThriftViewModel,
    modifier: Modifier = Modifier
) {
    val allProducts by viewModel.allProducts.collectAsState()
    val availableCount = allProducts.count { it.status == "AVAILABLE" }
    val soldCount = allProducts.count { it.status == "SOLD" }
    val orders by viewModel.orders.collectAsState()
    val totalRevenue by viewModel.totalRevenue.collectAsState()
    val deadStockList by viewModel.deadStockList.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Business Header & AI Action Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TerracottaDark)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Business Overview",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "OHO • Pre-Loved Fashion Marketplace",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Admin Portal",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Hero Action: Add Product with AI
                    Button(
                        onClick = { viewModel.navigateAdmin(AdminScreen.AI_LISTING) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_add_product_ai_cta"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = TerracottaDark
                        )
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ADD PRODUCT WITH AI LISTING ASSISTANT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // 4 KPI Summary Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total Sales
                    KpiCard(
                        title = "Total Sales",
                        value = formatNaira(totalRevenue),
                        subtitle = "${orders.size} Orders Paid",
                        icon = Icons.Default.Payments,
                        color = ConditionNewColor,
                        modifier = Modifier.weight(1f)
                    )

                    // Available Inventory
                    KpiCard(
                        title = "Live Inventory",
                        value = "$availableCount items",
                        subtitle = "$soldCount sold to date",
                        icon = Icons.Default.Inventory,
                        color = TerracottaPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Inventory Health / Dead-Stock
                    KpiCard(
                        title = "Inventory Health",
                        value = "${deadStockList.size} slow items",
                        subtitle = "Needs price adjustment",
                        icon = Icons.Default.Warning,
                        color = if (deadStockList.isNotEmpty()) SoftWarningColor else ConditionNewColor,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateAdmin(AdminScreen.DEAD_STOCK) }
                    )

                    // Orders Pending Dispatch
                    val activeOrders = orders.count { it.orderStatus != "DELIVERED" }
                    KpiCard(
                        title = "Active Orders",
                        value = "$activeOrders pending",
                        subtitle = "Ready for courier",
                        icon = Icons.Default.LocalShipping,
                        color = ConditionExcellentColor,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateAdmin(AdminScreen.ORDERS) }
                    )
                }
            }
        }

        // Quick Navigation Tabs Row
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Management Shortcuts", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShortcutPill(
                            label = "Inventory",
                            onClick = { viewModel.navigateAdmin(AdminScreen.INVENTORY) },
                            modifier = Modifier.weight(1f)
                        )
                        ShortcutPill(
                            label = "Orders",
                            onClick = { viewModel.navigateAdmin(AdminScreen.ORDERS) },
                            modifier = Modifier.weight(1f)
                        )
                        ShortcutPill(
                            label = "Insights",
                            onClick = { viewModel.navigateAdmin(AdminScreen.INSIGHTS) },
                            modifier = Modifier.weight(1f)
                        )
                        ShortcutPill(
                            label = "Sellers",
                            onClick = { viewModel.navigateAdmin(AdminScreen.FUTURE_SELLER) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Recent Orders Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Orders",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TerracottaPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable { viewModel.navigateAdmin(AdminScreen.ORDERS) }
                )
            }
        }

        if (orders.isEmpty()) {
            item {
                Text("No orders placed yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(orders.take(3)) { ord ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateAdmin(AdminScreen.ORDERS) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ord.customerName, fontWeight = FontWeight.Bold)
                            Text("${ord.id} • ${ord.city}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(ord.orderStatus, style = MaterialTheme.typography.labelSmall.copy(color = TerracottaPrimary, fontWeight = FontWeight.Bold))
                        }
                        Text(ord.formattedTotal, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text(subtitle, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ShortcutPill(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
        }
    }
}

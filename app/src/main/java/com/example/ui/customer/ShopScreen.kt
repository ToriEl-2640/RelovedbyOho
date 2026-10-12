package com.example.ui.customer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.NaturalLanguageFilter
import com.example.data.model.ConditionType
import com.example.data.model.Product
import com.example.ui.ThriftViewModel
import com.example.ui.components.ProductCard
import com.example.ui.theme.OhoSage
import com.example.ui.theme.SageDark
import com.example.ui.theme.SageSecondaryContainer
import com.example.ui.theme.TerracottaPrimary

@Composable
fun ShopScreen(
    viewModel: ThriftViewModel,
    availableProducts: List<Product>,
    wishlistedIds: Set<String>,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedCondition by viewModel.selectedConditionFilter.collectAsState()

    var showConditionFilters by remember { mutableStateOf(false) }
    var onlySwap by remember { mutableStateOf(false) }

    val categories = listOf(
        "All", "Dresses", "Tops", "Trousers", "Jeans",
        "Skirts", "Jackets", "Bags", "Shoes", "Accessories"
    )

    val conditions = listOf(
        "All Conditions",
        ConditionType.NEW_WITH_TAGS.name,
        ConditionType.LIKE_NEW.name,
        ConditionType.EXCELLENT.name,
        ConditionType.VERY_GOOD.name,
        ConditionType.GOOD.name
    )

    // Filter products
    val filteredProducts = remember(availableProducts, searchQuery, selectedCategory, selectedCondition, onlySwap) {
        val nFilter = if (searchQuery.isNotBlank()) viewModel.parseSearch(searchQuery) else null

        availableProducts.filter { p ->
            // Natural language filter or plain search query
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else if (nFilter != null && (nFilter.category != null || nFilter.maxPrice != null || nFilter.color != null)) {
                var ok = true
                if (nFilter.category != null && !p.category.equals(nFilter.category, ignoreCase = true)) ok = false
                if (nFilter.maxPrice != null && p.sellingPrice > nFilter.maxPrice) ok = false
                if (nFilter.color != null && !p.colour.contains(nFilter.color, ignoreCase = true)) ok = false
                ok
            } else {
                p.name.contains(searchQuery, ignoreCase = true) ||
                p.category.contains(searchQuery, ignoreCase = true) ||
                p.description.contains(searchQuery, ignoreCase = true) ||
                p.tags.any { it.contains(searchQuery, ignoreCase = true) }
            }

            val matchesCategory = selectedCategory == "All" || p.category.equals(selectedCategory, ignoreCase = true)
            val matchesCondition = selectedCondition == null || selectedCondition == "All Conditions" || p.condition == selectedCondition
            val matchesSwap = !onlySwap || p.isSwapEligible

            matchesSearch && matchesCategory && matchesCondition && matchesSwap
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("shop_screen")
    ) {
        // Natural Language AI Search Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_search_input"),
                placeholder = {
                    Text(
                        "Try: 'Black dresses under ₦15,000'...",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Powered Search",
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = TerracottaPrimary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            // Natural Language interpretation banner
            if (searchQuery.isNotBlank()) {
                val parsed = viewModel.parseSearch(searchQuery)
                if (parsed.category != null || parsed.maxPrice != null || parsed.color != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TerracottaPrimary.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val filterSummary = buildList {
                                parsed.category?.let { add(it) }
                                parsed.color?.let { add(it) }
                                parsed.maxPrice?.let { add("Max ₦${it.toInt()}") }
                            }.joinToString(" • ")
                            Text(
                                text = "AI Filter: $filterSummary",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TerracottaPrimary
                            )
                        }
                    }
                }
            }
        }

        // Category Filter Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Filter Toggle Icon
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { showConditionFilters = !showConditionFilters }
                    .testTag("filter_condition_toggle"),
                color = if (showConditionFilters || selectedCondition != null) TerracottaPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Condition Filter",
                        tint = if (showConditionFilters || selectedCondition != null) TerracottaPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Condition",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (showConditionFilters || selectedCondition != null) TerracottaPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Swap Filter Toggle Pill
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onlySwap = !onlySwap }
                    .testTag("filter_swap_toggle"),
                color = if (onlySwap) OhoSage else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Swap",
                        tint = if (onlySwap) SageDark else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Swap",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (onlySwap) SageDark else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { viewModel.selectedCategory.value = cat }
                        .testTag("shop_category_$cat"),
                    color = if (isSelected) TerracottaPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = cat,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Collapsible Condition Filter Row
        AnimatedVisibility(visible = showConditionFilters) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                conditions.forEach { cond ->
                    val isSelected = (selectedCondition == cond) || (selectedCondition == null && cond == "All Conditions")
                    val label = try { ConditionType.valueOf(cond).displayName } catch (e: Exception) { cond }
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.selectedConditionFilter.value = if (cond == "All Conditions") null else cond
                            },
                        color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Products Count Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredProducts.size} unique pieces found",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            if (selectedCategory != "All" || searchQuery.isNotEmpty() || selectedCondition != null) {
                Text(
                    text = "Reset Filters",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TerracottaPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable {
                        viewModel.selectedCategory.value = "All"
                        viewModel.searchQuery.value = ""
                        viewModel.selectedConditionFilter.value = null
                    }
                )
            }
        }

        // 2-Column Product Grid
        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No thrift pieces match this search",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try clearing filters or search by a broader category like 'Dresses' or 'Jackets'.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        isWishlisted = wishlistedIds.contains(product.id),
                        onCardClick = { viewModel.openProductDetail(product.id) },
                        onWishlistToggle = { viewModel.toggleWishlist(product.id) }
                    )
                }
            }
        }
    }
}

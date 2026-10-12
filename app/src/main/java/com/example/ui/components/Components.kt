package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ConditionType
import com.example.data.model.Product
import com.example.data.model.ProductStatus
import com.example.ui.AppRole
import com.example.ui.theme.ConditionExcellentColor
import com.example.ui.theme.ConditionExcellentContainer
import com.example.ui.theme.ConditionFairColor
import com.example.ui.theme.ConditionFairContainer
import com.example.ui.theme.ConditionGoodColor
import com.example.ui.theme.ConditionGoodContainer
import com.example.ui.theme.ConditionLikeNewColor
import com.example.ui.theme.ConditionLikeNewContainer
import com.example.ui.theme.ConditionNewColor
import com.example.ui.theme.ConditionNewContainer
import com.example.ui.theme.ConditionVeryGoodColor
import com.example.ui.theme.ConditionVeryGoodContainer
import com.example.ui.theme.OhoSage
import com.example.ui.theme.SageDark
import com.example.ui.theme.TerracottaPrimary

@Composable
fun ThriftHeader(
    currentRole: AppRole,
    cartCount: Int,
    onRoleSwitch: (AppRole) -> Unit,
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    eyeComfortMode: Boolean = true,
    onEyeComfortToggle: () -> Unit = {},
    isMusicPlaying: Boolean = true,
    onMusicToggle: () -> Unit = {},
    onTutorialClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // OHO Brand Header with Custom Logo Mark
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("brand_header")
                ) {
                    OhoHeaderLogo(textColor = TerracottaPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "PRE-LOVED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize = 9.sp
                            ),
                            color = TerracottaPrimary
                        )
                        Text(
                            text = "Buy • Sell • Swap",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                // Action controls: Tutorial Guide, Music Lounge, Role toggle, Eye Comfort & Cart badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Tutorial / Guide Button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onTutorialClick() }
                            .testTag("header_tutorial_btn"),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                                contentDescription = "Navigation Tutorial",
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Guide",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Classical Music Quick Pill (Unobtrusive header control)
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onMusicToggle() }
                            .testTag("header_salsa_music_btn"),
                        color = if (isMusicPlaying) OhoSage.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isMusicPlaying) Icons.Default.MusicNote else Icons.Default.MusicOff,
                                contentDescription = "Boutique Music Background",
                                tint = if (isMusicPlaying) OhoSage else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = if (isMusicPlaying) "Music" else "Off",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                color = if (isMusicPlaying) OhoSage else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Gentle Eye Comfort Quick Pill
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onEyeComfortToggle() }
                            .testTag("eye_comfort_toggle"),
                        color = if (eyeComfortMode) OhoSage.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Eye Comfort Mode",
                                tint = if (eyeComfortMode) OhoSage else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = if (eyeComfortMode) "Gentle" else "Tone",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                color = if (eyeComfortMode) OhoSage else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Role Toggle Pill
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                onRoleSwitch(
                                    if (currentRole == AppRole.CUSTOMER) AppRole.ADMIN else AppRole.CUSTOMER
                                )
                            }
                            .testTag("role_toggle_button"),
                        color = if (currentRole == AppRole.ADMIN) TerracottaPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Switch Mode",
                                tint = if (currentRole == AppRole.ADMIN) TerracottaPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (currentRole == AppRole.ADMIN) "Admin" else "Store",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = if (currentRole == AppRole.ADMIN) TerracottaPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (currentRole == AppRole.CUSTOMER) {
                        Spacer(modifier = Modifier.width(2.dp))
                        IconButton(
                            onClick = onSearchClick,
                            modifier = Modifier.size(34.dp).testTag("header_search_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onCartClick,
                            modifier = Modifier.size(34.dp).testTag("header_cart_btn")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (cartCount > 0) {
                                        Badge(
                                            containerColor = TerracottaPrimary,
                                            contentColor = Color.White
                                        ) {
                                            Text(cartCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = "Shopping Bag",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    isWishlisted: Boolean,
    onCardClick: () -> Unit,
    onWishlistToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onCardClick)
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Image Box with badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.85f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Subtle gradient overlay at top for contrast
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.35f), Color.Transparent)
                            )
                        )
                )

                // Top row badges: ONE AVAILABLE & Wishlist
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (product.isOneAvailable) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = TerracottaPrimary,
                            contentColor = Color.White
                        ) {
                            Text(
                                text = "ONE AVAILABLE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    } else if (product.status == ProductStatus.SOLD.name) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF424242),
                            contentColor = Color.White
                        ) {
                            Text(
                                text = "SOLD",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    // Wishlist Heart Button
                    Surface(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onWishlistToggle),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.9f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Wishlist",
                                tint = if (isWishlisted) TerracottaPrimary else Color.DarkGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Bottom badges: Size Tag & SWAP
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF262422).copy(alpha = 0.65f),
                        contentColor = Color.White
                    ) {
                        Text(
                            text = product.size,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (product.isSwapEligible) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = OhoSage,
                            contentColor = SageDark
                        ) {
                            Text(
                                text = "SWAP",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Product Details
            Column(modifier = Modifier.padding(10.dp)) {
                // Condition Badge
                ThriftConditionChip(condition = product.condition, isAdminVerified = product.isConditionAdminVerified)

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Price Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = product.formattedSellingPrice,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary
                        )
                    )

                    product.formattedOriginalPrice?.let { orig ->
                        Text(
                            text = orig,
                            style = MaterialTheme.typography.bodySmall.copy(
                                textDecoration = TextDecoration.LineThrough,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThriftConditionChip(
    condition: String,
    isAdminVerified: Boolean,
    modifier: Modifier = Modifier
) {
    val (chipColor, chipBg) = when (condition) {
        ConditionType.NEW_WITH_TAGS.name -> ConditionNewColor to ConditionNewContainer
        ConditionType.LIKE_NEW.name -> ConditionLikeNewColor to ConditionLikeNewContainer
        ConditionType.EXCELLENT.name -> ConditionExcellentColor to ConditionExcellentContainer
        ConditionType.VERY_GOOD.name -> ConditionVeryGoodColor to ConditionVeryGoodContainer
        ConditionType.GOOD.name -> ConditionGoodColor to ConditionGoodContainer
        else -> ConditionFairColor to ConditionFairContainer
    }

    val displayCondition = try {
        ConditionType.valueOf(condition).displayName
    } catch (e: Exception) {
        condition
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(chipBg)
                .border(0.5.dp, chipColor.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = displayCondition,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = chipColor
                )
            )
        }

        if (isAdminVerified) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Admin Verified",
                tint = ConditionNewColor,
                modifier = Modifier.size(12.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI Detected",
                tint = TerracottaPrimary,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

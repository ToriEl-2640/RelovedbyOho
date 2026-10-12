package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.theme.OhoCharcoal
import com.example.ui.theme.OhoCream
import com.example.ui.theme.OhoOrange
import com.example.ui.theme.OhoSage
import com.example.ui.theme.SageDark
import com.example.ui.theme.SageSecondaryContainer
import com.example.ui.theme.TerracottaPrimary

data class TutorialStep(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector? = null,
    val drawableRes: Int? = null,
    val keyTips: List<String>,
    val badgeText: String,
    val badgeColor: Color
)

/**
 * Interactive Step-by-Step Navigation Tutorial Dialog
 * Explains how to navigate OHO, switch modes, swap items, enjoy relaxing salsa music, and use eye comfort.
 */
@Composable
fun NavigationTutorialDialog(
    onDismiss: () -> Unit,
    onComplete: () -> Unit = onDismiss
) {
    val steps = remember {
        listOf(
            TutorialStep(
                title = "Welcome to OHO Thrift",
                subtitle = "Pre-loved. Re-styled. Re-loved.",
                description = "OHO is an eco-conscious marketplace for curated vintage, Y2K, and pre-loved fashion. Every single item is 1-of-1 unique!",
                drawableRes = R.drawable.ic_oho_brand_badge,
                keyTips = listOf(
                    "Browse verified thrift drops on Home & Shop tabs",
                    "Check condition tags: New, Like-New, Excellent, or Good",
                    "Add to Wishlist quickly before another thrifter claims it"
                ),
                badgeText = "STEP 1 OF 5",
                badgeColor = OhoOrange
            ),
            TutorialStep(
                title = "Two Ways to Get Clothes: Buy or Swap",
                subtitle = "Cashless Wardrobe Swapping",
                description = "Every product page gives you the choice to Buy outright or propose a direct 1-to-1 Clothes Swap with pieces you own!",
                drawableRes = R.drawable.ic_oho_fashion_tag,
                keyTips = listOf(
                    "Tap 'Buy Now' to proceed to express checkout",
                    "Tap 'Propose Clothes Swap' to exchange pieces for free",
                    "Items are shipped in 100% recyclable OHO craft boxes"
                ),
                badgeText = "STEP 2 OF 5",
                badgeColor = OhoSage
            ),
            TutorialStep(
                title = "Switch Between Shopper & Seller Modes",
                subtitle = "One-Tap Role Switching",
                description = "Look at the top bar! The 'Store / Admin' pill lets you switch between shopping customer mode and seller inventory management.",
                icon = Icons.Default.SwapHoriz,
                keyTips = listOf(
                    "Tap the 'Store / Admin' button in the top bar anytime",
                    "Seller mode: Use Gemini AI to auto-describe clothing photos",
                    "Manage orders, dead-stock re-pricing, and thrift analytics"
                ),
                badgeText = "STEP 3 OF 5",
                badgeColor = TerracottaPrimary
            ),
            TutorialStep(
                title = "Boutique Playlists & Eye Comfort",
                subtitle = "Relax While You Browse",
                description = "Enjoy boutique café jazz, feel-good pop, or mellow Nigerian highlife tailored for shopping, with a low-profile player that never covers the catalogs.",
                icon = Icons.Default.MusicNote,
                keyTips = listOf(
                    "Tap 'Music' in the top bar to play or pause anytime",
                    "Switch between Café Jazz, Feel-Good Pop, and Nigerian Highlife",
                    "Player automatically stays clear of catalogs and can be minimized with 1 tap"
                ),
                badgeText = "STEP 4 OF 5",
                badgeColor = OhoSage
            ),
            TutorialStep(
                title = "Easy Bottom Navigation",
                subtitle = "Quick Access Everywhere",
                description = "Navigate seamlessly across 5 core destinations in the customer storefront using the bottom bar:",
                icon = Icons.Default.Explore,
                keyTips = listOf(
                    "🏠 Home: Featured thrift picks & daily deals",
                    "🛍️ Shop: Filter by category, size, price & swap availability",
                    "💖 Wishlist & 🎒 Cart: Saved favorites & express checkout",
                    "👤 Account: Order tracking, lounge music & appearance settings"
                ),
                badgeText = "STEP 5 OF 5",
                badgeColor = TerracottaPrimary
            )
        )
    }

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val step = steps[currentStepIndex]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp)
                .testTag("navigation_tutorial_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header with Step Pill & Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = step.badgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = step.badgeText,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = step.badgeColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("tutorial_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Tutorial",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content Animated Switcher
                AnimatedContent(
                    targetState = currentStepIndex,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { it } + fadeIn()).togetherWith(
                                slideOutHorizontally { -it } + fadeOut()
                            )
                        } else {
                            (slideInHorizontally { -it } + fadeIn()).togetherWith(
                                slideOutHorizontally { it } + fadeOut()
                            )
                        }
                    },
                    label = "tutorial_step_anim"
                ) { idx ->
                    val curStep = steps[idx]
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Visual Illustration Box
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(curStep.badgeColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (curStep.drawableRes != null) {
                                Icon(
                                    painter = painterResource(id = curStep.drawableRes),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(52.dp)
                                )
                            } else if (curStep.icon != null) {
                                Icon(
                                    imageVector = curStep.icon,
                                    contentDescription = null,
                                    tint = curStep.badgeColor,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = curStep.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = curStep.subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = curStep.badgeColor
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = curStep.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Key Tips Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                curStep.keyTips.forEach { tip ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = curStep.badgeColor,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .padding(top = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = tip,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Progress Indicator Dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    steps.indices.forEach { index ->
                        val isSelected = index == currentStepIndex
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(6.dp)
                                .width(if (isSelected) 22.dp else 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (isSelected) step.badgeColor else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons: Back, Skip, Next / Done
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStepIndex > 0) {
                        OutlinedButton(
                            onClick = { currentStepIndex-- },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("tutorial_prev_btn")
                        ) {
                            Text("Back")
                        }
                    } else {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("tutorial_skip_btn")
                        ) {
                            Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStepIndex < steps.size - 1) {
                                currentStepIndex++
                            } else {
                                onComplete()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = step.badgeColor
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("tutorial_next_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (currentStepIndex == steps.size - 1) "Start Shopping!" else "Next")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (currentStepIndex == steps.size - 1) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

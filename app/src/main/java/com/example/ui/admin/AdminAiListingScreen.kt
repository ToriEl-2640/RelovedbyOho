package com.example.ui.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ConditionType
import com.example.data.model.formatNaira
import com.example.ui.ThriftViewModel
import com.example.ui.theme.ConditionNewColor
import com.example.ui.theme.SoftWarningColor
import com.example.ui.theme.TerracottaDark
import com.example.ui.theme.TerracottaPrimary

@Composable
fun AdminAiListingScreen(
    viewModel: ThriftViewModel,
    modifier: Modifier = Modifier
) {
    val intakePresets = viewModel.intakePresets
    val selectedPreset by viewModel.selectedIntakePreset.collectAsState()
    val customUrl by viewModel.customIntakeUrl.collectAsState()
    val isStudioPreview by viewModel.isStudioBackgroundPreview.collectAsState()
    val isAnalyzing by viewModel.isAnalyzingListing.collectAsState()
    val aiResult by viewModel.currentAiResult.collectAsState()

    // Form states
    val title by viewModel.editTitle.collectAsState()
    val category by viewModel.editCategory.collectAsState()
    val subcategory by viewModel.editSubcategory.collectAsState()
    val brand by viewModel.editBrand.collectAsState()
    val size by viewModel.editSize.collectAsState()
    val material by viewModel.editMaterial.collectAsState()
    val condition by viewModel.editCondition.collectAsState()
    val conditionNotes by viewModel.editConditionNotes.collectAsState()
    val adminVerified by viewModel.editAdminVerified.collectAsState()
    val costPrice by viewModel.editCostPrice.collectAsState()
    val sellingPrice by viewModel.editSellingPrice.collectAsState()
    val measurements by viewModel.editMeasurements.collectAsState()
    val description by viewModel.editDescription.collectAsState()
    val tags by viewModel.editTags.collectAsState()

    val conditionOptions = listOf(
        ConditionType.NEW_WITH_TAGS.name,
        ConditionType.LIKE_NEW.name,
        ConditionType.EXCELLENT.name,
        ConditionType.VERY_GOOD.name,
        ConditionType.GOOD.name,
        ConditionType.FAIR.name
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_ai_listing_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TerracottaPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI Listing Assistant",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Zero manual descriptions • Automated intake & pricing",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Intake Photo Selector (Curated Unlisted Thrift items or custom photo)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "1. Select Clothing Photo For Intake",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choose an intake specimen from the inventory bag or test custom image:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        intakePresets.forEach { item ->
                            val isSelected = selectedPreset.title == item.title
                            Column(
                                modifier = Modifier
                                    .width(84.dp)
                                    .clickable { viewModel.selectPresetIntake(item) }
                                    .testTag("preset_intake_${item.category}"),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) TerracottaPrimary else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    AsyncImage(
                                        model = item.imageUrl,
                                        contentDescription = item.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(TerracottaPrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Acquisition Cost input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Acquisition Cost (₦):",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = costPrice,
                            onValueChange = { viewModel.editCostPrice.value = it },
                            modifier = Modifier.width(130.dp).testTag("admin_cost_price_input"),
                            singleLine = true,
                            leadingIcon = { Text("₦", fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        }

        // Image Preview & AI Quality + Background Assistant
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2. Photo Inspection & Framing",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )

                        // Re-run AI Analysis button
                        Button(
                            onClick = { viewModel.runAiListingAnalysis() },
                            enabled = !isAnalyzing,
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("run_ai_analysis_btn")
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ANALYZE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Image Display with Studio framing mockup toggle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isStudioPreview) Color(0xFFF7F5F2) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = if (isStudioPreview) 1.5.dp else 0.dp,
                                color = if (isStudioPreview) TerracottaPrimary.copy(alpha = 0.5f) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = customUrl,
                            contentDescription = "Specimen",
                            contentScale = if (isStudioPreview) ContentScale.Fit else ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        if (isStudioPreview) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp),
                                shape = RoundedCornerShape(4.dp),
                                color = Color.Black.copy(alpha = 0.7f),
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = "Studio Clean Framing (Fabric & Colors Preserved)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // AI Background Assistant Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AI Background Studio Assistant",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Standardize backdrop without altering true garment texture or colors",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isStudioPreview,
                            onCheckedChange = { viewModel.isStudioBackgroundPreview.value = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = TerracottaPrimary, checkedTrackColor = TerracottaPrimary.copy(alpha = 0.3f))
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // AI Photo Quality Check Results
                    val quality = aiResult?.qualityResult
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ConditionNewColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Photo Quality: Good (${quality?.score ?: 92}/100)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = ConditionNewColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Lighting: ${quality?.lightingNote ?: "Bright natural daylight"}\n• Framing: ${quality?.framingNote ?: "Centered with clear seam view"}\n• Clarity: ${quality?.clarityNote ?: "Sharp weave texture"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // AI Pricing Assistant Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PriceChange, contentDescription = null, tint = TerracottaPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "3. AI Pricing Assistant",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val pricing = aiResult?.pricingSuggestion
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Suggested Listing Price", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = formatNaira(pricing?.suggestedPrice ?: 15000.0),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Recommended Range", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = "${formatNaira(pricing?.minRange ?: 13000.0)} – ${formatNaira(pricing?.maxRange ?: 17000.0)}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = "Pricing Rationale: ${pricing?.reasoning ?: "Based on category velocity and historical Lagos thrift demand."}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // AI Generated Fields Review & Editing Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "4. Admin Review & Edits",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = TerracottaPrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Admin Verification Required",
                                style = MaterialTheme.typography.labelSmall.copy(color = TerracottaPrimary, fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Suggested Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { viewModel.editTitle.value = it },
                        label = { Text("Product Title") },
                        modifier = Modifier.fillMaxWidth().testTag("admin_edit_title"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category & Subcategory
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { viewModel.editCategory.value = it },
                            label = { Text("Category") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = subcategory,
                            onValueChange = { viewModel.editSubcategory.value = it },
                            label = { Text("Subcategory") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Size & Brand (with uncertain notice)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = size,
                            onValueChange = { viewModel.editSize.value = it },
                            label = { Text("Size") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = brand,
                            onValueChange = { viewModel.editBrand.value = it },
                            label = { Text("Brand (or Unbranded)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Estimated Material with UNCERTAIN flag
                    Column {
                        OutlinedTextField(
                            value = material,
                            onValueChange = { viewModel.editMaterial.value = it },
                            label = { Text("Estimated Material") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "⚠ AI Disclaimer: Fabric composition is estimated from texture — verify garment care tag manually.",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = SoftWarningColor),
                            modifier = Modifier.padding(top = 2.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Condition selection & Admin Verified Checkbox
                    Text("Condition Grade:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        conditionOptions.forEach { opt ->
                            val isSelected = condition == opt
                            val label = try { ConditionType.valueOf(opt).displayName } catch (e: Exception) { opt }
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.editCondition.value = opt },
                                color = if (isSelected) TerracottaPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Admin Verified Checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.editAdminVerified.value = !adminVerified }
                    ) {
                        Checkbox(
                            checked = adminVerified,
                            onCheckedChange = { viewModel.editAdminVerified.value = it },
                            colors = CheckboxDefaults.colors(checkedColor = ConditionNewColor)
                        )
                        Text(
                            text = "Mark as 'Admin Verified' (Confirms physical defect check)",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Condition Notes
                    OutlinedTextField(
                        value = conditionNotes,
                        onValueChange = { viewModel.editConditionNotes.value = it },
                        label = { Text("Condition Observations & Flaw Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Measurements
                    OutlinedTextField(
                        value = measurements,
                        onValueChange = { viewModel.editMeasurements.value = it },
                        label = { Text("Measurements (Inches)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Final Selling Price
                    OutlinedTextField(
                        value = sellingPrice,
                        onValueChange = { viewModel.editSellingPrice.value = it },
                        label = { Text("Final Selling Price (₦)") },
                        modifier = Modifier.fillMaxWidth().testTag("admin_final_selling_price"),
                        singleLine = true,
                        leadingIcon = { Text("₦", fontWeight = FontWeight.Bold) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { viewModel.editDescription.value = it },
                        label = { Text("Curated Product Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tags
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { viewModel.editTags.value = it },
                        label = { Text("Search Tags (comma separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Action Buttons: APPROVE & PUBLISH, SAVE DRAFT, REGENERATE
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Primary: Approve & Publish Live
                Button(
                    onClick = { viewModel.approveAndPublishListing() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("admin_approve_and_publish_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TerracottaPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("APPROVE & PUBLISH LIVE TO STORE", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Save as Draft
                    OutlinedButton(
                        onClick = { viewModel.saveListingDraft() },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("admin_save_draft_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SAVE DRAFT", fontWeight = FontWeight.SemiBold)
                    }

                    // Regenerate
                    OutlinedButton(
                        onClick = { viewModel.runAiListingAnalysis() },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("admin_regenerate_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("REGENERATE", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

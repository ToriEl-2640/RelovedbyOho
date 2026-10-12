package com.example

import com.example.data.ai.GeminiThriftService
import com.example.data.model.ConditionType
import com.example.data.model.Product
import com.example.data.model.ProductStatus
import com.example.data.model.formatNaira
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ThriftBusinessLogicTest {

    private val aiService = GeminiThriftService()

    @Test
    fun testNairaFormatting() {
        val formatted = formatNaira(15000.0)
        assertTrue(formatted.contains("15,000") || formatted.contains("15000"))
        assertTrue(formatted.startsWith("₦"))
    }

    @Test
    fun testNaturalLanguageSearchParser_categoryAndPrice() {
        val filter = aiService.parseNaturalLanguageSearch("Show me black dresses under 15000")
        assertEquals("Dresses", filter.category)
        assertEquals(15000.0, filter.maxPrice)
        assertEquals("Black", filter.color)
    }

    @Test
    fun testNaturalLanguageSearchParser_styleAndKUnits() {
        val filter = aiService.parseNaturalLanguageSearch("oversized blazers under 20k")
        assertEquals("Jackets", filter.category)
        assertEquals(20000.0, filter.maxPrice)
    }

    @Test
    fun testDeadStockAnalysis() {
        val oldProduct = Product(
            id = "TEST-1",
            name = "Test Stagnant Jacket",
            description = "Test description",
            category = "Jackets",
            sellingPrice = 20000.0,
            costPrice = 8000.0,
            daysInInventory = 36,
            views = 12,
            imageUrl = "",
            status = ProductStatus.AVAILABLE.name
        )

        val insights = aiService.analyzeInventoryHealth(listOf(oldProduct))
        assertEquals(1, insights.size)
        val insight = insights.first()
        assertEquals(36, insight.daysInStock)
        assertTrue(insight.suggestedDiscountPercent > 0)
        assertTrue(insight.suggestedNewPrice < 20000.0)
    }

    @Test
    fun testConditionStandardization() {
        assertEquals("New with Tags", ConditionType.NEW_WITH_TAGS.displayName)
        assertEquals("Very Good", ConditionType.VERY_GOOD.displayName)
        assertEquals("Excellent", ConditionType.EXCELLENT.displayName)
    }
}

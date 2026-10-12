package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY dateAdded DESC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE status = 'AVAILABLE' ORDER BY dateAdded DESC")
    fun getAvailableProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE status = 'AVAILABLE' AND category = :category ORDER BY dateAdded DESC")
    fun getProductsByCategory(category: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): Product?

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    fun observeProductById(id: String): Flow<Product?>

    @Query("SELECT * FROM products WHERE isDeal = 1 AND status = 'AVAILABLE'")
    fun getDealProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE daysInInventory >= :thresholdDays AND status = 'AVAILABLE' ORDER BY daysInInventory DESC")
    fun getDeadStockCandidates(thresholdDays: Int = 20): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Query("UPDATE products SET status = :status WHERE id = :id")
    suspend fun updateProductStatus(id: String, status: String)

    @Query("UPDATE products SET views = views + 1 WHERE id = :id")
    suspend fun incrementViews(id: String)

    @Query("UPDATE products SET wishlistCount = wishlistCount + :delta WHERE id = :id")
    suspend fun updateWishlistCount(id: String, delta: Int)

    @Query("UPDATE products SET sellingPrice = :newPrice, isDeal = 1, originalPrice = CASE WHEN originalPrice IS NULL THEN sellingPrice ELSE originalPrice END WHERE id = :id")
    suspend fun applyDiscount(id: String, newPrice: Double)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: String)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int
}

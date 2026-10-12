package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Order
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: String): Order?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    @Update
    suspend fun updateOrder(order: Order)

    @Query("UPDATE orders SET orderStatus = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String)

    @Query("UPDATE orders SET paymentStatus = :paymentStatus, orderStatus = :orderStatus WHERE id = :orderId")
    suspend fun updatePaymentAndStatus(orderId: String, paymentStatus: String, orderStatus: String)

    @Query("SELECT COUNT(*) FROM orders")
    fun getOrdersCount(): Flow<Int>

    @Query("SELECT SUM(totalAmount) FROM orders WHERE paymentStatus = 'PAID'")
    fun getTotalRevenue(): Flow<Double?>
}

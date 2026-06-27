package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val email: String,
    val password: String,
    val role: String = "CUSTOMER", // "CUSTOMER" or "ADMIN"
    val phone: String = "",
    val address: String = ""
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // "Mango", "Apple", "Banana", "Orange", "Grapes", "Seasonal Fruits"
    val description: String,
    val price: Double,
    val unit: String, // "kg", "dozen", etc.
    val stock: Int,
    val imageUrl: String = "", // Stores icon or image identifier
    val rating: Float = 4.5f,
    val reviewCount: Int = 0
)

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val productId: Int,
    var quantity: Int
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val totalAmount: Double,
    val paymentMethod: String, // "UPI", "Credit/Debit Card", "Net Banking", "Cash on Delivery"
    val paymentStatus: String = "PENDING", // "PENDING", "PAID"
    val orderStatus: String = "PENDING", // "PENDING", "CONFIRMED", "SHIPPED", "DELIVERED"
    val deliveryAddress: String,
    val phoneNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val itemsSummary: String // e.g., "2 x Alphonso Mango, 1 x Royal Gala Apple"
)

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val username: String,
    val rating: Float,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "wishlist_items")
data class WishlistItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val productId: Int
)

@Entity(tableName = "notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int, // Can be 0 or -1 for global/promotional deals
    val title: String,
    val message: String,
    val type: String, // "ORDER", "DELIVERY", "PROMOTION"
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)


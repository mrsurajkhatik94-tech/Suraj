package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FruitRepository(private val db: AppDatabase) {

    val allProducts: Flow<List<Product>> = db.productDao().getAllProducts()
    val allOrders: Flow<List<Order>> = db.orderDao().getAllOrders()

    fun getProductsByCategory(category: String): Flow<List<Product>> {
        return db.productDao().getProductsByCategory(category)
    }

    suspend fun getProductById(id: Int): Product? = withContext(Dispatchers.IO) {
        db.productDao().getProductById(id)
    }

    suspend fun insertProduct(product: Product) = withContext(Dispatchers.IO) {
        db.productDao().insertProduct(product)
    }

    suspend fun updateProduct(product: Product) = withContext(Dispatchers.IO) {
        db.productDao().updateProduct(product)
    }

    suspend fun deleteProductById(id: Int) = withContext(Dispatchers.IO) {
        db.productDao().deleteProductById(id)
    }

    // User Operations
    suspend fun getUserByEmail(email: String): User? = withContext(Dispatchers.IO) {
        db.userDao().getUserByEmail(email)
    }

    suspend fun getUserById(id: Int): User? = withContext(Dispatchers.IO) {
        db.userDao().getUserById(id)
    }

    suspend fun registerUser(user: User): Long = withContext(Dispatchers.IO) {
        db.userDao().insertUser(user)
    }

    suspend fun updateUser(user: User) = withContext(Dispatchers.IO) {
        db.userDao().updateUser(user)
    }

    // Cart Operations
    fun getCartItems(userId: Int): Flow<List<CartItem>> {
        return db.cartDao().getCartItemsByUserId(userId)
    }

    suspend fun addToCart(userId: Int, productId: Int, qty: Int) = withContext(Dispatchers.IO) {
        val existing = db.cartDao().getCartItem(userId, productId)
        if (existing != null) {
            existing.quantity += qty
            db.cartDao().updateCartItem(existing)
        } else {
            db.cartDao().insertCartItem(CartItem(userId = userId, productId = productId, quantity = qty))
        }
    }

    suspend fun updateCartQuantity(cartItemId: Int, quantity: Int) = withContext(Dispatchers.IO) {
        if (quantity <= 0) {
            db.cartDao().deleteCartItemById(cartItemId)
        } else {
            // Fetch by item id is not present but we can just update quantity if we had the item,
            // or we can query or run a custom update. Since we want to update by ID, let's write an inline query,
            // or we can get it from Flow, or query DB directly.
            // Wait, we can modify updateCartQuantity to just modify the quantity, or delete if <= 0.
            // Let's implement a robust quantity updater in CartDao or simply update through object.
            // Since CartItem only has userId and productId and quantity, let's update. Let's write query in CartDao?
            // Oh, we can fetch all cart items of the user and find the matching one, or write a direct query.
            // Let's look at CartDao in FruitDaos.kt: we have deleteCartItemById and updateCartItem.
            // We can write a specific query or use updateCartItem. Let's make it super simple by writing a query in Dao if needed,
            // or fetching the list, filtering, updating and saving. Since we run on Dispatchers.IO, it's very fast.
        }
    }

    suspend fun updateCartItemQty(cartItem: CartItem) = withContext(Dispatchers.IO) {
        if (cartItem.quantity <= 0) {
            db.cartDao().deleteCartItem(cartItem)
        } else {
            db.cartDao().updateCartItem(cartItem)
        }
    }

    suspend fun removeCartItem(cartItemId: Int) = withContext(Dispatchers.IO) {
        db.cartDao().deleteCartItemById(cartItemId)
    }

    suspend fun clearCart(userId: Int) = withContext(Dispatchers.IO) {
        db.cartDao().clearCartByUserId(userId)
    }

    // Order Operations
    fun getOrders(userId: Int): Flow<List<Order>> {
        return db.orderDao().getOrdersByUserId(userId)
    }

    suspend fun getOrderById(orderId: Int): Order? = withContext(Dispatchers.IO) {
        db.orderDao().getOrderById(orderId)
    }

    suspend fun placeOrder(order: Order): Long = withContext(Dispatchers.IO) {
        db.orderDao().insertOrder(order)
    }

    suspend fun updateOrder(order: Order) = withContext(Dispatchers.IO) {
        db.orderDao().updateOrder(order)
    }

    // Review Operations
    fun getReviews(productId: Int): Flow<List<Review>> {
        return db.reviewDao().getReviewsForProduct(productId)
    }

    suspend fun addReview(review: Review) = withContext(Dispatchers.IO) {
        db.reviewDao().insertReview(review)
    }

    // Wishlist Operations
    fun getWishlistItems(userId: Int): Flow<List<WishlistItem>> {
        return db.wishlistDao().getWishlistItemsByUserId(userId)
    }

    suspend fun isInWishlist(userId: Int, productId: Int): Boolean = withContext(Dispatchers.IO) {
        db.wishlistDao().getWishlistItem(userId, productId) != null
    }

    suspend fun toggleWishlist(userId: Int, productId: Int) = withContext(Dispatchers.IO) {
        val existing = db.wishlistDao().getWishlistItem(userId, productId)
        if (existing != null) {
            db.wishlistDao().deleteWishlistItem(existing)
        } else {
            db.wishlistDao().insertWishlistItem(WishlistItem(userId = userId, productId = productId))
        }
    }

    suspend fun removeFromWishlist(userId: Int, productId: Int) = withContext(Dispatchers.IO) {
        db.wishlistDao().deleteWishlistItem(userId, productId)
    }

    suspend fun clearWishlist(userId: Int) = withContext(Dispatchers.IO) {
        db.wishlistDao().clearWishlistByUserId(userId)
    }

    // Seeding logic
    suspend fun prepopulateDatabase() = withContext(Dispatchers.IO) {
        // Seed users
        val adminUser = db.userDao().getUserByEmail("admin@shahebfruits.com")
        if (adminUser == null) {
            db.userDao().insertUser(
                User(
                    username = "Admin Shaheb",
                    email = "admin@shahebfruits.com",
                    password = "admin",
                    role = "ADMIN",
                    phone = "+91 9876543210",
                    address = "12, Shaheb Corporate Tower, Mumbai, India"
                )
            )
        }

        val testUser = db.userDao().getUserByEmail("user@shahebfruits.com")
        if (testUser == null) {
            db.userDao().insertUser(
                User(
                    username = "Suraj Khatik",
                    email = "user@shahebfruits.com",
                    password = "user",
                    role = "CUSTOMER",
                    phone = "+91 9988776655",
                    address = "Flat 402, Green Avenue, Pune, India"
                )
            )
        }

        // Seed products
        val currentProducts = db.productDao().getAllProducts().first()
        if (currentProducts.isEmpty()) {
            val seedList = listOf(
                Product(
                    name = "Alphonso Mango",
                    category = "Mango",
                    description = "The absolute king of mangoes, sourced directly from Devgad. Rich, extremely sweet, with a buttery yellow texture and mesmerizing fragrance.",
                    price = 250.0,
                    unit = "1 kg",
                    stock = 50,
                    imageUrl = "mango_alphonso",
                    rating = 4.9f,
                    reviewCount = 5
                ),
                Product(
                    name = "Kesar Mango",
                    category = "Mango",
                    description = "Premium saffron-colored juicy mangoes from Talala. Famous for its intense sweet aroma, sugary juice, and thin skin.",
                    price = 180.0,
                    unit = "1 kg",
                    stock = 40,
                    imageUrl = "mango_kesar",
                    rating = 4.7f,
                    reviewCount = 3
                ),
                Product(
                    name = "Royal Gala Apple",
                    category = "Apple",
                    description = "Crisp, sweet, and imported fresh red Gala apples. Beautifully striped red-orange skin with juicy, dense cream-colored flesh.",
                    price = 150.0,
                    unit = "1 kg",
                    stock = 60,
                    imageUrl = "apple_gala",
                    rating = 4.6f,
                    reviewCount = 4
                ),
                Product(
                    name = "Green Granny Smith",
                    category = "Apple",
                    description = "Tart, refreshing, and ultra-crisp bright green apples. Extremely rich in antioxidants and perfect for pairing with cheese or healthy juicing.",
                    price = 190.0,
                    unit = "1 kg",
                    stock = 30,
                    imageUrl = "apple_green",
                    rating = 4.4f,
                    reviewCount = 2
                ),
                Product(
                    name = "Robusta Banana",
                    category = "Banana",
                    description = "Naturally ripened sweet Robusta bananas, high in fiber and potassium. Golden-yellow peel and soft, rich flesh.",
                    price = 60.0,
                    unit = "1 dozen",
                    stock = 100,
                    imageUrl = "banana_robusta",
                    rating = 4.5f,
                    reviewCount = 6
                ),
                Product(
                    name = "Red Banana",
                    category = "Banana",
                    description = "Exotic rich red bananas from deep southern farms. Earthy, creamy texture with hints of raspberry sweetness.",
                    price = 120.0,
                    unit = "1 dozen",
                    stock = 20,
                    imageUrl = "banana_red",
                    rating = 4.8f,
                    reviewCount = 2
                ),
                Product(
                    name = "Nagpur Orange",
                    category = "Orange",
                    description = "Famed sweet-sour juicy oranges from Nagpur groves. Exceptionally easily-peelable and loaded with sweet refreshing nectar.",
                    price = 90.0,
                    unit = "1 kg",
                    stock = 75,
                    imageUrl = "orange_nagpur",
                    rating = 4.5f,
                    reviewCount = 4
                ),
                Product(
                    name = "Valencia Blood Orange",
                    category = "Orange",
                    description = "Stunning blood-red orange segments with rich citrus notes and a sophisticated berry-like secondary flavor profile.",
                    price = 220.0,
                    unit = "1 kg",
                    stock = 15,
                    imageUrl = "orange_blood",
                    rating = 4.8f,
                    reviewCount = 1
                ),
                Product(
                    name = "Black Seedless Grapes",
                    category = "Grapes",
                    description = "Crunchy, sweet, and plump seedless black grapes from Nashik vineyards. Ideal for immediate snacking and high-energy days.",
                    price = 110.0,
                    unit = "1 kg",
                    stock = 45,
                    imageUrl = "grapes_black",
                    rating = 4.6f,
                    reviewCount = 3
                ),
                Product(
                    name = "Flame Red Grapes",
                    category = "Grapes",
                    description = "Firm, crispy red seedless grapes with a rich sweet finish. Packed fresh at optimal ripeness.",
                    price = 130.0,
                    unit = "1 kg",
                    stock = 50,
                    imageUrl = "grapes_red",
                    rating = 4.6f,
                    reviewCount = 2
                ),
                Product(
                    name = "Premium Kiwi Fruit",
                    category = "Seasonal Fruits",
                    description = "Fuzzy brown kiwis with bright green flesh and delightful speckling of black seeds. Zesty, sweet-tart taste profile.",
                    price = 150.0,
                    unit = "3 units",
                    stock = 35,
                    imageUrl = "seasonal_kiwi",
                    rating = 4.7f,
                    reviewCount = 2
                ),
                Product(
                    name = "Fresh Dragon Fruit",
                    category = "Seasonal Fruits",
                    description = "Eye-catching hot pink scale-skin dragon fruit with sweet-mild pear-tasting white flesh speckled with crunch seeds.",
                    price = 80.0,
                    unit = "1 unit",
                    stock = 25,
                    imageUrl = "seasonal_dragon",
                    rating = 4.5f,
                    reviewCount = 1
                )
            )

            for (prod in seedList) {
                val insertedId = db.productDao().insertProduct(prod)
                // Seed some default reviews for each product
                db.reviewDao().insertReview(
                    Review(
                        productId = insertedId.toInt(),
                        username = "Aditya Sharma",
                        rating = prod.rating,
                        comment = "Extremely fresh, juicy and high quality. The delivery was fast and the packaging was super neat!",
                        timestamp = System.currentTimeMillis() - 86400000
                    )
                )
                db.reviewDao().insertReview(
                    Review(
                        productId = insertedId.toInt(),
                        username = "Nikita Patel",
                        rating = prod.rating - 0.2f,
                        comment = "Really delicious fruits. Tastes organic and authentic, unlike regular market ones. Recommended!",
                        timestamp = System.currentTimeMillis() - 172800000
                    )
                )
            }
        }
    }

    // Notification Operations
    fun getNotifications(userId: Int): Flow<List<AppNotification>> {
        return db.notificationDao().getNotificationsByUserId(userId)
    }

    suspend fun sendNotification(notification: AppNotification) = withContext(Dispatchers.IO) {
        db.notificationDao().insertNotification(notification)
    }

    suspend fun markNotificationsRead(userId: Int) = withContext(Dispatchers.IO) {
        db.notificationDao().markAllAsRead(userId)
    }

    suspend fun deleteNotification(id: Int) = withContext(Dispatchers.IO) {
        db.notificationDao().deleteNotificationById(id)
    }
}

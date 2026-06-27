package com.example.data

object DiscountCalculator {
    /**
     * Calculates the discount percentage based on quantity.
     * 5-9 units: 10% (0.10)
     * 10+ units: 20% (0.20)
     */
    fun getDiscountPercentage(quantity: Int): Double {
        return when {
            quantity >= 10 -> 0.20
            quantity >= 5 -> 0.10
            else -> 0.0
        }
    }

    /**
     * Returns the discounted unit price of a single product.
     */
    fun getDiscountedUnitPrice(price: Double, quantity: Int): Double {
        val discount = getDiscountPercentage(quantity)
        return price * (1.0 - discount)
    }

    /**
     * Returns the total discounted price for a given quantity of product.
     */
    fun getDiscountedTotalPrice(price: Double, quantity: Int): Double {
        return getDiscountedUnitPrice(price, quantity) * quantity
    }

    /**
     * Returns the savings amount.
     */
    fun getSavingsAmount(price: Double, quantity: Int): Double {
        val originalTotal = price * quantity
        val discountedTotal = getDiscountedTotalPrice(price, quantity)
        return originalTotal - discountedTotal
    }
}

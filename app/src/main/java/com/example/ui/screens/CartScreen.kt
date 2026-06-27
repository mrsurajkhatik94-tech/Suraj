package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CartItem
import com.example.data.Product
import com.example.data.Recipe
import com.example.ui.theme.*
import com.example.viewmodel.FruitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: FruitViewModel,
    onNavigateToCheckout: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cartWithProducts by viewModel.cartWithProducts.collectAsState()

    val subtotal by viewModel.cartSubtotal.collectAsState()
    val deliveryFee by viewModel.deliveryFee.collectAsState()
    val taxAndPacking by viewModel.taxAndPacking.collectAsState()
    val grandTotal by viewModel.grandTotal.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Cart", fontWeight = FontWeight.Bold, color = BrandDarkGreen) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go Back", tint = BrandDarkGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandLightBackground)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BrandLightBackground)
        ) {
            if (cartWithProducts.isEmpty()) {
                EmptyCartPlaceholder(onShopNowClick = onNavigateHome)
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Selected Fresh Fruits (${cartWithProducts.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandDarkGreen,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        // Cart List items
                        items(cartWithProducts) { (cartItem, product) ->
                            CartRowItem(
                                cartItem = cartItem,
                                product = product,
                                onQuantityChange = { delta ->
                                    viewModel.updateCartQty(cartItem, delta)
                                },
                                onRemoveItem = {
                                    viewModel.removeCartItem(cartItem)
                                    Toast.makeText(context, "${product.name} removed from cart", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // Order Summary Section
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            OrderSummaryCard(
                                subtotal = subtotal,
                                deliveryFee = deliveryFee,
                                taxAndPacking = taxAndPacking,
                                grandTotal = grandTotal
                            )
                        }

                        // AI Recipe Suggestions Section
                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                            CartRecipeSuggestionsSection(viewModel = viewModel)
                        }
                    }

                    // Total Checkout Action Bottom Bar
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(16.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Grand Total", fontSize = 11.sp, color = MutedText)
                                Text(
                                    text = "₹${String.format("%.2f", grandTotal)}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BrandOrange
                                )
                            }

                            Button(
                                onClick = onNavigateToCheckout,
                                modifier = Modifier
                                    .height(50.dp)
                                    .weight(1f)
                                    .padding(start = 24.dp)
                                    .testTag("checkout_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Secure Checkout", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartRowItem(
    cartItem: CartItem,
    product: Product,
    onQuantityChange: (Int) -> Unit,
    onRemoveItem: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .testTag("cart_item_row_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mini Geometric Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                getFruitColor(product.category).copy(alpha = 0.1f),
                                getFruitColor(product.category).copy(alpha = 0.3f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(36.dp)) {
                    drawCircle(color = getFruitColor(product.category), radius = size.width / 2f)
                }
                Text(text = getFruitEmoji(product.category), fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandDarkGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "₹${product.price} / ${product.unit}",
                    fontSize = 12.sp,
                    color = MutedText
                )
                Spacer(modifier = Modifier.height(4.dp))

                val originalItemPrice = product.price * cartItem.quantity
                val discountPercent = com.example.data.DiscountCalculator.getDiscountPercentage(cartItem.quantity)
                val isDiscounted = discountPercent > 0.0

                if (isDiscounted) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "₹${originalItemPrice.toInt()}",
                            fontSize = 12.sp,
                            style = androidx.compose.ui.text.TextStyle(
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            ),
                            color = MutedText,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BrandGreen.copy(alpha = 0.12f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${(discountPercent * 100).toInt()}% OFF Bulk",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandGreen
                            )
                        }
                    }
                    val discountedItemPrice = com.example.data.DiscountCalculator.getDiscountedTotalPrice(product.price, cartItem.quantity)
                    Text(
                        text = "₹${String.format("%.2f", discountedItemPrice)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandOrange
                    )
                } else {
                    Text(
                        text = "₹${product.price * cartItem.quantity}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandOrange
                    )
                }
            }

            // Controls
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight()
            ) {
                IconButton(
                    onClick = onRemoveItem,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove product",
                        tint = ErrorColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onQuantityChange(-1) },
                        modifier = Modifier
                            .background(LightGreenAccent, CircleShape)
                            .size(28.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = BrandGreen, modifier = Modifier.size(12.dp))
                    }

                    Text(
                        text = cartItem.quantity.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandDarkGreen,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )

                    IconButton(
                        onClick = { if (cartItem.quantity < product.stock) onQuantityChange(1) },
                        modifier = Modifier
                            .background(LightGreenAccent, CircleShape)
                            .size(28.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = BrandGreen, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun OrderSummaryCard(
    subtotal: Double,
    deliveryFee: Double,
    taxAndPacking: Double,
    grandTotal: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Order Summary",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = BrandDarkGreen
            )

            Spacer(modifier = Modifier.height(16.dp))

            SummaryRow(label = "Fruit Subtotal", value = "₹${String.format("%.2f", subtotal)}")
            Spacer(modifier = Modifier.height(10.dp))

            SummaryRow(
                label = "Delivery Charges",
                value = if (deliveryFee == 0.0) "FREE" else "₹${String.format("%.2f", deliveryFee)}",
                valueColor = if (deliveryFee == 0.0) BrandGreen else BrandDarkGreen,
                isBold = deliveryFee == 0.0
            )
            Spacer(modifier = Modifier.height(10.dp))

            SummaryRow(label = "Packaging & Taxes (5%)", value = "₹${String.format("%.2f", taxAndPacking)}")

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(14.dp))

            SummaryRow(
                label = "Estimated Payable Amount",
                value = "₹${String.format("%.2f", grandTotal)}",
                labelColor = BrandDarkGreen,
                valueColor = BrandOrange,
                isBold = true,
                textSize = 16.sp
            )

            // Prompts for Free Delivery
            if (subtotal < 500.0) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = LightOrangeAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💡 Add ₹${String.format("%.2f", 500.0 - subtotal)} more to unlock FREE home delivery!",
                        fontSize = 11.sp,
                        color = BrandOrange,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = LightGreenAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🎉 Congratulations! You have unlocked FREE home delivery!",
                        fontSize = 11.sp,
                        color = BrandGreen,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryRow(
    label: String,
    value: String,
    labelColor: Color = MutedText,
    valueColor: Color = BrandDarkGreen,
    isBold: Boolean = false,
    textSize: androidx.compose.ui.unit.TextUnit = 13.sp
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = textSize,
            color = labelColor,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = textSize,
            color = valueColor,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Bold
        )
    }
}

@Composable
fun EmptyCartPlaceholder(onShopNowClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Friendly, beautifully designed empty-cart illustration
        EmptyCartIllustration()

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Your Cart feels so light! 🍃",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = BrandDarkGreen,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Fill it up with delicious, naturally sweet, organic farm-fresh fruits to start your healthy habit today!",
            fontSize = 14.sp,
            color = MutedText,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onShopNowClick,
            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .height(52.dp)
                .width(220.dp)
                .testTag("shop_now_btn")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Continue Shopping",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun EmptyCartIllustration(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(180.dp)
    ) {
        // Glowing background decorative blobs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val center = androidx.compose.ui.geometry.Offset(cx, cy)
            
            // Draw background soft circles
            drawCircle(
                color = LightGreenAccent.copy(alpha = 0.4f),
                radius = size.width * 0.4f,
                center = center
            )
            
            drawCircle(
                color = LightOrangeAccent.copy(alpha = 0.3f),
                radius = size.width * 0.25f,
                center = androidx.compose.ui.geometry.Offset(cx - 30.dp.toPx(), cy + 20.dp.toPx())
            )
            
            drawCircle(
                color = Color(0xFFFFCDD2).copy(alpha = 0.3f), // Soft apple red
                radius = size.width * 0.2f,
                center = androidx.compose.ui.geometry.Offset(cx + 40.dp.toPx(), cy - 30.dp.toPx())
            )
        }

        // Smiling Empty Shopping Cart with a small leaf
        Canvas(modifier = Modifier.size(120.dp)) {
            val width = size.width
            val height = size.height

            val cartColor = BrandGreen
            val faceColor = BrandDarkGreen
            
            // Wheels
            drawCircle(
                color = faceColor,
                radius = 7.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(width * 0.35f, height * 0.75f)
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(width * 0.35f, height * 0.75f)
            )

            drawCircle(
                color = faceColor,
                radius = 7.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(width * 0.65f, height * 0.75f)
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(width * 0.65f, height * 0.75f)
            )

            // Connect wheels to cart body (chassis)
            drawLine(
                color = faceColor,
                start = androidx.compose.ui.geometry.Offset(width * 0.3f, height * 0.68f),
                end = androidx.compose.ui.geometry.Offset(width * 0.7f, height * 0.68f),
                strokeWidth = 3.dp.toPx()
            )

            // Cart basket outline (rounded rect or path)
            val basketPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(width * 0.25f, height * 0.35f) // top left
                lineTo(width * 0.75f, height * 0.35f) // top right
                quadraticTo(width * 0.72f, height * 0.63f, width * 0.68f, height * 0.65f) // curve to bottom right
                lineTo(width * 0.32f, height * 0.65f) // bottom left
                quadraticTo(width * 0.28f, height * 0.63f, width * 0.25f, height * 0.35f) // curve to top left
                close()
            }
            
            drawPath(
                path = basketPath,
                color = cartColor.copy(alpha = 0.12f),
                style = androidx.compose.ui.graphics.drawscope.Fill
            )
            drawPath(
                path = basketPath,
                color = cartColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 4.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )

            // Cart handle
            val handlePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(width * 0.25f, height * 0.38f)
                lineTo(width * 0.15f, height * 0.35f)
                lineTo(width * 0.12f, height * 0.42f)
            }
            drawPath(
                path = handlePath,
                color = faceColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 4.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )

            // Grid lines on basket (just a couple of soft, stylish horizontal lines)
            drawLine(
                color = cartColor.copy(alpha = 0.3f),
                start = androidx.compose.ui.geometry.Offset(width * 0.27f, height * 0.45f),
                end = androidx.compose.ui.geometry.Offset(width * 0.73f, height * 0.45f),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = cartColor.copy(alpha = 0.3f),
                start = androidx.compose.ui.geometry.Offset(width * 0.30f, height * 0.55f),
                end = androidx.compose.ui.geometry.Offset(width * 0.70f, height * 0.55f),
                strokeWidth = 2.dp.toPx()
            )

            // Friendly cartoon eyes
            drawCircle(
                color = faceColor,
                radius = 3.5.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(width * 0.43f, height * 0.46f)
            )
            drawCircle(
                color = faceColor,
                radius = 3.5.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(width * 0.57f, height * 0.46f)
            )

            // Smiling mouth
            val mouthPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(width * 0.47f, height * 0.51f)
                quadraticTo(width * 0.50f, height * 0.56f, width * 0.53f, height * 0.51f)
            }
            drawPath(
                path = mouthPath,
                color = faceColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 2.5.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )

            // Tiny blush cheeks
            drawCircle(
                color = Color(0xFFFF8A80), // Soft pink
                radius = 2.5.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(width * 0.39f, height * 0.50f)
            )
            drawCircle(
                color = Color(0xFFFF8A80), // Soft pink
                radius = 2.5.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(width * 0.61f, height * 0.50f)
            )

            // A tiny fresh leaf sprouting from the cart
            val leafPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(width * 0.72f, height * 0.32f)
                quadraticTo(width * 0.78f, height * 0.20f, width * 0.85f, height * 0.22f)
                quadraticTo(width * 0.82f, height * 0.34f, width * 0.72f, height * 0.32f)
            }
            drawPath(
                path = leafPath,
                color = BrandGreen,
                style = androidx.compose.ui.graphics.drawscope.Fill
            )
            // Leaf stem
            drawLine(
                color = faceColor,
                start = androidx.compose.ui.geometry.Offset(width * 0.72f, height * 0.35f),
                end = androidx.compose.ui.geometry.Offset(width * 0.74f, height * 0.28f),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Floating sparkles/stars around the empty cart
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = BrandOrange.copy(alpha = 0.8f),
            modifier = Modifier
                .size(18.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-10).dp, y = 15.dp)
        )

        Icon(
            imageVector = Icons.Default.Spa,
            contentDescription = null,
            tint = BrandGreen.copy(alpha = 0.7f),
            modifier = Modifier
                .size(16.dp)
                .align(Alignment.BottomStart)
                .offset(x = 10.dp, y = (-10).dp)
        )

        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = Color(0xFFE57373).copy(alpha = 0.8f),
            modifier = Modifier
                .size(14.dp)
                .align(Alignment.TopStart)
                .offset(x = 15.dp, y = 10.dp)
        )
    }
}

@Composable
fun CartRecipeSuggestionsSection(
    viewModel: FruitViewModel,
    modifier: Modifier = Modifier
) {
    val recipeSuggestions by viewModel.recipeSuggestions.collectAsState()
    val recipeLoading by viewModel.recipeLoading.collectAsState()
    val recipeError by viewModel.recipeError.collectAsState()
    val cartWithProducts by viewModel.cartWithProducts.collectAsState()

    // Trigger auto-fetch on entry or when cart content changes
    LaunchedEffect(cartWithProducts.map { it.second.id }) {
        if (cartWithProducts.isNotEmpty()) {
            viewModel.fetchRecipeSuggestions(force = false)
        }
    }

    if (cartWithProducts.isEmpty()) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .testTag("ai_recipes_section_card"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = BrandOrange,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "Chef's AI Suggestions",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandDarkGreen
                        )
                        Text(
                            text = "Custom recipes using your cart fruits",
                            fontSize = 11.sp,
                            color = MutedText
                        )
                    }
                }

                if (!recipeLoading) {
                    IconButton(
                        onClick = { viewModel.fetchRecipeSuggestions(force = true) },
                        modifier = Modifier.size(32.dp).testTag("refresh_recipes_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate recipes",
                            tint = BrandGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                recipeLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = BrandGreen,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Cooking up fresh healthy recipes...",
                            fontSize = 13.sp,
                            color = MutedText,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Analyzing your specific fruits combination",
                            fontSize = 10.sp,
                            color = MutedText.copy(alpha = 0.7f)
                        )
                    }
                }
                recipeError != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Oops! ${recipeError}",
                            fontSize = 13.sp,
                            color = ErrorColor,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.fetchRecipeSuggestions(force = true) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Retry Suggestions", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
                recipeSuggestions.isEmpty() -> {
                    Text(
                        text = "No suggestions ready. Click refresh or add fruits to your cart!",
                        fontSize = 13.sp,
                        color = MutedText,
                        modifier = Modifier.padding(vertical = 12.dp),
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        recipeSuggestions.forEachIndexed { index, recipe ->
                            RecipeItemView(recipe = recipe, index = index)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeItemView(recipe: Recipe, index: Int) {
    var isExpanded by remember { mutableStateOf(index == 0) } // First one is expanded by default

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .border(
                width = 1.dp,
                color = if (isExpanded) BrandGreen.copy(alpha = 0.25f) else Color.LightGray.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) BrandLightBackground.copy(alpha = 0.3f) else Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Recipe Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "🍳",
                        fontSize = 16.sp
                    )
                    Text(
                        text = recipe.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandDarkGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(LightOrangeAccent)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = recipe.prepTime,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandOrange
                        )
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = BrandDarkGreen.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Why it fits hint
            Text(
                text = "✨ " + recipe.whyItFits,
                fontSize = 11.sp,
                color = BrandGreen,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )

            // Expanded Recipe Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Ingredients Sub-section
                    Text(
                        text = "Ingredients Needed:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandDarkGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    recipe.ingredients.forEach { ingredient ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp)
                        ) {
                            Text(
                                text = "•",
                                fontSize = 12.sp,
                                color = BrandOrange,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = ingredient,
                                fontSize = 11.sp,
                                color = MutedText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Instructions Sub-section
                    Text(
                        text = "Instructions / Steps:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandDarkGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    recipe.instructions.forEachIndexed { stepIndex, instruction ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .size(16.dp)
                                    .background(LightGreenAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (stepIndex + 1).toString(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandGreen
                                )
                            }
                            Text(
                                text = instruction,
                                fontSize = 11.sp,
                                color = MutedText,
                                lineHeight = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

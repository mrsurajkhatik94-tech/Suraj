package com.example.ui.screens

import kotlinx.coroutines.flow.flowOf

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Product
import com.example.data.Review
import com.example.ui.theme.*
import com.example.viewmodel.FruitViewModel
import com.example.ui.components.ProductDetailsSkeleton
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    productId: Int,
    viewModel: FruitViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var product by remember { mutableStateOf<Product?>(null) }
    val reviews by (product?.let { viewModel.getProductReviews(it.id) } ?: flowOf(emptyList())).collectAsState(initial = emptyList())

    // Quantities state
    var selectedQty by remember { mutableStateOf(1) }

    // New review form state
    var newRating by remember { mutableStateOf(5f) }
    var newComment by remember { mutableStateOf("") }
    var isSubmittingReview by remember { mutableStateOf(false) }

    // Load product
    LaunchedEffect(productId) {
        product = viewModel.getProductById(productId)
    }

    Scaffold(
        topBar = {
            val wishlistItems by viewModel.wishlistItems.collectAsState()
            val isWishlisted = product?.let { p -> wishlistItems.any { it.productId == p.id } } ?: false

            TopAppBar(
                title = { Text("Fruit Details", fontWeight = FontWeight.Bold, color = BrandDarkGreen) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go Back", tint = BrandDarkGreen)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { product?.let { viewModel.toggleWishlist(it) } },
                        modifier = Modifier.testTag("details_wishlist_toggle")
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Toggle Wishlist",
                            tint = if (isWishlisted) ErrorColor else BrandDarkGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandLightBackground)
            )
        }
    ) { paddingValues ->
        if (product == null) {
            ProductDetailsSkeleton(
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            val prod = product!!
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(BrandLightBackground)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    // Fruit Art Banner Image
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            getFruitColor(prod.category).copy(alpha = 0.2f),
                                            getFruitColor(prod.category).copy(alpha = 0.5f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(120.dp)) {
                                drawCircle(
                                    color = getFruitColor(prod.category),
                                    radius = size.width / 2f
                                )
                            }
                            Text(
                                text = getFruitEmoji(prod.category),
                                fontSize = 72.sp
                            )
                        }
                    }

                    // Primary Info Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .shadow(2.dp, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column {
                                        Text(
                                            text = prod.name,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandDarkGreen
                                        )
                                        Text(
                                            text = prod.category,
                                            fontSize = 14.sp,
                                            color = BrandOrange,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "₹${prod.price}",
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Black,
                                            color = BrandGreen
                                        )
                                        Text(
                                            text = "per ${prod.unit}",
                                            fontSize = 12.sp,
                                            color = MutedText
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = BrandOrange, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = String.format("%.1f", prod.rating),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandDarkGreen
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "|   ${reviews.size} Customer Reviews",
                                        fontSize = 13.sp,
                                        color = MutedText
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Divider(color = Color.LightGray.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Description",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandDarkGreen
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = prod.description,
                                    fontSize = 14.sp,
                                    color = MutedText,
                                    lineHeight = 20.sp
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                SeasonalAvailabilityView(product = prod)

                                Spacer(modifier = Modifier.height(20.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Stock Status",
                                        fontWeight = FontWeight.Bold,
                                        color = BrandDarkGreen,
                                        fontSize = 14.sp
                                    )

                                    if (prod.stock > 0) {
                                        Box(
                                            modifier = Modifier
                                                .background(LightGreenAccent, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("In Stock (${prod.stock} units)", color = BrandGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Out of Stock", color = ErrorColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Quantity Selection Section
                    if (prod.stock > 0) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Select Quantity",
                                        fontWeight = FontWeight.Bold,
                                        color = BrandDarkGreen,
                                        fontSize = 14.sp
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { if (selectedQty > 1) selectedQty-- },
                                            modifier = Modifier.background(LightGreenAccent, CircleShape).size(36.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = BrandGreen, modifier = Modifier.size(16.dp))
                                        }

                                        Text(
                                            text = selectedQty.toString(),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandDarkGreen,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )

                                        IconButton(
                                            onClick = { if (selectedQty < prod.stock) selectedQty++ },
                                            modifier = Modifier.background(LightGreenAccent, CircleShape).size(36.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = BrandGreen, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bulk Discount Notification Section
                    if (prod.stock > 0) {
                        item {
                            BulkDiscountNotificationView(product = prod, quantity = selectedQty)
                        }
                    }

                    // Write Review Section
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .shadow(2.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Rate & Review This Fruit",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandDarkGreen
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Dynamic Star Rating selector
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Your Rating: ", fontSize = 13.sp, color = MutedText)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    (1..5).forEach { star ->
                                        val isFilled = star <= newRating
                                        Icon(
                                            imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = null,
                                            tint = BrandOrange,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clickable { newRating = star.toFloat() }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = newComment,
                                    onValueChange = { newComment = it },
                                    placeholder = { Text("Share your experience with this fruit. Was it fresh and sweet?") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 4,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        if (newComment.isNotBlank()) {
                                            viewModel.addProductReview(prod.id, newRating, newComment)
                                            Toast.makeText(context, "Review submitted! Thank you.", Toast.LENGTH_SHORT).show()
                                            newComment = ""
                                            newRating = 5f
                                        } else {
                                            Toast.makeText(context, "Please enter a comment.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Submit Review", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    // Reviews List Header
                    item {
                        Text(
                            text = "Customer Feedbacks",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandDarkGreen,
                            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                        )
                    }

                    // Reviews List Content
                    if (reviews.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Box(
                                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No reviews yet. Be the first to rate!", color = MutedText, fontSize = 13.sp)
                                }
                            }
                        }
                    } else {
                        items(reviews) { r ->
                            ReviewListItem(r)
                        }
                    }
                }

                // Add to Cart Action Bar at bottom
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
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
                            val originalPrice = prod.price * selectedQty
                            val discountPercent = com.example.data.DiscountCalculator.getDiscountPercentage(selectedQty)
                            val isDiscounted = discountPercent > 0.0

                            Text("Total Price", fontSize = 11.sp, color = MutedText)
                            if (isDiscounted) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "₹${String.format("%.0f", originalPrice)}",
                                        fontSize = 13.sp,
                                        style = androidx.compose.ui.text.TextStyle(
                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                        ),
                                        color = MutedText,
                                        modifier = Modifier.padding(end = 6.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (discountPercent >= 0.20) BrandOrange.copy(alpha = 0.15f)
                                                else BrandGreen.copy(alpha = 0.15f)
                                            )
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${(discountPercent * 100).toInt()}% OFF",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (discountPercent >= 0.20) BrandOrange else BrandGreen
                                        )
                                    }
                                }
                                val discountedPrice = com.example.data.DiscountCalculator.getDiscountedTotalPrice(prod.price, selectedQty)
                                Text(
                                    text = "₹${String.format("%.2f", discountedPrice)}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BrandOrange
                                )
                            } else {
                                Text(
                                    text = "₹${prod.price * selectedQty}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BrandOrange
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (prod.stock > 0) {
                                    viewModel.addToCart(prod, selectedQty)
                                    Toast.makeText(context, "$selectedQty x ${prod.name} added to cart!", Toast.LENGTH_SHORT).show()
                                    onNavigateBack()
                                }
                            },
                            enabled = prod.stock > 0,
                            modifier = Modifier
                                .height(50.dp)
                                .weight(1f)
                                .padding(start = 24.dp)
                                .testTag("add_to_cart_detail_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add To Cart", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewListItem(review: Review) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(1.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(LightGreenAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = review.username,
                        fontWeight = FontWeight.Bold,
                        color = BrandDarkGreen,
                        fontSize = 13.sp
                    )
                }

                val date = remember(review.timestamp) {
                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    sdf.format(Date(review.timestamp))
                }
                Text(
                    text = date,
                    fontSize = 10.sp,
                    color = MutedText
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                (1..5).forEach { star ->
                    val isFilled = star <= review.rating
                    Icon(
                        imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = BrandOrange,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = review.comment,
                fontSize = 13.sp,
                color = MutedText,
                lineHeight = 18.sp
            )
        }
    }
}

data class SeasonInfo(
    val peakMonths: String,
    val activeMonths: List<Int>, // 1-indexed (1=Jan, 12=Dec)
    val description: String
)

fun getSeasonInfo(category: String): SeasonInfo {
    return when (category) {
        "Mango" -> SeasonInfo(
            peakMonths = "April - July",
            activeMonths = listOf(4, 5, 6, 7),
            description = "Sourced fresh from coastal Ratnagiri orchards. Peak sweetness and rich aroma."
        )
        "Apple" -> SeasonInfo(
            peakMonths = "August - November",
            activeMonths = listOf(8, 9, 10, 11),
            description = "Crisp, hand-picked from Shimla high-altitude cold-mountain orchards."
        )
        "Banana" -> SeasonInfo(
            peakMonths = "Year-Round",
            activeMonths = (1..12).toList(),
            description = "Harvested daily across fertile local farms under steady sunshine."
        )
        "Orange" -> SeasonInfo(
            peakMonths = "November - February",
            activeMonths = listOf(11, 12, 1, 2),
            description = "Tangy, juicy Nagpur citrus harvested at the chill of winter peak."
        )
        "Grapes" -> SeasonInfo(
            peakMonths = "January - April",
            activeMonths = listOf(1, 2, 3, 4),
            description = "Sweet and seedless Nashik table grapes, crisp-picked at peak sugar levels."
        )
        "Seasonal Fruits" -> SeasonInfo(
            peakMonths = "June - August",
            activeMonths = listOf(6, 7, 8),
            description = "Limited-run high-quality harvest, packed fresh from organic partner growers."
        )
        else -> SeasonInfo(
            peakMonths = "Year-Round",
            activeMonths = (1..12).toList(),
            description = "Locally grown with sustainable agricultural methods for steady nutrient density."
        )
    }
}

@Composable
fun SeasonalAvailabilityView(
    product: Product,
    modifier: Modifier = Modifier
) {
    val seasonInfo = remember(product.category) { getSeasonInfo(product.category) }
    // Since current time is 2026-06-27, we use Month 6 (June)
    val currentMonth = 6
    val isInSeason = seasonInfo.activeMonths.contains(currentMonth)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BrandLightBackground)
            .border(1.dp, BrandGreen.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("seasonal_availability_card")
    ) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = null,
                tint = BrandOrange,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Seasonal Availability",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = BrandDarkGreen
            )
            Spacer(modifier = Modifier.weight(1f))

            // Peak/Off-peak Badge
            val badgeBgColor = if (isInSeason) BrandGreen.copy(alpha = 0.12f) else BrandOrange.copy(alpha = 0.12f)
            val badgeTextColor = if (isInSeason) BrandGreen else BrandOrange
            val badgeText = if (isInSeason) "Peak Season" else "Off-Peak Season"

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeBgColor)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeTextColor
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Peak Season Text Info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Peak Months: ",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MutedText
            )
            Text(
                text = seasonInfo.peakMonths,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BrandDarkGreen
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = seasonInfo.description,
            fontSize = 12.sp,
            color = MutedText,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Months Timeline (Jan - Dec)
        val monthLabels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

        Text(
            text = "Seasonal Calendar (Highlighting Peak Months)",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = BrandDarkGreen.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            monthLabels.forEachIndexed { index, name ->
                val monthNumber = index + 1
                val isPeak = seasonInfo.activeMonths.contains(monthNumber)
                val isCurrent = monthNumber == currentMonth

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCurrent && isPeak -> BrandGreen
                                    isCurrent -> BrandOrange
                                    isPeak -> BrandGreen.copy(alpha = 0.18f)
                                    else -> Color.White
                                }
                            )
                            .border(
                                width = if (isCurrent) 2.dp else 1.dp,
                                color = when {
                                    isCurrent && isPeak -> BrandOrange
                                    isCurrent -> BrandOrange
                                    isPeak -> BrandGreen.copy(alpha = 0.4f)
                                    else -> Color.LightGray.copy(alpha = 0.4f)
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name.first().toString(),
                            fontSize = 10.sp,
                            fontWeight = if (isCurrent || isPeak) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                isCurrent -> Color.White
                                isPeak -> BrandDarkGreen
                                else -> MutedText.copy(alpha = 0.6f)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = name,
                        fontSize = 8.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCurrent) BrandOrange else MutedText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Peak Legend
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(BrandGreen.copy(alpha = 0.5f))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "In Season", fontSize = 9.sp, color = MutedText)

            Spacer(modifier = Modifier.width(16.dp))

            // Current Month Legend
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(BrandOrange)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Current Month (June)", fontSize = 9.sp, color = MutedText)
        }
    }
}

@Composable
fun BulkDiscountNotificationView(
    product: Product,
    quantity: Int,
    modifier: Modifier = Modifier
) {
    val percent = com.example.data.DiscountCalculator.getDiscountPercentage(quantity)
    val isDiscounted = percent > 0.0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .testTag("bulk_discount_notification"),
        colors = CardDefaults.cardColors(
            containerColor = when {
                percent >= 0.20 -> Color(0xFFFFF9E6) // Light Amber Gold
                percent >= 0.10 -> LightGreenAccent.copy(alpha = 0.5f) // Soft active green
                else -> BrandLightBackground // Muted info bg
            }
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.2.dp,
            color = when {
                percent >= 0.20 -> BrandOrange.copy(alpha = 0.6f)
                percent >= 0.10 -> BrandGreen.copy(alpha = 0.5f)
                else -> Color.LightGray.copy(alpha = 0.4f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = when {
                        percent >= 0.20 -> Icons.Default.AutoAwesome
                        percent >= 0.10 -> Icons.Default.Check
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = when {
                        percent >= 0.20 -> BrandOrange
                        percent >= 0.10 -> BrandGreen
                        else -> BrandDarkGreen.copy(alpha = 0.7f)
                    },
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        percent >= 0.20 -> "👑 Super Bulk Savings Applied!"
                        percent >= 0.10 -> "🎉 10% Bulk Discount Applied!"
                        else -> "Unlock Bulk Discounts"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandDarkGreen
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body message & details
            Text(
                text = when {
                    percent >= 0.20 -> "Awesome choice! You have unlocked our maximum discount of 20% OFF on this purchase."
                    percent >= 0.10 -> "You are saving 10%! Add ${10 - quantity} more units of ${product.name} to unlock 20% OFF!"
                    else -> "Buy 5-9 units to get 10% OFF, or 10+ units to save a massive 20% OFF your entire fruit order!"
                },
                fontSize = 12.sp,
                color = MutedText,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar mapping towards next discount tier
            val progress = when {
                quantity >= 10 -> 1f
                quantity >= 5 -> 0.5f + (quantity - 5) * 0.1f
                else -> quantity / 10f
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = progress,
                    color = if (isDiscounted) BrandGreen else BrandOrange,
                    trackColor = Color.LightGray.copy(alpha = 0.25f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Price display with comparison
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isDiscounted) {
                            val originalUnit = product.price
                            val discountedUnit = com.example.data.DiscountCalculator.getDiscountedUnitPrice(product.price, quantity)
                            "Unit price: ₹${String.format("%.2f", discountedUnit)} (Saved ₹${String.format("%.2f", originalUnit - discountedUnit)}/unit)"
                        } else {
                            "Add 5 or more to start saving!"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDiscounted) BrandGreen else MutedText
                    )

                    Text(
                        text = when {
                            quantity >= 10 -> "Level: Max Savings"
                            quantity >= 5 -> "Level: 10% Off"
                            else -> "${quantity}/5 to 10%"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDiscounted) BrandGreen else BrandOrange
                    )
                }
            }
        }
    }
}


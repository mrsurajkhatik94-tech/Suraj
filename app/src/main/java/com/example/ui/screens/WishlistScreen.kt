package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import com.example.data.Product
import com.example.ui.theme.*
import com.example.viewmodel.FruitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    viewModel: FruitViewModel,
    onNavigateToProduct: (Int) -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val wishlistProducts by viewModel.wishlistProducts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Wishlist", fontWeight = FontWeight.Bold, color = BrandDarkGreen) },
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
            if (wishlistProducts.isEmpty()) {
                EmptyWishlistPlaceholder(onExploreClick = onNavigateHome)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Saved Fruits (${wishlistProducts.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandDarkGreen,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(wishlistProducts, key = { it.id }) { product ->
                            WishlistRowItem(
                                product = product,
                                onProductClick = { onNavigateToProduct(product.id) },
                                onRemoveFromWishlist = {
                                    viewModel.toggleWishlist(product)
                                    Toast.makeText(context, "${product.name} removed from wishlist", Toast.LENGTH_SHORT).show()
                                },
                                onAddToCart = {
                                    viewModel.addToCart(product)
                                    Toast.makeText(context, "${product.name} added to cart!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WishlistRowItem(
    product: Product,
    onProductClick: () -> Unit,
    onRemoveFromWishlist: () -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable { onProductClick() }
            .testTag("wishlist_item_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fruit Art Thumbnail Icon
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                getFruitColor(product.category).copy(alpha = 0.15f),
                                getFruitColor(product.category).copy(alpha = 0.35f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(40.dp)) {
                    drawCircle(
                        color = getFruitColor(product.category),
                        radius = size.width / 2f
                    )
                }
                Text(
                    text = getFruitEmoji(product.category),
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

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
                    text = product.category,
                    fontSize = 11.sp,
                    color = MutedText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₹${product.price}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandOrange
                    )
                    Text(
                        text = " / ${product.unit}",
                        fontSize = 10.sp,
                        color = MutedText
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Buttons
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Remove from wishlist
                IconButton(
                    onClick = onRemoveFromWishlist,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("remove_wishlist_${product.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Remove from Wishlist",
                        tint = ErrorColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Add to Cart Action
                IconButton(
                    onClick = onAddToCart,
                    modifier = Modifier
                        .size(36.dp)
                        .background(BrandGreen, CircleShape)
                        .testTag("add_to_cart_from_wishlist_${product.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = "Add to Cart",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyWishlistPlaceholder(
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color.White, CircleShape)
                .shadow(1.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = ErrorColor.copy(alpha = 0.2f),
                modifier = Modifier.size(60.dp)
            )
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = ErrorColor.copy(alpha = 0.8f),
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Your Wishlist is Empty",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = BrandDarkGreen,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Save your favorite organic fruits for later so you can easily purchase them whenever you want.",
            fontSize = 13.sp,
            color = MutedText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onExploreClick,
            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .testTag("explore_harvest_button")
        ) {
            Icon(Icons.Default.Eco, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Explore Fresh Harvest",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
}

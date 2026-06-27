package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Product
import com.example.ui.theme.*
import com.example.viewmodel.FruitViewModel
import com.example.viewmodel.ProductSortOption
import kotlinx.coroutines.launch
import com.example.ui.components.ProductGridItemSkeleton
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: FruitViewModel,
    onNavigateToProduct: (Int) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToInfo: (String) -> Unit,
    onNavigateToWishlist: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val currentUser by viewModel.currentUser.collectAsState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val isLoadingProducts by viewModel.isLoadingProducts.collectAsState()
    val wishlistItems by viewModel.wishlistItems.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val comparisonProducts by viewModel.comparisonProducts.collectAsState()
    var showComparisonModal by remember { mutableStateOf(false) }

    val notifications by viewModel.notifications.collectAsState()
    val unreadNotifications = remember(notifications) { notifications.count { !it.isRead } }

    val totalCartCount = cartItems.sumOf { it.quantity }
    val wishlistedIds = remember(wishlistItems) { wishlistItems.map { it.productId }.toSet() }

    val categories = listOf("All", "Mango", "Apple", "Banana", "Orange", "Grapes", "Seasonal Fruits")

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = BrandLightBackground,
                modifier = Modifier.width(300.dp)
            ) {
                // Drawer Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(BrandGreen, BrandDarkGreen)
                            )
                        )
                        .padding(vertical = 32.dp, horizontal = 24.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .padding(2.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_app_icon),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Shaheb Fruits",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = currentUser?.username ?: "Guest Shopper",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation Items
                NavigationDrawerItem(
                    label = { Text("Shop Home", fontWeight = FontWeight.Bold) },
                    selected = true,
                    onClick = { coroutineScope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Home, contentDescription = null, tint = BrandGreen) },
                    colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = LightGreenAccent)
                )

                NavigationDrawerItem(
                    label = { Text("My Shopping Cart") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onNavigateToCart()
                    },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = BrandGreen) }
                )

                NavigationDrawerItem(
                    label = { Text("Track Orders") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onNavigateToOrders()
                    },
                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = null, tint = BrandGreen) }
                )

                NavigationDrawerItem(
                    label = { Text("My Wishlist") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onNavigateToWishlist()
                    },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = BrandGreen) }
                )

                NavigationDrawerItem(
                    label = {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Notifications Inbox")
                            if (unreadNotifications > 0) {
                                Box(
                                    modifier = Modifier
                                        .background(BrandOrange, CircleShape)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$unreadNotifications new",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onNavigateToNotifications()
                    },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null, tint = BrandGreen) }
                )

                if (currentUser?.role == "ADMIN") {
                    NavigationDrawerItem(
                        label = { Text("Admin Dashboard ✨", fontWeight = FontWeight.Bold, color = BrandOrange) },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            onNavigateToAdmin()
                        },
                        icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = BrandOrange) }
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp))

                // Info pages
                Text("COMPANY INFO", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 20.dp, bottom = 8.dp), color = MutedText)

                listOf("About Us", "Contact Us", "FAQ", "Privacy Policy", "Terms & Conditions").forEach { page ->
                    NavigationDrawerItem(
                        label = { Text(page) },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            onNavigateToInfo(page)
                        },
                        icon = {
                            val icon = when (page) {
                                "About Us" -> Icons.Default.Info
                                "Contact Us" -> Icons.Default.ContactSupport
                                "FAQ" -> Icons.Default.HelpCenter
                                else -> Icons.Default.Policy
                            }
                            Icon(icon, contentDescription = null, tint = BrandGreen.copy(alpha = 0.7f))
                        }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Logout
                NavigationDrawerItem(
                    label = { Text("Sign Out", color = ErrorColor) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        viewModel.logout()
                        onLogout()
                    },
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = ErrorColor) },
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                Surface(
                    color = BrandLightBackground,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(bottom = 12.dp)
                    ) {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Eco,
                                        contentDescription = null,
                                        tint = BrandOrange,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Shaheb Fruits",
                                        fontWeight = FontWeight.Bold,
                                        color = BrandDarkGreen,
                                        fontSize = 20.sp
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = { coroutineScope.launch { drawerState.open() } },
                                    modifier = Modifier.testTag("drawer_menu_button")
                                ) {
                                    Icon(Icons.Default.Menu, contentDescription = "Open Navigation Menu", tint = BrandDarkGreen)
                                }
                            },
                            actions = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    // Notification Bell Icon with unread badge
                                    Box {
                                        IconButton(
                                            onClick = onNavigateToNotifications,
                                            modifier = Modifier.testTag("notification_bell_button")
                                        ) {
                                            Icon(
                                                imageVector = if (unreadNotifications > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                                contentDescription = "Notifications Inbox",
                                                tint = if (unreadNotifications > 0) BrandOrange else BrandDarkGreen
                                            )
                                        }
                                        if (unreadNotifications > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .offset(x = (-2).dp, y = (2).dp)
                                                    .background(BrandOrange, CircleShape)
                                                    .size(16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = unreadNotifications.toString(),
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    // Cart Icon with badge
                                    Box {
                                        IconButton(onClick = onNavigateToCart, modifier = Modifier.testTag("cart_nav_button")) {
                                            Icon(Icons.Default.ShoppingCart, contentDescription = "View Cart", tint = BrandDarkGreen)
                                        }
                                        if (totalCartCount > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .offset(x = (-2).dp, y = (2).dp)
                                                    .background(BrandGreen, CircleShape)
                                                    .size(16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = totalCartCount.toString(),
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            windowInsets = WindowInsets(0),
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = Color.Transparent
                            )
                        )
                        
                        SearchBarAndFilters(
                            searchQuery = searchQuery,
                            onQueryChange = { viewModel.searchQuery.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )
                    }
                }
            },
            floatingActionButton = {
                // Floating WhatsApp chat Button
                ExtendedFloatingActionButton(
                    onClick = {
                        openWhatsApp(context)
                    },
                    containerColor = Color(0xFF25D366), // Official WhatsApp Green
                    contentColor = Color.White,
                    modifier = Modifier.testTag("whatsapp_fab")
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("WhatsApp Live Chat", fontWeight = FontWeight.Bold)
                }
            }
        ) { paddingValues ->
            BoxWithConstraints(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(BrandLightBackground)
            ) {
                val availableWidth = maxWidth
                val columnsCount = if (availableWidth > 600.dp) 3 else 2 // Tablet support: 3 columns; Mobile: 2 columns

                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnsCount),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Span the premium hero banner across all columns
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        HeroBannerCard()
                    }

                    // Span category selector pills
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        CategorySelectorSection(
                            categories = categories,
                            selectedCategory = selectedCategory,
                            onCategorySelected = { viewModel.selectedCategory.value = it }
                        )
                    }

                    // Heading for products
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val headingText = if (searchQuery.isNotEmpty()) {
                                "Results for \"$searchQuery\" (${filteredProducts.size})"
                            } else if (selectedCategory != "All") {
                                "$selectedCategory Harvest"
                            } else {
                                "Our Fresh Harvest"
                            }
                            Text(
                                text = headingText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandDarkGreen,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            SortingDropdown(
                                selectedOption = sortOption,
                                onOptionSelected = { viewModel.sortOption.value = it }
                            )
                        }
                    }

                    // Loading State with Skeleton Cards
                    if (isLoadingProducts) {
                        items(6) {
                            ProductGridItemSkeleton()
                        }
                    } else if (filteredProducts.isEmpty()) {
                        // Empty State Check
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EmptyStatePlaceholder(searchQuery)
                        }
                    } else {
                        // Product cards
                        items(filteredProducts.size) { index ->
                            val product = filteredProducts[index]
                            val isWishlisted = wishlistedIds.contains(product.id)
                            val isCompared = comparisonProducts.any { it.id == product.id }
                            ProductGridItem(
                                product = product,
                                isWishlisted = isWishlisted,
                                isCompared = isCompared,
                                onCompareClick = {
                                    val msg = viewModel.toggleComparison(product)
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                },
                                onWishlistClick = { viewModel.toggleWishlist(product) },
                                onProductClick = { onNavigateToProduct(product.id) },
                                onAddToCart = { viewModel.addToCart(product) }
                            )
                        }
                    }
                    
                    // Newsletter Subscription Footer
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        NewsletterFooter(
                            viewModel = viewModel,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }

                    // Spacing for FAB
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }

                // Floating Comparison Bar at the bottom of the screen
                AnimatedVisibility(
                    visible = comparisonProducts.isNotEmpty(),
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 76.dp) // Float above bottom area nicely
                        .padding(horizontal = 16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BrandDarkGreen),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(16.dp))
                            .testTag("comparison_floating_bar")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(BrandOrange, CircleShape)
                                        .size(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = comparisonProducts.size.toString(),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Fruits Selected for Comparison",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = comparisonProducts.joinToString(", ") { it.name },
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                TextButton(
                                    onClick = { viewModel.clearComparison() }
                                ) {
                                    Text("Clear", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                }

                                Button(
                                    onClick = { showComparisonModal = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("compare_now_button")
                                ) {
                                    Text("Compare", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Comparison Modal dialog overlay
                if (showComparisonModal) {
                    ProductComparisonModal(
                        products = comparisonProducts,
                        onDismiss = { showComparisonModal = false },
                        onAddToCart = { product -> viewModel.addToCart(product) }
                    )
                }
            }
        }
    }
}

@Composable
fun HeroBannerCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_banner),
                contentDescription = "Shaheb Fruits Banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Elegant brand overlay gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.75f),
                                Color.Transparent
                            )
                        )
                    )
            )
            // Promotional Banner Text
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(18.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(BrandOrange, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "FARM TO HOME",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Shaheb's Fresh Picks",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Get up to 20% OFF on organic Alphonso Mangoes this week!",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SearchBarAndFilters(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onQueryChange,
        placeholder = { Text("Search delicious mangoes, grapes...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BrandGreen) },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear Search", tint = BrandOrange)
                }
            }
        },
        singleLine = true,
        modifier = modifier
            .testTag("search_bar")
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedBorderColor = BrandGreen,
            unfocusedBorderColor = Color.Transparent,
            focusedLabelColor = BrandGreen,
            unfocusedLabelColor = Color.Gray
        )
    )
}

@Composable
fun CategorySelectorSection(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            val containerColor = if (isSelected) BrandGreen else Color.White
            val textColor = if (isSelected) Color.White else BrandDarkGreen
            val borderModifier = if (isSelected) Modifier else Modifier.border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(containerColor)
                    .then(borderModifier)
                    .clickable { onCategorySelected(category) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("category_pill_$category")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = when (category) {
                        "Mango" -> "🥭"
                        "Apple" -> "🍎"
                        "Banana" -> "🍌"
                        "Orange" -> "🍊"
                        "Grapes" -> "🍇"
                        "Seasonal Fruits" -> "🥝"
                        else -> "🍎"
                    }
                    if (category != "All") {
                        Text(text = icon, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = category,
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ProductGridItem(
    product: Product,
    isWishlisted: Boolean,
    isCompared: Boolean,
    onCompareClick: () -> Unit,
    onWishlistClick: () -> Unit,
    onProductClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    val context = LocalContext.current
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.22f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "ProductZoom"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable { onProductClick() }
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Creative geometric fruit background in place of actual image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                getFruitColor(product.category).copy(alpha = 0.15f),
                                getFruitColor(product.category).copy(alpha = 0.35f)
                            )
                        )
                    )
                    .clipToBounds()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                when (event.type) {
                                    PointerEventType.Enter, PointerEventType.Press -> isHovered = true
                                    PointerEventType.Exit, PointerEventType.Release -> isHovered = false
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Scaling Container for Zoom Effect
                Box(
                    modifier = Modifier
                        .scale(scale)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Generates organic artistic fruit circles
                    Canvas(modifier = Modifier.size(60.dp)) {
                        drawCircle(
                            color = getFruitColor(product.category),
                            radius = size.width / 2f
                        )
                        // Draw a cute leaf
                        drawCircle(
                            color = Color(0xFF2E7D32),
                            radius = size.width / 8f,
                            center = center.copy(
                                x = center.x + size.width / 3f,
                                y = center.y - size.height / 3f
                            )
                        )
                    }
                    Text(
                        text = getFruitEmoji(product.category),
                        fontSize = 32.sp,
                        modifier = Modifier.offset(y = (-4).dp)
                    )
                }

                // Heart toggle button for Wishlist
                IconButton(
                    onClick = onWishlistClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.75f), CircleShape)
                        .testTag("wishlist_toggle_${product.id}")
                ) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Toggle Wishlist",
                        tint = if (isWishlisted) ErrorColor else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Compare Toggle Badge (Bottom Left)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isCompared) BrandGreen else Color.White.copy(alpha = 0.85f))
                        .clickable { onCompareClick() }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                        .testTag("compare_toggle_${product.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        if (isCompared) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                        Text(
                            text = if (isCompared) "Compared" else "Compare",
                            color = if (isCompared) Color.White else BrandDarkGreen,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Stock warning label
                if (product.stock <= 5) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(ErrorColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Only ${product.stock} left", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = product.name,
                    fontSize = 14.sp,
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

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = BrandOrange,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = String.format("%.1f", product.rating),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandDarkGreen
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${product.reviewCount})",
                        fontSize = 11.sp,
                        color = MutedText
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "₹${product.price}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = BrandOrange
                        )
                        Text(
                            text = "/ ${product.unit}",
                            fontSize = 10.sp,
                            color = MutedText
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BrandGreen)
                            .clickable {
                                onAddToCart()
                                Toast.makeText(context, "${product.name} added to cart!", Toast.LENGTH_SHORT).show()
                            }
                            .testTag("add_to_cart_btn_${product.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add to Cart",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStatePlaceholder(searchQuery: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            tint = BrandGreen.copy(alpha = 0.5f),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Fruits Found",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = BrandDarkGreen
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No fruits match \"$searchQuery\". Try checking categories or adjusting filters.",
            fontSize = 13.sp,
            color = MutedText,
            textAlign = TextAlign.Center
        )
    }
}

// Utility maps for aesthetic lookups
fun getFruitColor(category: String): Color {
    return when (category) {
        "Mango" -> Color(0xFFFFB300) // Mango yellow
        "Apple" -> Color(0xFFD32F2F) // Apple red
        "Banana" -> Color(0xFFFFEB3B) // Banana yellow
        "Orange" -> Color(0xFFFF9800) // Orange
        "Grapes" -> Color(0xFF673AB7) // Grape purple
        "Seasonal Fruits" -> Color(0xFF8BC34A) // Lime/Kiwi green
        else -> Color(0xFF4CAF50)
    }
}

fun getFruitEmoji(category: String): String {
    return when (category) {
        "Mango" -> "🥭"
        "Apple" -> "🍎"
        "Banana" -> "🍌"
        "Orange" -> "🍊"
        "Grapes" -> "🍇"
        "Seasonal Fruits" -> "🥝"
        else -> "🍎"
    }
}

// Intent action to launch WhatsApp chat with Shaheb Support
fun openWhatsApp(context: Context) {
    try {
        val number = "+919876543210" // Brand WhatsApp helpline
        val message = "Hello Shaheb Fruits support, I am shopping on your app and need help with my organic fruit selection!"
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$number&text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "WhatsApp is not installed on this device.", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun NewsletterFooter(
    viewModel: FruitViewModel,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp)
            .shadow(2.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BrandDarkGreen)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Subscribe to our Newsletter",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Get weekly updates on fresh organic arrivals, delicious healthy recipes, and exclusive seasonal subscriber discounts!",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (isSuccess) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = BrandOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusMessage ?: "Thank you for subscribing!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        statusMessage = null
                    },
                    placeholder = { 
                        Text(
                            "Enter your email address",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 13.sp
                        ) 
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BrandOrange,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                        cursorColor = BrandOrange,
                        focusedLabelColor = BrandOrange,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("newsletter_email_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                statusMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = ErrorColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        if (email.isBlank()) {
                            statusMessage = "Email cannot be empty."
                            return@Button
                        }
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                            statusMessage = "Please enter a valid email address."
                            return@Button
                        }

                        isSubmitting = true
                        statusMessage = null
                        
                        viewModel.subscribeToNewsletter(
                            email = email.trim(),
                            onSuccess = {
                                isSubmitting = false
                                isSuccess = true
                                statusMessage = "Successfully subscribed!"
                                email = ""
                            },
                            onError = { errorMsg ->
                                isSubmitting = false
                                statusMessage = errorMsg
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("newsletter_subscribe_button"),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Subscribe Now", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SortingDropdown(
    selectedOption: ProductSortOption,
    onOptionSelected: (ProductSortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        AssistChip(
            onClick = { expanded = true },
            label = { 
                Text(
                    text = selectedOption.displayName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = BrandDarkGreen
                ) 
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = "Sort Products",
                    tint = BrandGreen,
                    modifier = Modifier.size(16.dp)
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = Color.White,
                labelColor = BrandDarkGreen,
                leadingIconContentColor = BrandGreen
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = Color.LightGray.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("sorting_dropdown_trigger")
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color.White)
                .testTag("sorting_dropdown_menu")
        ) {
            ProductSortOption.values().forEach { option ->
                val isSelected = option == selectedOption
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = option.displayName,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) BrandGreen else BrandDarkGreen,
                            fontSize = 13.sp
                        ) 
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(16.dp)) }
                    } else null,
                    modifier = Modifier.testTag("sort_option_${option.name}")
                )
            }
        }
    }
}

data class ProductSpecs(
    val origin: String,
    val calories: String,
    val vitaminC: String,
    val fiber: String,
    val carbs: String,
    val certifications: String
)

fun getProductSpecs(product: Product): ProductSpecs {
    return when (product.category) {
        "Mango" -> ProductSpecs(
            origin = "Ratnagiri, MH",
            calories = "60 kcal",
            vitaminC = "60% DV",
            fiber = "1.6g",
            carbs = "15g",
            certifications = "NPOP Organic"
        )
        "Apple" -> ProductSpecs(
            origin = "Shimla, HP",
            calories = "52 kcal",
            vitaminC = "8% DV",
            fiber = "2.4g",
            carbs = "14g",
            certifications = "Pesticide-Free"
        )
        "Banana" -> ProductSpecs(
            origin = "Jalgaon, MH",
            calories = "89 kcal",
            vitaminC = "15% DV",
            fiber = "2.6g",
            carbs = "23g",
            certifications = "Organic Cert."
        )
        "Orange" -> ProductSpecs(
            origin = "Nagpur, MH",
            calories = "47 kcal",
            vitaminC = "85% DV",
            fiber = "2.4g",
            carbs = "12g",
            certifications = "Juicing Grade"
        )
        "Grapes" -> ProductSpecs(
            origin = "Nashik, MH",
            calories = "69 kcal",
            vitaminC = "5% DV",
            fiber = "0.9g",
            carbs = "18g",
            certifications = "Residue-Free"
        )
        "Seasonal Fruits" -> ProductSpecs(
            origin = "Kashmir Farms",
            calories = "61 kcal",
            vitaminC = "112% DV",
            fiber = "3.0g",
            carbs = "15g",
            certifications = "Premium Grade"
        )
        else -> ProductSpecs(
            origin = "Local Farm",
            calories = "55 kcal",
            vitaminC = "20% DV",
            fiber = "2.0g",
            carbs = "14g",
            certifications = "Local Organic"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductComparisonModal(
    products: List<Product>,
    onDismiss: () -> Unit,
    onAddToCart: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 12.dp)
            .testTag("product_comparison_dialog"),
        content = {
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandLightBackground),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = BrandGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Fruit Comparison Specs",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandDarkGreen
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .background(Color.White, CircleShape)
                                .size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Dialog",
                                tint = BrandDarkGreen,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (products.isEmpty()) {
                        Text(
                            text = "No fruits selected for comparison.",
                            color = MutedText,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        // Table layout or side-by-side columns
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Side-by-side columns for selected products
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                products.forEach { product ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(Color.White, RoundedCornerShape(12.dp))
                                            .border(1.dp, BrandGreen.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                            .padding(6.dp)
                                    ) {
                                        val specs = getProductSpecs(product)
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            // Fruit Art Thumbnail Icon
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(getFruitColor(product.category).copy(alpha = 0.12f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Canvas(modifier = Modifier.size(24.dp)) {
                                                    drawCircle(
                                                        color = getFruitColor(product.category),
                                                        radius = size.width / 2f
                                                    )
                                                }
                                                Text(
                                                    text = getFruitEmoji(product.category),
                                                    fontSize = 18.sp
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = product.name,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BrandDarkGreen,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = product.category,
                                                fontSize = 9.sp,
                                                color = BrandOrange,
                                                fontWeight = FontWeight.SemiBold
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))
                                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                                            Spacer(modifier = Modifier.height(4.dp))

                                            // Comparison details
                                            ComparisonItemLabel("Price", "₹${product.price} / ${product.unit}")
                                            ComparisonItemLabel("Rating", "★ ${String.format("%.1f", product.rating)}")
                                            ComparisonItemLabel("Stock", if (product.stock > 0) "${product.stock} left" else "Out of stock")
                                            ComparisonItemLabel("Origin", specs.origin)
                                            ComparisonItemLabel("Calories", specs.calories)
                                            ComparisonItemLabel("Vitamin C", specs.vitaminC)
                                            ComparisonItemLabel("Carbs", specs.carbs)
                                            ComparisonItemLabel("Fiber", specs.fiber)
                                            ComparisonItemLabel("Organic Spec", specs.certifications)

                                            Spacer(modifier = Modifier.height(6.dp))
                                            
                                            // Quick purchase actions
                                            val context = LocalContext.current
                                            if (product.stock > 0) {
                                                Button(
                                                    onClick = { 
                                                        onAddToCart(product)
                                                        Toast.makeText(context, "${product.name} added to cart!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(28.dp)
                                                        .testTag("compare_add_to_cart_${product.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ShoppingCart,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text("Add", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                Button(
                                                    onClick = {},
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                                                    shape = RoundedCornerShape(6.dp),
                                                    enabled = false,
                                                    contentPadding = PaddingValues(0.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(28.dp)
                                                ) {
                                                    Text("Sold Out", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ComparisonItemLabel(
    title: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 8.sp,
            color = MutedText,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 10.sp,
            color = BrandDarkGreen,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.15f))
    }
}

package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Order
import com.example.data.Product
import com.example.ui.theme.*
import com.example.viewmodel.FruitViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: FruitViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Products Manager", "Live Orders", "Analytics Hub")

    // DB States
    val adminProducts by viewModel.allAdminProducts.collectAsState()
    val adminOrders by viewModel.userOrders.collectAsState() // ViewModel handles returning all orders for admin role

    // Dialog state for add/edit product
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    // Dialog inputs
    var prodName by remember { mutableStateOf("") }
    var prodCategory by remember { mutableStateOf("Mango") }
    var prodDesc by remember { mutableStateOf("") }
    var prodPrice by remember { mutableStateOf("") }
    var prodUnit by remember { mutableStateOf("1 kg") }
    var prodStock by remember { mutableStateOf("") }

    val categories = listOf("Mango", "Apple", "Banana", "Orange", "Grapes", "Seasonal Fruits")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Console ✨", fontWeight = FontWeight.Bold, color = BrandDarkGreen) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go Back", tint = BrandDarkGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandLightBackground)
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = {
                        prodName = ""
                        prodCategory = "Mango"
                        prodDesc = ""
                        prodPrice = ""
                        prodUnit = "1 kg"
                        prodStock = ""
                        showAddDialog = true
                    },
                    containerColor = BrandOrange,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_product_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add New Fruit")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BrandLightBackground)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Tab selector bar
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = BrandGreen,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = BrandGreen
                        )
                    }
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                // Dynamic tab content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (selectedTab) {
                        0 -> AdminProductsTab(
                            products = adminProducts,
                            onEditClick = { product ->
                                editingProduct = product
                                prodName = product.name
                                prodCategory = product.category
                                prodDesc = product.description
                                prodPrice = product.price.toString()
                                prodUnit = product.unit
                                prodStock = product.stock.toString()
                                showEditDialog = true
                            },
                            onDeleteClick = { id ->
                                viewModel.adminDeleteProduct(id)
                                Toast.makeText(context, "Fruit Deleted successfully!", Toast.LENGTH_SHORT).show()
                            }
                        )
                        1 -> AdminOrdersTab(
                            orders = adminOrders,
                            onStatusChange = { order, newStatus ->
                                viewModel.adminUpdateOrderStatus(order, newStatus)
                                Toast.makeText(context, "Order #${order.id} status updated to $newStatus", Toast.LENGTH_SHORT).show()
                            }
                        )
                        2 -> AdminAnalyticsTab(
                            products = adminProducts,
                            orders = adminOrders
                        )
                    }
                }
            }

            // ADD PRODUCT DIALOG
            if (showAddDialog) {
                AdminProductDialog(
                    title = "Add Premium Fruit",
                    name = prodName,
                    category = prodCategory,
                    desc = prodDesc,
                    price = prodPrice,
                    unit = prodUnit,
                    stock = prodStock,
                    categories = categories,
                    onNameChange = { prodName = it },
                    onCategoryChange = { prodCategory = it },
                    onDescChange = { prodDesc = it },
                    onPriceChange = { prodPrice = it },
                    onUnitChange = { prodUnit = it },
                    onStockChange = { prodStock = it },
                    onDismiss = { showAddDialog = false },
                    onConfirm = {
                        val priceVal = prodPrice.toDoubleOrNull() ?: 0.0
                        val stockVal = prodStock.toIntOrNull() ?: 0
                        if (prodName.isNotBlank() && priceVal > 0 && stockVal >= 0) {
                            viewModel.adminAddProduct(prodName, prodCategory, prodDesc, priceVal, prodUnit, stockVal)
                            Toast.makeText(context, "$prodName cataloged successfully!", Toast.LENGTH_SHORT).show()
                            showAddDialog = false
                        } else {
                            Toast.makeText(context, "Please enter valid values.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            // EDIT PRODUCT DIALOG
            if (showEditDialog && editingProduct != null) {
                AdminProductDialog(
                    title = "Edit Fruit Specifications",
                    name = prodName,
                    category = prodCategory,
                    desc = prodDesc,
                    price = prodPrice,
                    unit = prodUnit,
                    stock = prodStock,
                    categories = categories,
                    onNameChange = { prodName = it },
                    onCategoryChange = { prodCategory = it },
                    onDescChange = { prodDesc = it },
                    onPriceChange = { prodPrice = it },
                    onUnitChange = { prodUnit = it },
                    onStockChange = { prodStock = it },
                    onDismiss = { showEditDialog = false },
                    onConfirm = {
                        val priceVal = prodPrice.toDoubleOrNull() ?: 0.0
                        val stockVal = prodStock.toIntOrNull() ?: 0
                        if (prodName.isNotBlank() && priceVal > 0 && stockVal >= 0) {
                            val updated = editingProduct!!.copy(
                                name = prodName,
                                category = prodCategory,
                                description = prodDesc,
                                price = priceVal,
                                unit = prodUnit,
                                stock = stockVal
                            )
                            viewModel.adminUpdateProduct(updated)
                            Toast.makeText(context, "$prodName updated successfully!", Toast.LENGTH_SHORT).show()
                            showEditDialog = false
                        } else {
                            Toast.makeText(context, "Please enter valid values.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AdminProductsTab(
    products: List<Product>,
    onEditClick: (Product) -> Unit,
    onDeleteClick: (Int) -> Unit
) {
    if (products.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No products in catalogue yet.", color = MutedText)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(products) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // mini graphic
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(getFruitColor(product.category).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(getFruitEmoji(product.category), fontSize = 18.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandDarkGreen)
                            Text("Category: ${product.category}", fontSize = 11.sp, color = MutedText)
                            Row {
                                Text("Price: ₹${product.price}/${product.unit}  |  ", fontSize = 11.sp, color = MutedText)
                                Text(
                                    text = "Stock: ${product.stock} units",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (product.stock <= 5) ErrorColor else BrandGreen
                                )
                            }
                        }

                        // operations
                        IconButton(onClick = { onEditClick(product) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Product", tint = BrandGreen)
                        }
                        IconButton(onClick = { onDeleteClick(product.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Product", tint = ErrorColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminOrdersTab(
    orders: List<Order>,
    onStatusChange: (Order, String) -> Unit
) {
    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No client orders received yet.", color = MutedText)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(orders) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Order ID: SF-${order.id}992", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandDarkGreen)
                            Text(
                                text = "₹${String.format("%.2f", order.totalAmount)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = BrandOrange
                            )
                        }

                        Divider(color = Color.LightGray.copy(alpha = 0.5f))

                        Text("Items: ${order.itemsSummary}", fontSize = 12.sp, color = MutedText, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text("Address: ${order.deliveryAddress}", fontSize = 11.sp, color = MutedText)
                        Text("Phone: ${order.phoneNumber}", fontSize = 11.sp, color = MutedText)

                        Divider(color = Color.LightGray.copy(alpha = 0.3f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Change Stage:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandDarkGreen)

                            // Status selectors
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val states = listOf("CONFIRMED", "SHIPPED", "DELIVERED")
                                states.forEach { s ->
                                    val isCurrent = order.orderStatus == s
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isCurrent) BrandGreen else LightGreenAccent)
                                            .clickable { onStatusChange(order, s) }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(s, color = if (isCurrent) Color.White else BrandDarkGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
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

@Composable
fun AdminAnalyticsTab(
    products: List<Product>,
    orders: List<Order>
) {
    val totalRevenue = orders.sumOf { it.totalAmount }
    val totalOrders = orders.size
    val lowStockCount = products.count { it.stock <= 5 }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // KPI Dashboard Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Payments, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Total Income", fontSize = 11.sp, color = MutedText)
                        Text("₹${String.format("%.0f", totalRevenue)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandDarkGreen)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = BrandOrange, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Total Orders", fontSize = 11.sp, color = MutedText)
                        Text(totalOrders.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandDarkGreen)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorColor, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Low Stocks", fontSize = 11.sp, color = MutedText)
                        Text(lowStockCount.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ErrorColor)
                    }
                }
            }
        }

        // Custom drawn Analytics Canvas Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Category Crop Inventory Density", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandDarkGreen)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Group density
                    val categories = listOf("Mango", "Apple", "Banana", "Orange", "Grapes", "Seasonal Fruits")
                    val densities = categories.map { cat ->
                        products.filter { it.category == cat }.sumOf { it.stock }
                    }
                    val maxStock = densities.maxOrNull()?.coerceAtLeast(1) ?: 1

                    // Custom horizontal bar drawing
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        categories.forEachIndexed { idx, cat ->
                            val stock = densities[idx]
                            val progress = stock.toFloat() / maxStock

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(cat, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BrandDarkGreen)
                                    Text("$stock units", fontSize = 11.sp, color = MutedText)
                                }
                                Spacer(modifier = Modifier.height(4.dp))

                                // Render custom progress bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(12.dp)
                                        .clip(CircleShape)
                                        .background(LightGreenAccent)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(progress)
                                            .fillMaxHeight()
                                            .clip(CircleShape)
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(BrandGreen, BrandOrange)
                                                )
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminProductDialog(
    title: String,
    name: String,
    category: String,
    desc: String,
    price: String,
    unit: String,
    stock: String,
    categories: List<String>,
    onNameChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onDescChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onStockChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, color = BrandDarkGreen, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Fruit Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category dropdown selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        label = { Text("Category") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { isDropdownExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { isDropdownExpanded = true }
                    )
                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    onCategoryChange(cat)
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = desc,
                    onValueChange = onDescChange,
                    label = { Text("Description") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = onPriceChange,
                        label = { Text("Price (INR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = onUnitChange,
                        label = { Text("Unit (e.g. 1 kg)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = stock,
                    onValueChange = onStockChange,
                    label = { Text("Initial Stock") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MutedText)
            }
        }
    )
}

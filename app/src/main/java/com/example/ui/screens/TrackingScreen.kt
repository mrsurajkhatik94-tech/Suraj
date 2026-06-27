package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.Order
import com.example.ui.theme.*
import com.example.viewmodel.FruitViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    viewModel: FruitViewModel,
    targetOrderId: Int?, // Optional specific order to highlight first
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val orders by viewModel.userOrders.collectAsState()
    var selectedOrder by remember { mutableStateOf<Order?>(null) }

    // Auto highlight target or newest order on launch
    LaunchedEffect(orders, targetOrderId) {
        if (orders.isNotEmpty()) {
            selectedOrder = if (targetOrderId != null) {
                orders.find { it.id == targetOrderId } ?: orders.first()
            } else {
                orders.first()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track My Orders", fontWeight = FontWeight.Bold, color = BrandDarkGreen) },
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
            if (orders.isEmpty()) {
                EmptyOrdersPlaceholder(onNavigateHome = onNavigateBack)
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Quick Selector row for past orders
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .shadow(1.dp, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Your Orders Log", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandDarkGreen)
                            Spacer(modifier = Modifier.height(8.dp))

                            Box(modifier = Modifier.fillMaxWidth().height(60.dp)) {
                                val scrollState = rememberScrollState()
                                Row(
                                    modifier = Modifier.horizontalScroll(scrollState),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    orders.forEach { order ->
                                        val isSel = selectedOrder?.id == order.id
                                        val bg = if (isSel) BrandGreen else LightGreenAccent
                                        val textCol = if (isSel) Color.White else BrandDarkGreen

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(bg)
                                                .clickable { selectedOrder = order }
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = "Order #${order.id}",
                                                color = textCol,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Selected Order Status details
                    selectedOrder?.let { order ->
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Section: Status Tracker Timeline
                            item {
                                TrackingTimelineCard(order)
                            }

                            // Section: Order Items Summary Invoice
                            item {
                                InvoiceSummaryCard(order)
                            }

                            // Support Card
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = LightOrangeAccent),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Need fast delivery updates?", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandOrange)
                                            Text("Chat directly with our shipping helpline regarding Order #${order.id}", fontSize = 11.sp, color = MutedText, lineHeight = 14.sp)
                                        }
                                        Button(
                                            onClick = { openWhatsApp(context) },
                                            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Help", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrackingTimelineCard(order: Order) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Tracking ID: SF-${order.id}992", fontSize = 12.sp, color = MutedText)
                    val date = remember(order.timestamp) {
                        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(order.timestamp))
                    }
                    Text(text = "Placed on $date", fontSize = 11.sp, color = MutedText)
                }

                Box(
                    modifier = Modifier
                        .background(
                            if (order.orderStatus == "DELIVERED") LightGreenAccent else LightOrangeAccent,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = order.orderStatus,
                        color = if (order.orderStatus == "DELIVERED") BrandGreen else BrandOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Progress Timeline UI Block
            val statuses = listOf("PENDING", "CONFIRMED", "SHIPPED", "DELIVERED")
            val currentIdx = statuses.indexOf(order.orderStatus)

            statuses.forEachIndexed { idx, step ->
                val isDone = idx <= currentIdx
                val isCurrent = idx == currentIdx

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circle indicator & Line connectors
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(40.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDone) BrandGreen else Color.LightGray
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            } else {
                                Box(modifier = Modifier.size(6.dp).background(Color.White, CircleShape))
                            }
                        }

                        // Vertical connecting line
                        if (idx < statuses.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(30.dp)
                                    .background(
                                        if (idx < currentIdx) BrandGreen else Color.LightGray
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Text descriptions
                    Column {
                        Text(
                            text = when (step) {
                                "PENDING" -> "Order Received"
                                "CONFIRMED" -> "Fresh Fruits Selected"
                                "SHIPPED" -> "Out for Delivery"
                                else -> "Delivered to Customer"
                            },
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            color = if (isDone) BrandDarkGreen else MutedText,
                            fontSize = 14.sp
                        )
                        Text(
                            text = when (step) {
                                "PENDING" -> "Shaheb corporate received your organic cart request."
                                "CONFIRMED" -> "Our warehouse in-charge curated prime grade mangoes and oranges."
                                "SHIPPED" -> "Dispatched with certified climate controlled cold-delivery container."
                                else -> "Placed directly at your doorstep safely. Enjoy organic freshness!"
                            },
                            fontSize = 11.sp,
                            color = MutedText,
                            lineHeight = 14.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceSummaryCard(order: Order) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Delivery Invoice Summary", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandDarkGreen)
            Divider(color = Color.LightGray.copy(alpha = 0.5f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Payment Type", fontSize = 12.sp, color = MutedText)
                Text(order.paymentMethod, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandDarkGreen)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Payment Status", fontSize = 12.sp, color = MutedText)
                Text(order.paymentStatus, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (order.paymentStatus == "PAID") BrandGreen else BrandOrange)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Deliver To", fontSize = 12.sp, color = MutedText)
                Text(order.phoneNumber, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandDarkGreen)
            }

            Column {
                Text("Destination Address", fontSize = 11.sp, color = MutedText)
                Text(order.deliveryAddress, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandDarkGreen, lineHeight = 16.sp)
            }

            Divider(color = Color.LightGray.copy(alpha = 0.3f))

            Column {
                Text("Ordered Fruits", fontSize = 11.sp, color = MutedText)
                Text(order.itemsSummary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandDarkGreen, lineHeight = 16.sp)
            }

            Divider(color = Color.LightGray.copy(alpha = 0.3f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Amount Charged", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandDarkGreen)
                Text("₹${String.format("%.2f", order.totalAmount)}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = BrandOrange)
            }
        }
    }
}

@Composable
fun EmptyOrdersPlaceholder(onNavigateHome: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(LightGreenAccent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("No Orders Placed Yet", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BrandDarkGreen)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Your premium organic harvest logs will be visible here once you checkout some products.", fontSize = 13.sp, color = MutedText, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNavigateHome,
            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Browse Fresh Produce", color = Color.White)
        }
    }
}

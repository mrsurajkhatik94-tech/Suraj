package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppNotification
import com.example.ui.theme.*
import com.example.viewmodel.FruitViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: FruitViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount = notifications.count { !it.isRead }

    // Sample promos list for the custom simulation panel
    val samplePromos = listOf(
        Pair("🍏 Crisp Green Apple Sale!", "Refresh your summer with granny smiths at an unbelievable flat 15% OFF today only!"),
        Pair("🍊 Juicy Orange Fest!", "Buy 3kg of sweet Nagpur oranges and get a complimentary 500g grapes box! Use code JUICY3."),
        Pair("🍌 Banana Bundle Deal!", "Save ₹30 on premium robusta banana dozens. High-potassium natural snacks for your mornings!"),
        Pair("🍉 Weekend Watermelon Rush!", "Stay hydrated! Fresh watermelons sliced and shipped chilled at 20% flat discount.")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Notifications Inbox", fontWeight = FontWeight.Bold, color = BrandDarkGreen)
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(
                                containerColor = BrandOrange,
                                contentColor = Color.White
                            ) {
                                Text("$unreadCount new", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(2.dp))
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go Back", tint = BrandDarkGreen)
                    }
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.markAllNotificationsAsRead() },
                            modifier = Modifier.testTag("mark_read_all")
                        ) {
                            Text("Mark all read", color = BrandGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Quick simulator drawer/panel to make tests extremely straightforward and interactive
                SimulationControlPanel(
                    onSimulatePromo = { title, msg ->
                        viewModel.triggerPromotionalNotification(context, title, msg)
                    },
                    promos = samplePromos
                )

                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = BrandDarkGreen.copy(alpha = 0.3f),
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Your Inbox is Empty",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandDarkGreen
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "We'll keep you posted with the latest order updates, delivery statuses, and seasonal hot deals!",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(notifications, key = { it.id }) { notif ->
                            NotificationItemCard(
                                notification = notif,
                                onDelete = { viewModel.deleteNotification(notif.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimulationControlPanel(
    onSimulatePromo: (String, String) -> Unit,
    promos: List<Pair<String, String>>
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(1.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = LightOrangeAccent),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = BrandOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Notification Testing Center",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandDarkGreen
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = BrandDarkGreen
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Click a button to instantly trigger a system push notification and write it to your inbox:",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onSimulatePromo(
                                "🍉 Chilled Summer Rush!",
                                "Sweet Watermelons are currently priced at flat ₹40 per unit! Offer expires soon."
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text("Promo Deal 🎁", fontSize = 11.sp, color = Color.White)
                    }

                    Button(
                        onClick = {
                            onSimulatePromo(
                                "🚚 Delivery Update: Out for Delivery",
                                "Great news! Courier driver Shaheb is on the way to your door with fresh organic harvests."
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text("Delivery Status 🚚", fontSize = 11.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text("Pre-set Promotional Campaigns:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandDarkGreen)
                    Spacer(modifier = Modifier.height(6.dp))
                    promos.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSimulatePromo(item.first, item.second) }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.first, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                                Text(item.second, fontSize = 9.sp, color = Color.Gray, maxLines = 1)
                            }
                            Icon(Icons.Default.Send, contentDescription = "Simulate", tint = BrandGreen, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItemCard(
    notification: AppNotification,
    onDelete: () -> Unit
) {
    val (icon, tintColor, badgeBg) = when (notification.type) {
        "ORDER" -> Triple(Icons.Default.ShoppingBag, BrandDarkGreen, LightGreenAccent)
        "DELIVERY" -> Triple(Icons.Default.LocalShipping, BrandGreen, LightGreenAccent)
        "PROMOTION" -> Triple(Icons.Default.Campaign, BrandOrange, LightOrangeAccent)
        else -> Triple(Icons.Default.Notifications, Color.Gray, Color.LightGray.copy(alpha = 0.4f))
    }

    val dateStr = try {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        sdf.format(Date(notification.timestamp))
    } catch (e: Exception) {
        ""
    }

    val cardBg = if (notification.isRead) Color.White else Color(0xFFF0FAF3)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tintColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 14.sp,
                        fontWeight = if (notification.isRead) FontWeight.Bold else FontWeight.ExtraBold,
                        color = BrandDarkGreen
                    )
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(BrandOrange)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateStr,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Light
                    )

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Notification",
                            tint = Color.Red.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CreditCard
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.FruitViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: FruitViewModel,
    onOrderPlaced: (Int) -> Unit, // returns placed order ID
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val currentUser by viewModel.currentUser.collectAsState()
    val grandTotal by viewModel.grandTotal.collectAsState()

    // Form inputs
    var deliveryAddress by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("UPI") }

    // Mock payment inputs
    var upiId by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    var selectedBank by remember { mutableStateOf("State Bank of India") }

    // Loading & Overlay states
    var isProcessingPayment by remember { mutableStateOf(false) }

    // Autofill details from user profile
    LaunchedEffect(currentUser) {
        currentUser?.let {
            deliveryAddress = it.address
            phoneNumber = it.phone
        }
    }

    val banks = listOf("State Bank of India", "HDFC Bank", "ICICI Bank", "Axis Bank", "Kotak Mahindra Bank")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Secure Checkout", fontWeight = FontWeight.Bold, color = BrandDarkGreen) },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Delivery Information
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = BrandGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delivery Destination", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandDarkGreen)
                        }

                        Divider(color = Color.LightGray.copy(alpha = 0.5f))

                        OutlinedTextField(
                            value = deliveryAddress,
                            onValueChange = { deliveryAddress = it },
                            label = { Text("Complete Delivery Address") },
                            placeholder = { Text("Enter Flat No., Street, Area, Pin Code") },
                            modifier = Modifier.fillMaxWidth().testTag("address_input"),
                            shape = RoundedCornerShape(10.dp),
                            maxLines = 3
                        )

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Contact Phone Number") },
                            placeholder = { Text("e.g. +91 9988776655") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth().testTag("phone_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                }

                // Section 2: Online Payment Integration Options
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payment, contentDescription = null, tint = BrandGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Payment Gateway Options", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandDarkGreen)
                        }

                        Divider(color = Color.LightGray.copy(alpha = 0.5f))

                        // Selection list
                        val paymentMethods = listOf("UPI", "Credit/Debit Card", "Net Banking", "Cash on Delivery")
                        paymentMethods.forEach { method ->
                            val isSelected = selectedPaymentMethod == method
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) LightGreenAccent else Color.Transparent)
                                    .clickable { selectedPaymentMethod = method }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedPaymentMethod = method },
                                    colors = RadioButtonDefaults.colors(selectedColor = BrandGreen)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(
                                    imageVector = when (method) {
                                        "UPI" -> Icons.Default.QrCode
                                        "Credit/Debit Card" -> Icons.Default.CreditCard
                                        "Net Banking" -> Icons.Default.AccountBalance
                                        else -> Icons.Default.Payments
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) BrandGreen else MutedText,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = method,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = BrandDarkGreen,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                // Section 3: Dynamic Form based on selected payment method
                AnimatedContent(
                    targetState = selectedPaymentMethod,
                    label = "PaymentFormTransition"
                ) { method ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(1.dp, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Secure $method Details",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandDarkGreen
                            )

                            Divider(color = Color.LightGray.copy(alpha = 0.5f))

                            when (method) {
                                "UPI" -> {
                                    OutlinedTextField(
                                        value = upiId,
                                        onValueChange = { upiId = it },
                                        label = { Text("Your UPI ID") },
                                        placeholder = { Text("e.g. user@okhdfcbank") },
                                        modifier = Modifier.fillMaxWidth().testTag("upi_input"),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true
                                    )
                                    Text("Immediate payment authorization via GooglePay, PhonePe, or BHIM.", fontSize = 11.sp, color = MutedText)
                                }

                                "Credit/Debit Card" -> {
                                    OutlinedTextField(
                                        value = cardNumber,
                                        onValueChange = { cardNumber = it },
                                        label = { Text("16-Digit Card Number") },
                                        placeholder = { Text("0000 0000 0000 0000") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth().testTag("card_num_input"),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = cardExpiry,
                                            onValueChange = { cardExpiry = it },
                                            label = { Text("Expiry (MM/YY)") },
                                            placeholder = { Text("12/29") },
                                            modifier = Modifier.weight(1f).testTag("card_expiry_input"),
                                            shape = RoundedCornerShape(10.dp),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = cardCvv,
                                            onValueChange = { cardCvv = it },
                                            label = { Text("CVV Security Code") },
                                            placeholder = { Text("123") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f).testTag("card_cvv_input"),
                                            shape = RoundedCornerShape(10.dp),
                                            singleLine = true
                                        )
                                    }
                                    Text("Safe & encrypted payment vault. 128-bit SSL secured.", fontSize = 11.sp, color = MutedText)
                                }

                                "Net Banking" -> {
                                    Text("Select Your Retail Bank:", fontSize = 12.sp, color = MutedText)
                                    banks.forEach { bank ->
                                        val isBankSel = selectedBank == bank
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isBankSel) LightOrangeAccent else Color.Transparent)
                                                .clickable { selectedBank = bank }
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = isBankSel, onClick = { selectedBank = bank })
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(bank, fontSize = 13.sp, color = BrandDarkGreen)
                                        }
                                    }
                                }

                                "Cash on Delivery" -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = BrandGreen)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            "Pay in cash or UPI QR code at your doorstep upon receiving fresh organic products safely.",
                                            fontSize = 13.sp,
                                            color = MutedText,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 4: Secure Action Trigger
                Button(
                    onClick = {
                        // Validation Checks
                        if (deliveryAddress.isBlank()) {
                            Toast.makeText(context, "Please enter a valid delivery address", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (phoneNumber.isBlank() || phoneNumber.length < 8) {
                            Toast.makeText(context, "Please enter a valid contact phone number", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // Payment specific validation
                        if (selectedPaymentMethod == "UPI" && upiId.isBlank()) {
                            Toast.makeText(context, "Please enter your UPI ID", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (selectedPaymentMethod == "Credit/Debit Card" && (cardNumber.isBlank() || cardExpiry.isBlank() || cardCvv.isBlank())) {
                            Toast.makeText(context, "Please fill in all card details", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // Trigger payment loader and final order placing
                        coroutineScope.launch {
                            isProcessingPayment = true
                            delay(2500) // Realistic secure banking transaction simulation
                            isProcessingPayment = false

                            viewModel.placeOrder(
                                address = deliveryAddress,
                                phone = phoneNumber,
                                paymentMethod = selectedPaymentMethod
                            ) { order ->
                                // On profile update if blank
                                viewModel.updateUserProfile(phoneNumber, deliveryAddress)
                                Toast.makeText(context, "Order Placed Successfully! 🎉", Toast.LENGTH_LONG).show()
                                onOrderPlaced(order.id)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("place_order_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Authorize & Pay ₹${String.format("%.2f", grandTotal)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Secure Banking Overlay Processing Frame
            if (isProcessingPayment) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.width(280.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = BrandGreen, strokeWidth = 4.dp, modifier = Modifier.size(50.dp))
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                "Processing Payment Securely",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = BrandDarkGreen,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Verifying details with your banking gateway. Please do not close the app or click back.",
                                fontSize = 11.sp,
                                color = MutedText,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

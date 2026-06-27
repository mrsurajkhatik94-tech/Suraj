package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoPagesScreen(
    pageType: String, // "About Us", "Contact Us", "FAQ", "Privacy Policy", "Terms & Conditions"
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pageType, fontWeight = FontWeight.Bold, color = BrandDarkGreen) },
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (pageType) {
                    "About Us" -> {
                        item { AboutUsContent() }
                    }
                    "Contact Us" -> {
                        item { ContactUsContent() }
                    }
                    "FAQ" -> {
                        item { FAQContent() }
                    }
                    "Privacy Policy" -> {
                        item { PrivacyPolicyContent() }
                    }
                    "Terms & Conditions" -> {
                        item { TermsAndConditionsContent() }
                    }
                }
            }
        }
    }
}

@Composable
fun AboutUsContent() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Feature Hero Image Card
        Card(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))
                Text(
                    text = "Rooted in Purity since 2012",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Our Legacy", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandDarkGreen)
                Text(
                    text = "Shaheb Fruit Company was founded in Mumbai with a singular commitment: delivering unadulterated, farm-fresh fruit delicacies directly to premium households. We source our mangoes, grapes, oranges, and apples straight from organic certified growers across Maharashtra, Kashmir, and Gujarat.",
                    fontSize = 14.sp,
                    color = MutedText,
                    lineHeight = 22.sp
                )
                Text(
                    text = "We actively bypass traditional middlemen to ensure growers get paid fairly and our patrons receive optimal freshness within hours of harvesting. Each piece of fruit is hand-inspected for weight, blemish constraints, sugar density, and optimal peak ripening states.",
                    fontSize = 14.sp,
                    color = MutedText,
                    lineHeight = 22.sp
                )
            }
        }

        // Values grids
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Our Core Pillars", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandDarkGreen)

                listOf(
                    Pair(Icons.Default.Eco, "100% Naturally Ripened") to "We strictly prohibit toxic chemicals, carbide sprays, or unnatural waxes. Our fruits ripen organically.",
                    Pair(Icons.Default.LocalShipping, "Cold Chain Logistics") to "Our cold vans preserve maximum crispness and nutrients from rural farms directly to your table.",
                    Pair(Icons.Default.People, "Empowering Farmers") to "Over 250 farming families partner with Shaheb, enjoying direct pay-outs and modern farming aid."
                ).forEach { (pair, desc) ->
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier.size(36.dp).background(LightGreenAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(pair.first, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(pair.second, fontWeight = FontWeight.Bold, color = BrandDarkGreen, fontSize = 14.sp)
                            Text(desc, color = MutedText, fontSize = 12.sp, lineHeight = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun ContactUsContent() {
    val context = LocalContext.current
    var contactName by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var contactQuery by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Direct support info cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Get In Touch", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandDarkGreen)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = BrandOrange)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("+91 9876543210 (Toll-Free Helpline)", fontSize = 13.sp, color = MutedText)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = BrandOrange)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("care@shahebfruits.com", fontSize = 13.sp, color = MutedText)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandOrange)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Shaheb Fruit Towers, Crawford Market, Mumbai, 400001", fontSize = 13.sp, color = MutedText)
                }
            }
        }

        // Mock Interactive Google Maps Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Our Store Location", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BrandDarkGreen)
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    // Geometric map background
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(Color(0xFFC8E6C9)) // green lands
                        // draw roads
                        drawLine(Color.White, start = androidx.compose.ui.geometry.Offset(0f, 150f), end = androidx.compose.ui.geometry.Offset(this.size.width, 150f), strokeWidth = 30f)
                        drawLine(Color.White, start = androidx.compose.ui.geometry.Offset(300f, 0f), end = androidx.compose.ui.geometry.Offset(300f, this.size.height), strokeWidth = 30f)
                    }
                    // Pin
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = ErrorColor, modifier = Modifier.size(36.dp).offset(y = (-10).dp))
                    Text("Shaheb Headquarters", color = BrandDarkGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        openMapsNavigation(context)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Get Directions on Google Maps", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Support Message form
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Leave A Message", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BrandDarkGreen)

                OutlinedTextField(
                    value = contactName,
                    onValueChange = { contactName = it },
                    label = { Text("Your Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = contactEmail,
                    onValueChange = { contactEmail = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = contactQuery,
                    onValueChange = { contactQuery = it },
                    label = { Text("Describe your query / feedback") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )

                Button(
                    onClick = {
                        if (contactName.isNotBlank() && contactEmail.isNotBlank() && contactQuery.isNotBlank()) {
                            Toast.makeText(context, "Query submitted! We will reach back to you in 2 hours.", Toast.LENGTH_LONG).show()
                            contactName = ""
                            contactEmail = ""
                            contactQuery = ""
                        } else {
                            Toast.makeText(context, "Please fill in all details.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Submit Query", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun FAQContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Frequently Answered Questions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandDarkGreen, modifier = Modifier.padding(bottom = 4.dp))

        val faqs = listOf(
            "Are your fruits 100% organic and wax-free?" to "Absolutely. We are proud partners of organic certified grower associations. None of our mangoes or apples undergo wax coatings, carbide ripening, or toxic pesticide showers. We believe in natural purity.",
            "How fast is your delivery service?" to "We deliver across major metro areas in under 12-24 hours. Placing an order before 11:00 AM ensures same-day afternoon cold-chain delivery. Orders placed later will arrive the next morning.",
            "Do you offer Cash on Delivery (COD)?" to "Yes, we accept cash as well as contactless digital UPI (GPay, PhonePe, Paytm) at your doorstep during delivery. There are no additional fees for COD.",
            "Can I get a refund if the fruits are damaged?" to "Yes. Shaheb stands by premium quality. If any fruit is damaged during delivery, simply take a photo and WhatsApp it to our hotline within 2 hours of delivery. We will issue a 100% replacement or refund immediately.",
            "How do I track my delivery status?" to "Simply open the navigation menu in the top-left, click 'Track Orders', and select your active order to see real-time updates from our cold warehouse sorting to your doorstep."
        )

        faqs.forEach { (q, a) ->
            var isExpanded by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .shadow(0.5.dp, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(q, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandDarkGreen, modifier = Modifier.weight(0.9f))
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = BrandOrange
                        )
                    }

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = a,
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
}

@Composable
fun PrivacyPolicyContent() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Privacy Policy", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandDarkGreen)
            Divider(color = Color.LightGray.copy(alpha = 0.5f))

            Text("1. Data Collection", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandDarkGreen)
            Text("We gather only standard shipping coordinates, delivery contact phones, and authentication credentials to operate your fruit e-commerce orders. We do NOT store or parse any of your raw credit card numbers or UPI banking secrets.", fontSize = 12.sp, color = MutedText, lineHeight = 16.sp)

            Text("2. Location Coordinates", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandDarkGreen)
            Text("Our application may query location data strictly to autofill doorstep destination coordinates or map nearby premium stores. This is strictly voluntary and user authorized.", fontSize = 12.sp, color = MutedText, lineHeight = 16.sp)

            Text("3. Zero Spam Policy", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandDarkGreen)
            Text("Shaheb Fruit Company never trades, leases, or leaks your contact address or mail directory to external marketing corporations. You will receive only transactional updates and weekly harvest discounts.", fontSize = 12.sp, color = MutedText, lineHeight = 16.sp)
        }
    }
}

@Composable
fun TermsAndConditionsContent() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Terms & Conditions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandDarkGreen)
            Divider(color = Color.LightGray.copy(alpha = 0.5f))

            Text("1. Service and Operations", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandDarkGreen)
            Text("These terms oversee usage of the Shaheb e-commerce mobile system. By registering or checking out fruits, you agree to our policies of verified crop transactions.", fontSize = 12.sp, color = MutedText, lineHeight = 16.sp)

            Text("2. Perishable Assets Policy", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandDarkGreen)
            Text("Because fruits are raw, perishable assets, exact sizing, color gradients, and sweetness indices can fluctuate organically. We do not warranty structural clone-like consistency.", fontSize = 12.sp, color = MutedText, lineHeight = 16.sp)

            Text("3. Secure Accounts", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandDarkGreen)
            Text("Users must take complete responsibility for protecting their login profiles and local database integrity. Promptly report any malicious system activities.", fontSize = 12.sp, color = MutedText, lineHeight = 16.sp)
        }
    }
}

// Intent action to launch Google Maps with Shaheb Crawford Market coordinates
fun openMapsNavigation(context: Context) {
    try {
        val lat = "18.9472"
        val lng = "72.8336" // Crawford Market Mumbai coordinates
        val geoUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(Shaheb Fruit Company HQ)")
        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        context.startActivity(mapIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Google Maps app is not installed.", Toast.LENGTH_SHORT).show()
    }
}

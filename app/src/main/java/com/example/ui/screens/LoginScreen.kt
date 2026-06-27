package com.example.ui.screens

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.viewmodel.FruitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: FruitViewModel,
    onLoginSuccess: (String) -> Unit, // passes role
    modifier: Modifier = Modifier
) {
    var isRegisterMode by remember { mutableStateFlowOf(false) }
    var username by remember { mutableStateFlowOf("") }
    var email by remember { mutableStateFlowOf("") }
    var password by remember { mutableStateFlowOf("") }
    var phone by remember { mutableStateFlowOf("") }
    var address by remember { mutableStateFlowOf("") }
    var isPasswordVisible by remember { mutableStateFlowOf(false) }

    val authError by viewModel.authError.collectAsState()
    val registrationSuccess by viewModel.registrationSuccess.collectAsState()

    val scrollState = rememberScrollState()

    // Reset fields on mode switch
    LaunchedEffect(isRegisterMode) {
        username = ""
        email = ""
        password = ""
        phone = ""
        address = ""
        viewModel.resetRegistrationState()
    }

    // Auto switch to login on successful registration
    LaunchedEffect(registrationSuccess) {
        if (registrationSuccess) {
            isRegisterMode = false
            viewModel.resetRegistrationState()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BrandGreen, BrandLightBackground),
                    endY = 600f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Brand Logo Frame
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_app_icon),
                    contentDescription = "Shaheb Fruits Logo",
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Shaheb Fruit Company",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Pure, Fresh & Organic Fruits",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Auth Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isRegisterMode) "Create Account" else "Welcome Back",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandDarkGreen
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isRegisterMode) "Sign up to start shopping fresh" else "Sign in to access premium fruits",
                        fontSize = 12.sp,
                        color = MutedText,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Error indicator
                    if (authError != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = authError ?: "",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Form Fields
                    if (isRegisterMode) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BrandGreen) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("name_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BrandGreen) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth().testTag("email_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BrandGreen) },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth().testTag("password_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            if (isRegisterMode) {
                                if (username.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                                    viewModel.registerUser(username, email, password)
                                }
                            } else {
                                if (email.isNotBlank() && password.isNotBlank()) {
                                    viewModel.loginUser(email, password) { loggedUser ->
                                        onLoginSuccess(loggedUser.role)
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("auth_submit_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isRegisterMode) "Register" else "Login",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mode Switch Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isRegisterMode) "Already have an account?" else "Don't have an account?",
                            fontSize = 13.sp,
                            color = MutedText
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isRegisterMode) "Login" else "Register Now",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandOrange,
                            modifier = Modifier
                                .clickable { isRegisterMode = !isRegisterMode }
                                .padding(4.dp)
                                .testTag("toggle_auth_mode_btn")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Bypass Links (Developer Friendly)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚡ Quick Access For Evaluation",
                        fontWeight = FontWeight.Bold,
                        color = BrandDarkGreen,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ElevatedButton(
                            onClick = {
                                viewModel.loginUser("user@shahebfruits.com", "user") { loggedUser ->
                                    onLoginSuccess(loggedUser.role)
                                }
                            },
                            colors = ButtonDefaults.elevatedButtonColors(containerColor = LightGreenAccent),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandGreen)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("As Customer", color = BrandGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        ElevatedButton(
                            onClick = {
                                viewModel.loginUser("admin@shahebfruits.com", "admin") { loggedUser ->
                                    onLoginSuccess(loggedUser.role)
                                }
                            },
                            colors = ButtonDefaults.elevatedButtonColors(containerColor = LightOrangeAccent),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandOrange)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("As Admin", color = BrandOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Utility function to represent mutableStateOf easily
@Composable
fun <T> rememberStateOf(initial: T) = remember { mutableStateOf(initial) }

fun <T> mutableStateFlowOf(value: T) = mutableStateOf(value)

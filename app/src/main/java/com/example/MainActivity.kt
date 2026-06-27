package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.FruitViewModel
import com.example.viewmodel.FruitViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Initialize the FruitViewModel using our custom Factory
                val viewModel: FruitViewModel = viewModel(
                    factory = FruitViewModelFactory(application)
                )

                AppNavigation(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: FruitViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = Modifier.fillMaxSize()
    ) {
        // Auth screen (Login/Register)
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { role ->
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // Shop Landing Home screen
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToProduct = { productId ->
                    navController.navigate("product_detail/$productId")
                },
                onNavigateToCart = {
                    navController.navigate("cart")
                },
                onNavigateToOrders = {
                    navController.navigate("order_tracking/-1") // -1 means track newest order
                },
                onNavigateToAdmin = {
                    navController.navigate("admin_dashboard")
                },
                onNavigateToInfo = { page ->
                    navController.navigate("info_pages/$page")
                },
                onNavigateToWishlist = {
                    navController.navigate("wishlist")
                },
                onNavigateToNotifications = {
                    navController.navigate("notifications")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        // Saved Wishlist screen
        composable("wishlist") {
            WishlistScreen(
                viewModel = viewModel,
                onNavigateToProduct = { productId ->
                    navController.navigate("product_detail/$productId")
                },
                onNavigateHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Product Specification Detail screen
        composable(
            route = "product_detail/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
            ProductDetailsScreen(
                productId = productId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Shopping Cart screen
        composable("cart") {
            CartScreen(
                viewModel = viewModel,
                onNavigateToCheckout = {
                    navController.navigate("checkout")
                },
                onNavigateHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Secure checkout screen
        composable("checkout") {
            CheckoutScreen(
                viewModel = viewModel,
                onOrderPlaced = { orderId ->
                    navController.navigate("order_tracking/$orderId") {
                        popUpTo("cart") { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Order tracing timeline screen
        composable(
            route = "order_tracking/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.IntType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getInt("orderId") ?: -1
            val targetOrderId = if (orderId == -1) null else orderId

            TrackingScreen(
                viewModel = viewModel,
                targetOrderId = targetOrderId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Info and Policy sheets screen
        composable(
            route = "info_pages/{pageType}",
            arguments = listOf(navArgument("pageType") { type = NavType.StringType })
        ) { backStackEntry ->
            val pageType = backStackEntry.arguments?.getString("pageType") ?: "About Us"
            InfoPagesScreen(
                pageType = pageType,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Admin panel screen
        composable("admin_dashboard") {
            AdminScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Notifications Inbox screen
        composable("notifications") {
            NotificationsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

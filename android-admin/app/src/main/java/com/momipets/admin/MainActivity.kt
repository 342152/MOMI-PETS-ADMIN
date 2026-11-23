package com.momipets.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import com.momipets.admin.data.AdminWebsiteSettings
import com.momipets.admin.data.FirebaseAdminRepository
import com.momipets.admin.ui.screens.*
import com.momipets.admin.ui.theme.ForestDark
import com.momipets.admin.ui.theme.MomiPetsAdminTheme

private enum class AdminTab(val label: String, val icon: ImageVector) {
    Dashboard("Home", Icons.Default.Home),
    Orders("Orders", Icons.Default.ShoppingCart),
    Products("Products", Icons.Default.Inventory2),
    Suppliers("Suppliers", Icons.Default.LocalShipping),
    Profits("Profits", Icons.Default.Assessment),
    Website("Site", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MomiPetsAdminTheme {
                var isAuthorized by remember { mutableStateOf(false) }
                var checkedInitialAuth by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    val user = FirebaseAdminRepository.auth.currentUser
                    isAuthorized = if (user != null) {
                        FirebaseAdminRepository.verifyOwnerAccess(user)
                    } else false
                    checkedInitialAuth = true
                }

                if (!checkedInitialAuth) {
                    Box(modifier = Modifier.fillMaxSize())
                } else if (!isAuthorized) {
                    LoginScreen(onAuthenticated = { isAuthorized = true })
                } else {
                    AdminShell(onSignOut = {
                        FirebaseAdminRepository.signOut()
                        isAuthorized = false
                    })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminShell(onSignOut: () -> Unit) {
    var currentTab by remember { mutableStateOf(AdminTab.Dashboard) }

    val orders by FirebaseAdminRepository.observeOrders().collectAsState(initial = emptyList())
    val products by FirebaseAdminRepository.observeProducts().collectAsState(initial = emptyList())
    val categories by FirebaseAdminRepository.observeCategories().collectAsState(initial = emptyList())
    val suppliers by FirebaseAdminRepository.observeSuppliers().collectAsState(initial = emptyList())
    val payments by FirebaseAdminRepository.observePayments().collectAsState(initial = emptyList())
    val commissions by FirebaseAdminRepository.observeCommissions().collectAsState(initial = emptyList())
    val notifications by FirebaseAdminRepository.observeNotifications().collectAsState(initial = emptyList())
    val settings by FirebaseAdminRepository.observeWebsiteSettings().collectAsState(initial = AdminWebsiteSettings())
    val banners by FirebaseAdminRepository.observeBanners().collectAsState(initial = emptyList())

    val uriHandler = LocalUriHandler.current
    val waDigits = settings.whatsappNumber.replace(Regex("[^0-9]"), "").let {
        if (it.startsWith("0")) "92${it.drop(1)}" else it
    }.ifBlank { "923363958385" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MOMI PETS ADMIN") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ForestDark,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { uriHandler.openUri("https://wa.me/$waDigits") }) {
                        Icon(Icons.Default.Chat, contentDescription = "WhatsApp Support (03363958385)")
                    }
                    IconButton(onClick = onSignOut) {
                        Icon(Icons.Default.Logout, contentDescription = "Sign Out")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                AdminTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentTab) {
                AdminTab.Dashboard -> DashboardScreen(orders, commissions, payments, notifications)
                AdminTab.Orders -> OrdersScreen(orders)
                AdminTab.Products -> ProductsScreen(products, categories, suppliers)
                AdminTab.Suppliers -> SuppliersScreen(suppliers, payments)
                AdminTab.Profits -> ProfitsScreen(orders, commissions, suppliers)
                AdminTab.Website -> WebsiteSettingsScreen(settings, banners)
            }
        }
    }
}
